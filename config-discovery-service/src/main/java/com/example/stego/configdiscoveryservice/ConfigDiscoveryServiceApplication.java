package com.example.stego.configdiscoveryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class ConfigDiscoveryServiceApplication {

    static void main(String[] args) {
        SpringApplication.run(ConfigDiscoveryServiceApplication.class, args);
    }

}
