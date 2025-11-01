package com.example.stego.cryptographyservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableDiscoveryClient
@EnableMongoAuditing
public class PqcServiceApplication {

    static void main(String[] args) {
        SpringApplication.run(PqcServiceApplication.class, args);
    }

}
