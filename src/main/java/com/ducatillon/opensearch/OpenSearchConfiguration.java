package com.ducatillon.opensearch;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.GetIndexRequest;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class OpenSearchConfiguration {

    private static final String INDEX = "person";
    private static final String MAPPING = "opensearch/mapping.json";

    private String host = "localhost";
    private String port = "9200";
    private String username = "";
    private String password = "";
    private String protocol = "http";

    private static RestClient restClient;
    private static OpenSearchClient openSearchClient;

    // Static getters for the initialized clients
    public static RestClient getRestClient() {
        return restClient;
    }

    public static OpenSearchClient getOpenSearchClient() {
        return openSearchClient;
    }

    @PostConstruct
    public void init() throws IOException {
        final CredentialsProvider credentialsProvider =  new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));

        RestClientBuilder restClientBuilder = RestClient
                .builder(new HttpHost(host, Integer.parseInt(port),protocol))
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));

        restClient = restClientBuilder.build();

        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        openSearchClient = new OpenSearchClient(transport);


        createIndexIfNotExists();
    }

    private static void createIndexIfNotExists() throws IOException {
        // Check if the index exists and create it if it does not
        GetIndexRequest getIndexRequest = new GetIndexRequest.Builder()
                .index(INDEX).build();

        try {
            openSearchClient.indices().get(getIndexRequest);
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
}
