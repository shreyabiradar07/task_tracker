package org.example.datasource;

import com.google.gson.Gson;

public class ImportDataJsonObject {
    private String version;
    private ClusterGroup clusterGroup;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public ClusterGroup getClusterGroup() {
        return clusterGroup;
    }

    public void setClusterGroup(ClusterGroup clusterGroup) {
        this.clusterGroup = clusterGroup;
    }

    // Nested classes representing the JSON structure
    public static class ClusterGroup {
        private String clusterGroupName;
        private Cluster cluster;

        public String getClusterGroupName() {
            return clusterGroupName;
        }

        public void setClusterGroupName(String clusterGroupName) {
            this.clusterGroupName = clusterGroupName;
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
        private String clusterName;
        private Namespace nameSpace;

        public String getClusterName() {
            return clusterName;
        }

        public void setClusterName(String clusterName) {
            this.clusterName = clusterName;
        }

        public Namespace getNamespace() {
            return nameSpace;
        }

        public void setNamespace(Namespace nameSpace) {
            this.nameSpace = nameSpace;
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
}
