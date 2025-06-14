package com.ducatillon.opensearch;

import org.opensearch.client.opensearch.indices.IndexState;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.lang.Long.parseLong;

@RestController
@RequestMapping
public class PersonController {

    PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @PostMapping
    public void postPerson(@RequestBody Person person) {
        personService.addPerson(person);
    }

    @GetMapping("/persons")
    public List<Person> getPersons() {
        return personService.findAllPersons();
    }
    @GetMapping("/persons/{id}")
    public ResponseEntity<Person> getPersonById(@PathVariable String id) throws IOException {
        Person person = SearchClientConfiguration.getPersonById(id);
        return new ResponseEntity<>(person, HttpStatus.OK);
    }

    @DeleteMapping
    public void deletePerson(@RequestParam("id") Long id) {}

    @GetMapping("/health")
    public String getClusterHealthStatus() throws IOException {
        return SearchClientConfiguration.getClusterHealthStatus();
    }

    @GetMapping("/indexes")
    public Map<String, IndexState> getListOfIndexesInAlias() throws IOException {

        return SearchClientConfiguration.getListOfIndexesInAlias();
    }

}
