package com.example.stego.videoprocessingservice.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient fileServiceRestClient(
            RestClient.Builder builder,
            @Value("${services.file-service-url}") String fileServiceUrl
    ) {
        return builder.baseUrl(fileServiceUrl).build();
    }

    @Bean
    public RestClient pqcServiceRestClient(
            RestClient.Builder builder,
            @Value("${services.pqc-service-url}") String pqcServiceUrl
    ) {
        return builder.baseUrl(pqcServiceUrl).build();
    }

}
