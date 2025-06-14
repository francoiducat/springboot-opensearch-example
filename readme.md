# Spring Boot OpenSearch Example
This project demonstrates how to integrate OpenSearch with a Spring Boot application. It provides a simple example of indexing and searching data using OpenSearch.

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

# Getting Started

Clone the repository:

```bash
git clone https://github.com/YOUR_USERNAME/springboot-opensearch-example.git
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

The PersonController provides a basic endpoint:

POST /person: This endpoint will trigger personService, which is where your OpenSearch indexing logic (or other OpenSearch operations) would reside.

Example cURL request:

```
curl -X POST \
     -H "Content-Type: application/json" \
     -d '{"firstName": "John", "lastName": "Doe", "id": 1}' \
     "http://localhost:8080/person"
```

Get all Persons from springboot app:
```
curl -X GET "localhost:8080/person"
```

Get all Person documents from OpenSearch:
```
curl -X GET "localhost:9200/person/_search"
```

# Contributing
Feel free to fork this repository, create your feature branch, and send a pull request!

# Acknowledgments
Spring Boot Documentation
OpenSearch Documentation