package com.ducatillon.opensearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.util.EntityUtils;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.GetIndexRequest;
import org.opensearch.client.opensearch.indices.GetIndexResponse;
import org.opensearch.client.opensearch.indices.IndexState;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;


@Service
class SearchClientConfiguration {

    private final OpenSearchConfiguration config;

    private static OpenSearchClient client;

    private static RestClient restClient;

    private static final ObjectMapper objectMapper = new ObjectMapper(); // Reuse ObjectMapper instance


    public SearchClientConfiguration(OpenSearchConfiguration config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        final CredentialsProvider credentialsProvider =  new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(config.getUsername(), config.getPassword()));

        RestClientBuilder restClientBuilder = RestClient
                .builder(new HttpHost(config.getHost(), Integer.parseInt(config.getPort()),config.getProtocol()))
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));

        restClient = restClientBuilder.build();

        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        client = new OpenSearchClient(transport);
    }

    public static String getClusterHealthStatus() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Request request = new Request("GET", "/_cluster/health");
        Response response = restClient.performRequest(request);
        String responseBody = EntityUtils.toString(response.getEntity());
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.path("status").asText();
    }

    public static Map<String, IndexState> getListOfIndexesInAlias() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest.Builder()
                .index("*").build();
        GetIndexResponse getIndexResponse = client.indices().get(getIndexRequest);
        return getIndexResponse.result();
    }

    public static Person getPersonById(String id) throws IOException {
        Request request = new Request("GET", "/person/_doc/" + id);
        Response response = restClient.performRequest(request);

        if (response.getStatusLine().getStatusCode() == 404) {
            return null;
        }

        String responseBody = EntityUtils.toString(response.getEntity());
        // Debugging print to see the full response
        System.out.println("Full OpenSearch response for ID " + id + ": " + responseBody);
        JsonNode rootNode = objectMapper.readTree(responseBody);

        boolean found = rootNode.path("found").asBoolean(false);
        if (!found) {
            System.out.println("Document with ID " + id + " not found.");
            return null; // Or handle as appropriate
        }
        // Navigate to the _source node
        JsonNode sourceNode = rootNode.path("_source");
        Person person = objectMapper.treeToValue(sourceNode, Person.class);
        if (person != null && rootNode.has("_id")) {
            person.setId(rootNode.path("_id").asLong());
        }

        return person;
    }

}