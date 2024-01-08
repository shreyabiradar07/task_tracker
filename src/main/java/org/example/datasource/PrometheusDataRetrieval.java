package org.example.datasource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.datasource.ImportDataJsonObject;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Map;

public class PrometheusDataRetrieval {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusDataRetrieval.class);
    public PrometheusDataRetrieval(){
        System.out.println("Prometheus instance being invoked");
    }
    public List<ImportDataJsonObject> retrievePrometheusData() {
        String PROMETHEUS_URL = "http://localhost:9090/api/v1/query";
        String PROMETHEUS_QUERY = "sum by (namespace) (kube_namespace_status_phase{phase=\"Active\"})";
        List<ImportDataJsonObject> importDataList = new ArrayList<>();

        try {
            String encodedQuery = URLEncoder.encode(PROMETHEUS_QUERY, "UTF-8");
            URL url = new URL(PROMETHEUS_URL + "?query=" + encodedQuery);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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

                // Parse the JSON response from Prometheus to extract namespaces
                List<String> namespaces = parsePrometheusResponse(response.toString());

                // Create a ImportDataJsonObject instance including each namespace
                    ImportDataJsonObject importData = createImportDataObject(namespaces);
                    importDataList.add(importData);

            } else {
                System.out.println("Error - HTTP Response Code: " + responseCode);
            }

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return importDataList;
    }
    private List<String> parsePrometheusResponse(String jsonResponse) {
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

    private ImportDataJsonObject createImportDataObject(List<String> namespaces) {
        ImportDataJsonObject output = new ImportDataJsonObject();
        output.setVersion("1.0");

        // Set cluster and group details as needed
        ImportDataJsonObject.ClusterGroup clusterGroup = new ImportDataJsonObject.ClusterGroup();
        clusterGroup.setClusterGroupName("prometheus");

        ImportDataJsonObject.Cluster cluster = new ImportDataJsonObject.Cluster();
        cluster.setClusterName("k8s-cluster");

        // Create a list to store multiple Namespace objects
        List<ImportDataJsonObject.Namespace> namespaceList = new ArrayList<>();

        // Set the fetched namespace
        for (String fetchedNamespace : namespaces) {
            ImportDataJsonObject.Namespace namespace = new ImportDataJsonObject.Namespace();
            namespace.setNamespaceName(fetchedNamespace);
            namespaceList.add(namespace);
        }

        cluster.setNamespaces(namespaceList);
        clusterGroup.setCluster(cluster);
        output.setClusterGroup(clusterGroup);

        return output;
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
        writeJsonToFile(jsonOutput, "output.json");
    }
}
