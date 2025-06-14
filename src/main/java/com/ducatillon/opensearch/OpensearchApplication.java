package com.ducatillon.opensearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;


@SpringBootApplication(exclude = ElasticsearchDataAutoConfiguration.class)
public class OpensearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(OpensearchApplication.class, args);
	}

}
