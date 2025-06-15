package com.ducatillon.opensearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.opensearch.client.RestClient;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.GetIndexRequest;
import org.opensearch.client.opensearch.indices.GetIndexResponse;
import org.opensearch.client.opensearch.indices.IndexState;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.util.Map;

import static com.ducatillon.opensearch.OpenSearchConfiguration.getOpenSearchClient;
import static com.ducatillon.opensearch.OpenSearchConfiguration.getRestClient;


@Service
@Slf4j
@AllArgsConstructor
class SearchService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String getClusterHealthStatus() throws IOException {
        Request request = new Request("GET", "/_cluster/health");
        Response response = getRestClient().performRequest(request);
        String responseBody = EntityUtils.toString(response.getEntity());
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.path("status").asText();
    }

    public static Map<String, IndexState> getListOfIndexesInAlias() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest.Builder()
                .index("*").build();
        GetIndexResponse getIndexResponse = getOpenSearchClient().indices().get(getIndexRequest);
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
        Response response = getRestClient().performRequest(request);

        if (response.getStatusLine().getStatusCode() == 404) {
            return null;
        }

        String responseBody = EntityUtils.toString(response.getEntity());
        // Debugging print to see the full response
        System.out.println("Full OpenSearch response for Person with id  " + id + ": " + responseBody);
        JsonNode rootNode = objectMapper.readTree(responseBody);

        // Navigate to the _source node
        if( rootNode.path("hits").path("hits").isEmpty()) {
            log.warn("No person found with id: {}", id);
            return null;
        }
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
        Response response = getRestClient().performRequest(request);

        if (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 300) {
            log.info("Person added successfully: {}", person);
        } else {
            log.error("Failed to add person: {}. Response: {}", person, response.getStatusLine());
            throw new RuntimeException("Failed to add person to OpenSearch index.");
        }
    }

}