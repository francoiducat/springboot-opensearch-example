package com.ducatillon.opensearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;


@Service
@Slf4j
class SearchService {

    private static final String INDEX = "person";
    private static final String MAPPING = "opensearch/mapping.json";

    private final OpenSearchConfiguration config;
    private static OpenSearchClient client;
    private static RestClient restClient;
    private static final ObjectMapper objectMapper = new ObjectMapper(); // Reuse ObjectMapper instance


    public SearchService(OpenSearchConfiguration config) {
        this.config = config;
    }

    @PostConstruct
    public void init() throws IOException {
        final CredentialsProvider credentialsProvider =  new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(config.getUsername(), config.getPassword()));

        RestClientBuilder restClientBuilder = RestClient
                .builder(new HttpHost(config.getHost(), Integer.parseInt(config.getPort()),config.getProtocol()))
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));

        restClient = restClientBuilder.build();

        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        client = new OpenSearchClient(transport);


        createIndexIfNotExists();
    }

    private static void createIndexIfNotExists() throws IOException {
        // Check if the index exists and create it if it does not
        GetIndexRequest getIndexRequest = new GetIndexRequest.Builder()
                .index(INDEX).build();

        try {
            client.indices().get(getIndexRequest);
            log.info("OpenSearch index '{}' already exists. Skipping creation.", INDEX);
        } catch (Exception exception) {
            // If the index does not exist, an exception will be thrown
            log.info("OpenSearch index '{}' does not exist. Creating it with custom mapping via OpenSearchClient.", INDEX);
            try {
                ClassPathResource resource = new ClassPathResource(MAPPING);
                String mappingJsonString = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

                Request request = new Request("PUT", "/" + INDEX);
                request.setJsonEntity(mappingJsonString);

                Response response = restClient.performRequest(request);

                if (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 300) {
                    log.info("OpenSearch index '{}' created successfully with custom mapping. Response: {}", INDEX, response.getStatusLine());
                } else {
                    log.error("Failed to create OpenSearch index '{}' with custom mapping. Response: {} Body: {}", INDEX, response.getStatusLine(), StreamUtils.copyToString(response.getEntity().getContent(), StandardCharsets.UTF_8));
                    throw new RuntimeException("Failed to create OpenSearch index on startup due to API error.");
                }

            } catch (IOException e) {
                log.error("Failed to read mapping file for '{}': {}", INDEX, e.getMessage(), e);
                throw new RuntimeException("Failed to load OpenSearch index mapping on startup", e);
            } catch (Exception e) {
                log.error("An unexpected error occurred while creating OpenSearch index '{}': {}", INDEX, e.getMessage(), e);
                throw new RuntimeException("Unexpected error during OpenSearch index creation", e);
            }
        }

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
        Request request = new Request("GET", "/person/_search" );
        String query = String.format("""
                {
                  "query": {
                    "match": {
                      "id": %s
                    }
                  }
                }
                """, id);

        request.setJsonEntity(query);
        Response response = restClient.performRequest(request);

        if (response.getStatusLine().getStatusCode() == 404) {
            return null;
        }

        String responseBody = EntityUtils.toString(response.getEntity());
        // Debugging print to see the full response
        System.out.println("Full OpenSearch response for Person with id  " + id + ": " + responseBody);
        JsonNode rootNode = objectMapper.readTree(responseBody);

        // Navigate to the _source node
        JsonNode sourceNode = rootNode.path("hits").path("hits").get(0).path("_source");
        Person person = objectMapper.treeToValue(sourceNode, Person.class);
        if (person != null) {
            person.setId(sourceNode.path("id").asLong());
            person.setFirstName(sourceNode.path("firstName").asText());
            person.setLastName(sourceNode.path("lastName").asText());
        }
        return person;
    }

    public static void addPerson(Person person) throws IOException {
        String json = objectMapper.writeValueAsString(person);
        Request request = new Request("POST", "/person/_doc");
        request.setJsonEntity(json);
        Response response = restClient.performRequest(request);

        if (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 300) {
            log.info("Person added successfully: {}", person);
        } else {
            log.error("Failed to add person: {}. Response: {}", person, response.getStatusLine());
            throw new RuntimeException("Failed to add person to OpenSearch index.");
        }
    }

}