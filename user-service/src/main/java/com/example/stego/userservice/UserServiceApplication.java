package com.example.stego.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableDiscoveryClient
@EnableMongoAuditing // Enable auto population of auditing fields
public class UserServiceApplication {

    static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

}
