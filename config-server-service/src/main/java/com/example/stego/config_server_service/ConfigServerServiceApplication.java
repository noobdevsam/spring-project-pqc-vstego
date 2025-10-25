package com.example.stego.config_server_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class ConfigServerServiceApplication {

    static void main(String[] args) {
        SpringApplication.run(ConfigServerServiceApplication.class, args);
    }

}
