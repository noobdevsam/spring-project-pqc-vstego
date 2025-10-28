package com.example.stego.orchestrationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableDiscoveryClient
@EnableMongoAuditing
@EnableKafka
public class OrchestrationServiceApplication {

    static void main(String[] args) {
        SpringApplication.run(OrchestrationServiceApplication.class, args);
    }

}
