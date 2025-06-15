package com.ducatillon.opensearch;

import org.opensearch.client.opensearch.indices.IndexState;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

import static java.lang.Long.parseLong;

@RestController
@RequestMapping
public class PersonController {

    SearchService searchService;

    public PersonController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping("/persons")
    public void postPerson(@RequestBody Person person) throws IOException {
        searchService.addPerson(person);
    }

    @GetMapping("/persons/{id}")
    public ResponseEntity<Person> getPersonById(@PathVariable String id) throws IOException {
        Person person = SearchService.getPersonById(id);
        return new ResponseEntity<>(person, HttpStatus.OK);
    }

    @DeleteMapping
    public void deletePerson(@RequestParam("id") Long id) {}

    @GetMapping("/health")
    public String getClusterHealthStatus() throws IOException {
        return SearchService.getClusterHealthStatus();
    }

    @GetMapping("/indexes")
    public Map<String, IndexState> getListOfIndexesInAlias() throws IOException {
        return SearchService.getListOfIndexesInAlias();
    }

}
