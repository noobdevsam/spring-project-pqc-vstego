package com.example.stego.orchestrationservice.configs;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${pqcstego.topics.request-encode}")
    private String encodeRequestTopic;

    @Value("${pqcstego.topics.request-decode}")
    private String decodeRequestTopic;

    @Value("${pqcstego.topics.job-completion}")
    private String jobCompletionTopic;

    @Bean
    public NewTopic encodeRequestTopic() {
        return TopicBuilder.name(encodeRequestTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic decodeRequestTopic() {
        return TopicBuilder.name(decodeRequestTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic jobCompletionTopic() {
        return TopicBuilder.name(jobCompletionTopic).partitions(3).replicas(1).build();
    }

}
