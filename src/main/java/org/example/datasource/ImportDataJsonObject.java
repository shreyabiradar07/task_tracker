package org.example.datasource;


import java.util.List;

public class ImportDataJsonObject {
    private String version;
    private ClusterGroup cluster_group;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public ClusterGroup getClusterGroup() {
        return cluster_group;
    }

    public void setClusterGroup(ClusterGroup cluster_group) {
        this.cluster_group = cluster_group;
    }

    // Nested classes representing the JSON structure
    public static class ClusterGroup {
        private String cluster_group_name;
        private Cluster cluster;

        public String getClusterGroupName() {
            return cluster_group_name;
        }

        public void setClusterGroupName(String cluster_group_name) {
            this.cluster_group_name = cluster_group_name;
        }

        public Cluster getCluster() {
            return cluster;
        }

        public void setCluster(Cluster cluster) {
            this.cluster = cluster;
        }

        // Other getters and setters if needed
    }

    public static class Cluster {
        private String cluster_name;
        private List<Namespace> namespaces;

        public String getClusterName() {
            return cluster_name;
        }

        public void setClusterName(String cluster_name) {
            this.cluster_name = cluster_name;
        }

        public List<Namespace> getNamespaces() {
            return namespaces;
        }

        public void setNamespaces(List<Namespace> namespaces) {
            this.namespaces = namespaces;
        }
    }

    public static class Namespace {
        private String namespace;
        private List<Workload> workloads;
        public String getNamespaceName() {
            return namespace;
        }

        public void setNamespaceName(String namespace) {
            this.namespace = namespace;
        }

        public List<Workload> getWorkloads() {
            return workloads;
        }

        public void setWorkloads(List<Workload> workloads) {
            this.workloads = workloads;
        }
    }

    public static class Workload {
        private String workload_name;
        private String workload_type;
        private List<Containers> containers;

        public String getWorkloadName() {return workload_name;}

        public void setWorkloadName(String workload_name) {this.workload_name = workload_name;}

        public String getWorkloadType() {return workload_type;}

        public void setWorkloadType(String workload_type) {this.workload_type = workload_type;}
        public List<Containers> getContainers() {
            return containers;
        }

        public void setContainers(List<Containers> containers) {
            this.containers = containers;
        }

    }

    public static class Containers {
        private String container_name;
        private String container_image_name;
        public String getContainerName() { return container_name;}
        public void setContainerName(String container_name) {this.container_name = container_name;}
        public String getContainerImageName() { return container_image_name;}
        public void setContainerImageName(String container_image_name) {this.container_image_name = container_image_name;}

    }
}
