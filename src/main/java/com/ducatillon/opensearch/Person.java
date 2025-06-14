package com.ducatillon.opensearch;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.springframework.stereotype.Repository;


@Entity
@JsonIgnoreProperties(ignoreUnknown = true) // Add this annotation
public class Person {
    @Id
    private Long id;
    private String firstName;
    private String lastName;

    public Person() {}

    public Person(String firstName, String lastName, Long id) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Long getId() {
        return id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
