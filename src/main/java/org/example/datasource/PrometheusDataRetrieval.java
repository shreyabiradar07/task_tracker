package org.example.datasource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.datasource.ImportDataJsonObject;
import java.io.BufferedWriter;
import java.io.FileWriter;


public class PrometheusDataRetrieval {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusDataRetrieval.class);

    public PrometheusDataRetrieval(){
        System.out.println("Prometheus instance being invoked");
    }
    public List<ImportDataJsonObject> retrievePrometheusData() {
        String PROMETHEUS_URL = "http://localhost:9090/api/v1/query";
        String ACTIVE_NAMESPACES_QUERY = "sum by (namespace) (kube_namespace_status_phase{phase=\"Active\"})";
        String WORKLOAD_INFO_QUERY = "sum by (namespace, workload, workload_type) (namespace_workload_pod:kube_pod_owner:relabel)";
        String CONTAINERS_QUERY = "sum by (container, image, workload) (kube_pod_container_info * on(pod) group_left(workload, workload_type) (namespace_workload_pod:kube_pod_owner:relabel))";

        List<ImportDataJsonObject> importDataList = new ArrayList<>();
        // Fetch Active Namespaces
        List<String> activeNamespaces = fetchActiveNamespaces(PROMETHEUS_URL, ACTIVE_NAMESPACES_QUERY);

        // Fetch Workload Info
        Map<String, List<Map<String, String>>> namespaceWorkloadMap = fetchWorkloadInfo(PROMETHEUS_URL, WORKLOAD_INFO_QUERY);

        // Fetch Container Info
        Map<String, List<Map<String, String>>> workloadContainerMap = fetchContainerInfo(PROMETHEUS_URL, CONTAINERS_QUERY);

        ImportDataJsonObject importData =   createImportDataObject(activeNamespaces, namespaceWorkloadMap, workloadContainerMap);
        importDataList.add(importData);

        return importDataList;
    }
    private List<String> fetchActiveNamespaces(String url, String query) {
        List<String> activeNamespaces = new ArrayList<>();

        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            URL apiUrl = new URL(url + "?query=" + encodedQuery);

            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse the JSON response to extract active namespaces
                activeNamespaces = parseActiveNamespaces(response.toString());
            } else {
                System.out.println("Error - HTTP Response Code: " + responseCode);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return activeNamespaces;
    }
    private List<String> parseActiveNamespaces(String jsonResponse) {
        List<String> namespaces = new ArrayList<>();

        // Parse the JSON response and extract namespaces
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

        // Check if the response status is "success" and it contains data
        if (jsonObject.has("status") && jsonObject.get("status").getAsString().equals("success")
                && jsonObject.has("data") && jsonObject.get("data").isJsonObject()) {

            JsonObject dataObject = jsonObject.getAsJsonObject("data");

            // Check if the response data contains "result" as an array
            if (dataObject.has("result") && dataObject.get("result").isJsonArray()) {
                JsonArray resultArray = dataObject.getAsJsonArray("result");

                // Iterate through the "result" array to extract namespaces
                for (JsonElement result : resultArray) {
                    JsonObject resultObject = result.getAsJsonObject();

                    // Check if the result object contains the "metric" field with "namespace"
                    if (resultObject.has("metric") && resultObject.get("metric").isJsonObject()) {
                        JsonObject metricObject = resultObject.getAsJsonObject("metric");

                        // Extract the namespace value and add it to the list
                        if (metricObject.has("namespace")) {
                            String namespace = metricObject.get("namespace").getAsString();
                            namespaces.add(namespace);
                        }
                    }
                }
            }
        }

        return namespaces;
    }
    private Map<String, List<Map<String, String>>> fetchWorkloadInfo(String url, String query) {
        Map<String, List<Map<String, String>>> namespaceWorkloadMap = new HashMap<>();

        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            URL apiUrl = new URL(url + "?query=" + encodedQuery);

            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse the JSON response to extract workload info by namespaces
                namespaceWorkloadMap = parseWorkloadInfo(response.toString());
            } else {
                System.out.println("Error - HTTP Response Code: " + responseCode);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return namespaceWorkloadMap;
    }

    private Map<String, List<Map<String, String>>> parseWorkloadInfo(String jsonResponse) {
        Map<String, List<Map<String, String>>> namespaceWorkloadMap = new HashMap<>();

        // Parse the JSON response and extract namespaces
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

        // Check if the response status is "success" and it contains data
        if (jsonObject.has("status") && jsonObject.get("status").getAsString().equals("success")
                && jsonObject.has("data") && jsonObject.get("data").isJsonObject()) {

            JsonObject dataObject = jsonObject.getAsJsonObject("data");

            // Check if the response data contains "result" as an array
            if (dataObject.has("result") && dataObject.get("result").isJsonArray()) {
                JsonArray resultArray = dataObject.getAsJsonArray("result");

                // Iterate through the "result" array to extract namespaces
                for (JsonElement result : resultArray) {
                    JsonObject resultObject = result.getAsJsonObject();

                    // Check if the result object contains the "metric" field with "namespace"
                    if (resultObject.has("metric") && resultObject.get("metric").isJsonObject()) {
                        JsonObject metricObject = resultObject.getAsJsonObject("metric");

                        Map<String, String> workloadEntry = new HashMap<>();
                        // Extract the namespace value and add it to the list
                        if (metricObject.has("namespace")) {
                            String namespace = metricObject.get("namespace").getAsString();
                            String workload_name = metricObject.get("workload").getAsString();
                            String workload_type = metricObject.get("workload_type").getAsString();
                            workloadEntry.put(workload_name,workload_type);

                            // Using compute to compute the list for the namespace and add the workload entry
                            namespaceWorkloadMap.compute(namespace, (key, oldValue) -> {
                                if (oldValue == null) {
                                    List<Map<String, String>> newList = new ArrayList<>();
                                    newList.add(workloadEntry);
                                    return newList;
                                } else {
                                    oldValue.add(workloadEntry);
                                    return oldValue;
                                }
                            });
                        }
                    }
                }
            }
        }
        return namespaceWorkloadMap;
    }

    private Map<String, List<Map<String, String>>> fetchContainerInfo(String url, String query) {
        Map<String, List<Map<String, String>>> workloadContainerMap = new HashMap<>();

        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            URL apiUrl = new URL(url + "?query=" + encodedQuery);

            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse the JSON response to extract workload info by namespaces
                workloadContainerMap = parseContainerInfo(response.toString());
            } else {
                System.out.println("Error - HTTP Response Code: " + responseCode);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return workloadContainerMap;
    }

    private Map<String, List<Map<String, String>>> parseContainerInfo(String jsonResponse) {
        Map<String, List<Map<String, String>>> workloadContainerMap = new HashMap<>();

        // Parse the JSON response and extract namespaces
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

        // Check if the response status is "success" and it contains data
        if (jsonObject.has("status") && jsonObject.get("status").getAsString().equals("success")
                && jsonObject.has("data") && jsonObject.get("data").isJsonObject()) {

            JsonObject dataObject = jsonObject.getAsJsonObject("data");

            // Check if the response data contains "result" as an array
            if (dataObject.has("result") && dataObject.get("result").isJsonArray()) {
                JsonArray resultArray = dataObject.getAsJsonArray("result");

                // Iterate through the "result" array to extract namespaces
                for (JsonElement result : resultArray) {
                    JsonObject resultObject = result.getAsJsonObject();

                    // Check if the result object contains the "metric" field with "namespace"
                    if (resultObject.has("metric") && resultObject.get("metric").isJsonObject()) {
                        JsonObject metricObject = resultObject.getAsJsonObject("metric");

                        Map<String, String> containerEntry = new HashMap<>();
                        // Extract the namespace value and add it to the list
                        if (metricObject.has("workload")) {
                            String workload = metricObject.get("workload").getAsString();
                            String container_name = metricObject.get("container").getAsString();
                            String container_image_name = metricObject.get("image").getAsString();
                            containerEntry.put(container_name,container_image_name);

                            // Using compute to compute the list for the namespace and add the workload entry
                            workloadContainerMap.compute(workload, (key, oldValue) -> {
                                if (oldValue == null) {
                                    List<Map<String, String>> newList = new ArrayList<>();
                                    newList.add(containerEntry);
                                    return newList;
                                } else {
                                    oldValue.add(containerEntry);
                                    return oldValue;
                                }
                            });
                        }
                    }
                }
            }
        }
        return workloadContainerMap;
    }

    private ImportDataJsonObject createImportDataObject(List<String> activeNamespaces, Map<String, List<Map<String, String>>> namespaceWorkloadMap, Map<String, List<Map<String, String>>> workloadContainerMap) {
        ImportDataJsonObject dataObject = new ImportDataJsonObject();
        dataObject.setVersion("1.0");

        ImportDataJsonObject.ClusterGroup clusterGroup = new ImportDataJsonObject.ClusterGroup();
        clusterGroup.setClusterGroupName("prometheus");

        ImportDataJsonObject.Cluster cluster = new ImportDataJsonObject.Cluster();
        cluster.setClusterName("k8s-cluster");

        List<ImportDataJsonObject.Namespace> namespaceList = new ArrayList<>();

        for (String namespaceName : activeNamespaces) {
            ImportDataJsonObject.Namespace namespace = new ImportDataJsonObject.Namespace();
            namespace.setNamespaceName(namespaceName);

            List<Map<String, String>> workloadMaps = namespaceWorkloadMap.getOrDefault(namespaceName, Collections.emptyList());
            List<ImportDataJsonObject.Workload> workloadList = new ArrayList<>();

            System.out.println("workload maps = "+workloadMaps);
            for (Map<String, String> workloadMap : workloadMaps) {
                ImportDataJsonObject.Workload workload = new ImportDataJsonObject.Workload();

                for (Map.Entry<String, String> entry : workloadMap.entrySet()) {
                    String workloadName = entry.getKey();
                    String workloadType = entry.getValue();
                    workload.setWorkloadName(workloadName);
                    workload.setWorkloadType(workloadType);

                    List<Map<String, String>> containerMaps = workloadContainerMap.getOrDefault(workloadName, Collections.emptyList());
                    List<ImportDataJsonObject.Containers> containerList = new ArrayList<>();

                    for (Map<String, String> containerMap : containerMaps) {
                        ImportDataJsonObject.Containers container = new ImportDataJsonObject.Containers();
                        for (Map.Entry<String, String> containerEntry : containerMap.entrySet()) {
                            String containerName = containerEntry.getKey();
                            String containerImage = containerEntry.getValue();
                            container.setContainerName(containerName);
                            container.setContainerImageName(containerImage);
                            containerList.add(container);
                        }
                    }
                    workload.setContainers(containerList);
                }
                workloadList.add(workload);
            }
            namespace.setWorkloads(workloadList);
            namespaceList.add(namespace);
        }

        cluster.setNamespaces(namespaceList);
        clusterGroup.setCluster(cluster);
        dataObject.setClusterGroup(clusterGroup);

        return dataObject;
    }


    private static void writeJsonToFile(String jsonContent, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(jsonContent);
            System.out.println("JSON content written to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        PrometheusDataRetrieval prometheusDataRetrieval = new PrometheusDataRetrieval();
        List<ImportDataJsonObject> importDataList = prometheusDataRetrieval.retrievePrometheusData();

        // Convert the list of ImportDataJsonObject to JSON using Gson
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(importDataList);

        // Display the generated JSON
        //System.out.println(jsonOutput);

        // Write JSON output to a file
        writeJsonToFile(jsonOutput, "output1.json");
    }
}
