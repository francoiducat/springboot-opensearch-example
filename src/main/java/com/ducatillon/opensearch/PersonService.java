package com.ducatillon.opensearch;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonService {

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public void doWork() {

        personRepository.deleteAll();

        Person person = new Person("Oliver", "Gierke", 1L);
        personRepository.save(person);

        List<Person> lastNameResults = personRepository.findByLastName("Gierke");
        List<Person> firstNameResults = personRepository.findByFirstName("Oliver");

        System.out.println("Found " + lastNameResults.size() + " persons with last name 'Gierke'");
        System.out.println("Persons with last name 'Gierke':" + lastNameResults.getFirst().toString());
    }
}