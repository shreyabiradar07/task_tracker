import requests
import csv
import sched
import time
from datetime import datetime, timedelta
import subprocess
import sys, getopt


csv_headers = ['interval_start','interval_end','container_name','image_name','pod','owner_name','owner_kind','workload','workload_type','namespace','node','memory_usage_container_max']

queries_map = {
    "image_owners": "max_over_time(kube_pod_container_info{container!='', container!='POD', pod!='', namespace!=''}[15m]) * on(pod) group_left(owner_kind, owner_name) max by(pod, owner_kind, owner_name) (max_over_time(kube_pod_owner{container!='', container!='POD', pod!='', namespace!=''}[15m]))",
    "image_workloads": "max_over_time(kube_pod_container_info{container!='', container!='POD', pod!='', namespace!=''}[15m]) * on(pod) group_left(workload, workload_type) max by(pod, workload, workload_type) (max_over_time(namespace_workload_pod:kube_pod_owner:relabel{pod!='', namespace!=''}[15m]))",
    "memory_usage_container_max":  "max by(container, pod, namespace, node) (max_over_time(container_memory_working_set_bytes{pod!='', namespace!=''}[15m]))"
}


def run_queries():
    TOKEN = 'TOKEN'
    prometheus_url = None
    if cluster_type == "openshift":
        output = subprocess.check_output(['oc', 'whoami', '--show-token'])
        TOKEN = output.decode().strip()
        prometheus_url = f"https://thanos-querier-openshift-monitoring.apps.{server}/api/v1/query"
    elif cluster_type == "minikube":
        prometheus_url = f"http://{server}:9090/api/v1/query"
    headers = {'Authorization': f'Bearer {TOKEN}'}
    print("running queries")
    results_map = {}
    for key, query in queries_map.items():
        response = requests.get(prometheus_url, headers=headers, params={'query': query}, verify=False)
        if response.status_code == 200:
            results_map[key] = response.json()['data']['result']
        else:
            print(f"Failed to run query '{query}' with status code {response.status_code}")

    return results_map

def parse_results(results_map):
    imageowners = results_map["image_owners"]
    imageworkloads = results_map["image_workloads"]
    #result_maps= ["cpu_usage_container_avg", "cpu_usage_container_min", "cpu_usage_container_max", "cpu_usage_container_sum","memory_usage_container_avg", "memory_usage_container_min", "memory_usage_container_max", "memory_usage_container_sum"]
    result_maps = ["memory_usage_container_max"]
    result_map_values = [None] * len(result_maps)
    rows = []
    result_map_node = ""

    for data in imageowners:
        for workloaddata in imageworkloads:
            if data["metric"]["container_id"] == workloaddata["metric"]["container_id"]:
                for i, result_map in enumerate(result_maps):
                    for result in results_map[result_map]:
                        if result["metric"]["pod"] == data["metric"]["pod"]:
                            result_map_values[i] = result["value"][1]
                            result_map_node = result["metric"]["node"]

                row = {'container_name': data["metric"]["container"],
                       'image_name': data["metric"]["image"],
                       'pod': data["metric"]["pod"],
                       'owner_name': data["metric"]["owner_name"],
                       'owner_kind': data["metric"]["owner_kind"],
                       'workload': workloaddata["metric"]["workload"],
                       'workload_type': workloaddata["metric"]["workload_type"],
                       'namespace': data["metric"]["namespace"],
                       'node': result_map_node,
                       'memory_usage_container_max': result_map_values[0]
                       }

                rows.append(row)

    return rows

def write_header_to_csv():
    with open(clusterResults, 'w') as f:
        writer = csv.DictWriter(f, fieldnames=csv_headers)
        writer.writeheader()


def write_results_to_csv(rows):
    with open(clusterResults, 'a') as f:
        writer = csv.DictWriter(f, fieldnames=csv_headers)
        for row in rows:
            if row['memory_usage_container_max'] is not None:
                writer.writerow(row)
    with open("intervalResults.csv", 'w') as f:
        writer = csv.DictWriter(f, fieldnames=csv_headers)
        writer.writeheader()
        for row in rows:
            if row['memory_usage_container_max'] is not None:
                writer.writerow(row)

def job():
    # Get current timestamp and run queries
    # Get the current time in UTC
    now_utc = datetime.utcnow()
    # Subtract 15 minutes from the current time
    time_15mins_ago_utc = now_utc - timedelta(minutes=30)

    # Format the time as an ISO 8601 string
    timestamp_15mins_ago_utc = time_15mins_ago_utc.isoformat()
    timestamp_utc = now_utc.isoformat()
    results_map = run_queries()

    #print("interval start time = ", timestamp_15mins_ago_utc)
    #print("interval end time = ", timestamp_utc)
    # Parse results and store in rows
    rows = parse_results(results_map)
    for row in rows:
        row['interval_end'] = timestamp_utc
        row['interval_start'] = timestamp_15mins_ago_utc

        # Write rows to CSV file
    write_results_to_csv(rows)


def main(argv):
    global duration
    global cluster_type
    global server
    global clusterResults
    try:
        opts, args = getopt.getopt(argv,"h:c:s:d:r:")
    except getopt.GetoptError:
        print("recommendation_experiment.py -c <cluster type> -s <server>")
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            print("recommendation_experiment.py -c <cluster type>")
            sys.exit()
        elif opt == '-c':
            cluster_type = arg
        elif opt == '-s':
            server = arg
        elif opt == '-d':
            duration = arg
        elif opt == '-r':
            clusterResults = arg

    # Default duration to 1 hour if not passed.
    if '-d' not in sys.argv:
        duration = 1
    if '-r' not in sys.argv:
        clusterResults = 'clusterresults.csv'

    # Create a csv with header
    write_header_to_csv()

    # Create a scheduler object
    scheduler = sched.scheduler(time.time, time.sleep)

    # Schedule the function to run every 15 minutes for the next 6 hours
    now = datetime.utcnow()
    end = now + timedelta(hours=duration)

    while now < end:
        #print("now= ", now)
        scheduler.enterabs(now.timestamp(), 1, job, ())
        now += timedelta(minutes=30)
        # Start the scheduler
        scheduler.run()
        time.sleep(1800)



if __name__ == '__main__':
    main(sys.argv[1:])