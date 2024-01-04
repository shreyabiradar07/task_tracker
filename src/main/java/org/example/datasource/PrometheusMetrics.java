package org.example.datasource;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrometheusMetrics {
    private static Map<String, List<Map<String, Object>>> runQueries(String clusterType, String server, Map<String, String> queriesMap) {
        String TOKEN = "TOKEN"; // Replace with the default token
        String prometheusUrl = null;
        Map<String, List<Map<String, Object>>> resultsMap = new HashMap<>();

        try {
            if (clusterType.equals("openshift")) {
                Process process = Runtime.getRuntime().exec(new String[]{"oc", "whoami", "--show-token"});
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                TOKEN = reader.readLine().trim();
                prometheusUrl = "https://thanos-querier-openshift-monitoring.apps." + server + "/api/v1/query";
            } else if (clusterType.equals("minikube")) {
                prometheusUrl = "http://" + server + ":9090/api/v1/query";
            }

            for (Map.Entry<String, String> entry : queriesMap.entrySet()) {
                String key = entry.getKey();
                String query = entry.getValue();

                URL url = new URL(prometheusUrl + "?query=" + query);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + TOKEN);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    List<Map<String, Object>> parsedResponse = parseResponse(response.toString());
                    resultsMap.put(key, parsedResponse);
                } else {
                    System.out.println("Failed to run query '" + query + "' with status code: " + responseCode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(resultsMap);
        return resultsMap;
    }

    private static List<Map<String, Object>> parseResponse(String response) {
        List<Map<String, Object>> parsedData = new ArrayList<>();

        try {
            Gson gson = new Gson();
            Map<String, Object> jsonObject = gson.fromJson(response, new TypeToken<Map<String, Object>>(){}.getType());

            Map<String, Object> data = (Map<String, Object>) jsonObject.get("data");
            List<Map<String, Object>> result = (List<Map<String, Object>>) data.get("result");

            parsedData.addAll(result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return parsedData;
    }

    // Function to parse results and create JSON objects
    private static List<Map<String, Object>> parseResults(Map<String, List<Map<String, Object>>> resultsMap) {
        List<Map<String, Object>> rows = new ArrayList<>();

        // Logic to parse results and create JSON objects

        return rows;
    }

    // Function to write JSON objects to a file
    private static void writeResultsToJson(List<Map<String, Object>> rows) {
        // File path to write JSON objects
        String jsonFilePath = "metrics.json";

        try (Writer writer = new FileWriter(jsonFilePath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            // Convert rows to JSON and write to the file
            String json = gson.toJson(rows);
            writer.write(json);

            System.out.println("Metrics have been written to " + jsonFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String clusterType = "minikube";
        String server = "localhost";

        Map<String, String> queriesMap = new HashMap<>();
        queriesMap.put("image_owners", "max_over_time(kube_pod_container_info{container!='', container!='POD', pod!='', namespace!=''}[15m]) * on(pod) group_left(owner_kind, owner_name) max by(pod, owner_kind, owner_name) (max_over_time(kube_pod_owner{container!='', container!='POD', pod!='', namespace!=''}[15m]))");
        queriesMap.put("image_workloads", "max_over_time(kube_pod_container_info{container!='', container!='POD', pod!='', namespace!=''}[15m]) * on(pod) group_left(workload, workload_type) max by(pod, workload, workload_type) (max_over_time(namespace_workload_pod:kube_pod_owner:relabel{pod!='', namespace!=''}[15m]))");
        queriesMap.put("memory_usage_container_max", "max by(container, pod, namespace, node) (max_over_time(container_memory_working_set_bytes{pod!='', namespace!=''}[15m]))");

        // Function to run the Prometheus metrics extraction and conversion
        Map<String, List<Map<String, Object>>> resultsMap = runQueries(clusterType, server, queriesMap);
        List<Map<String, Object>> rows = parseResults(resultsMap);
        writeResultsToJson(rows);
    }
}