package com.example.stego.orchestrationservice.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {

    // 1. Create a load-balanced RestClient builder bean
    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    // 2. Create  RestClient instances for each service
    @Bean
    public RestClient fileServiceRestClient(
            RestClient.Builder builder,
            @Value("${services.file-service-url}") String fileServiceUrl
    ) {
        return builder.baseUrl(fileServiceUrl).build();
    }

    @Bean
    public RestClient videoServiceRestClient(
            RestClient.Builder builder,
            @Value("${services.video-service-url}") String videoServiceUrl
    ) {
        return builder.baseUrl(videoServiceUrl).build();
    }

}
