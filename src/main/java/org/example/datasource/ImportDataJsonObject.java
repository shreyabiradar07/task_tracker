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


    public static void main(String[] args) {
        ImportDataJsonObject output = new ImportDataJsonObject();
        output.setVersion("1.0");

        ClusterGroup clusterGroup = new ClusterGroup();
        clusterGroup.setClusterGroupName("prometheus");

        Cluster cluster = new Cluster();
        cluster.setClusterName("k8s-cluster");

        Namespace nameSpace = new Namespace();
        // Assuming fetchedNamespace is the value obtained from the Prometheus query
        String fetchedNamespace = "default";
        nameSpace.setNamespaceName(fetchedNamespace);

        cluster.setNamespace(nameSpace);
        clusterGroup.setCluster(cluster);
        output.setClusterGroup(clusterGroup);

        // Convert the Java object to JSON using Gson
        Gson gson = new Gson();
        String jsonOutput = gson.toJson(output);

        // Display the generated JSON
        System.out.println(jsonOutput);
    }


}
