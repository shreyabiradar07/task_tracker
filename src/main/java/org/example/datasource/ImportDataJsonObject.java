package org.example.datasource;


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
        private Namespace name_space;

        public String getClusterName() {
            return cluster_name;
        }

        public void setClusterName(String cluster_name) {
            this.cluster_name = cluster_name;
        }

        public Namespace getNamespace() {
            return name_space;
        }

        public void setNamespace(Namespace name_space) {
            this.name_space = name_space;
        }
    }

    public static class Namespace {
        private String namespace;

        public String getNamespaceName() {
            return namespace;
        }

        public void setNamespaceName(String namespace) {
            this.namespace = namespace;
        }
    }

    public static class Workload {
        private String workload_name;
        private String workload_type;

        public String getWorkloadName() {return workload_name;}

        public void setWorkloadName(String workload_name) {this.workload_name = workload_name;}
        public String getWorkloadType() {return workload_type;}

        public void setWorkloadType(String workload_type) {this.workload_type = workload_type;}

    }
}
