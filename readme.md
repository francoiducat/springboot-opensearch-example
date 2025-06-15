# Spring Boot OpenSearch Example
This project demonstrates how to integrate OpenSearch with a Spring Boot application. 
It provides a simple example of indexing and searching data using OpenSearch.
It does not use the OpenSearch High-Level REST Client( which is deprecated), but instead uses the OpenSearchClient, which is a type-safe client built on top of the OpenSearch REST Client.
It does not use the spring-data-opensearch library, but instead uses the OpenSearchClient directly for indexing and searching documents.

Note that spring-data-opensearch loads spring-data-elasticsearch anyway, which may lead to conflicts with the OpenSearch connectivity, especially with the latest versions of OpenSearch.
Although spring-data-opensearch follows springboot and spring-data-elasticsearch release cycles, compatibility issues with OpenSearch may arise due to the differences in how OpenSearch and Elasticsearch handle certain features and configurations.

This repository aims to clarify opensearch connectivity issues with springboot and spring-data-elasticsearch/spring-data-open-search, and to provide a simple example of how to use OpenSearch with Spring Boot.

# Prerequisites

- JDK 21
- Spring Boot 3.5.0
- Apache Maven
- OpenSearch Instance 2.17.1
- Spring Data OpenSearch 1.7.0
- Docker (Recommended): The easiest way to get OpenSearch running locally is via Docker.
```bash
docker run -p 9200:9200 -p 9600:9600 -e "discovery.type=single-node" opensearchproject/opensearch:2.17.1
```

Alternatively, you can use the docker-compose file provided in this repository to start OpenSearch:

```bash
docker-compose up opensearch
```

and OpenSearch Dashboards:

```bash
docker-compose up opensearch-dashboards
```

# Getting Started

Clone the repository:

```bash
git clone https://github.com/francoiducat/springboot-opensearch-example.git
cd springboot-opensearch-example
```

Build the project:

```bash
mvn clean install
```

Run the Spring Boot application:

```
mvn spring-boot:run
```

# Usage

Once the application is running, you can interact with it using a tool like Postman or curl.

The PersonController provides basic endpoints:

## POST Person

This endpoint will trigger SearchService, which is where OpenSearch indexing logic would reside.

Example cURL request:

## Add a Person

Add a new Person:

```
curl -X POST \
-H "Content-Type: application/json" \
-d '{"firstName": "John", "lastName": "Doe", "id": 1}' \
"http://localhost:8080/persons"
```

## GET Persons by id

Get a Person by id from springboot app:

```
curl -X GET "localhost:8080/persons/id"
```

You can also ge all person documents from OpenSearch directly:

```
curl -X GET "localhost:9200/person/_search"
```

# RestClient vs OpenSearchClient vs Rest High-Level Client

## RestClient 
**RestClient** is a low-level client that provides a way to interact with OpenSearch using HTTP requests.
It is suitable for simple use cases and provides basic functionality for indexing and searching documents.

## OpenSearchClient
**OpenSearchClient** is a higher-level client that provides a more convenient API for interacting with OpenSearch.
type-safe OpenSearchClient that builds upon opensearch-rest-client to offer a more convenient and robust way to interact with OpenSearch.

## Rest High-Level Client
**Rest High-Level Client** is a deprecated client that was used in earlier versions of OpenSearch.
It is not recommended for new projects.

# Contributing
Feel free to fork this repository, create your feature branch, and send a pull request!

# Acknowledgments
Spring Boot Documentation
OpenSearch Documentation
https://docs.opensearch.org/docs/latest/clients/java/
