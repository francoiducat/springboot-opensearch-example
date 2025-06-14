package com.ducatillon.opensearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepository extends ElasticsearchRepository<Person, Long> {

    List<Person> findByLastName(String lastName);
    List<Person> findByFirstName(String firstName);
    List<Person> findAll();
}