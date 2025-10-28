package com.example.stego.orchestrationservice.services.impl;

import com.example.stego.orchestrationservice.model.KafkaDecodeRequest;
import com.example.stego.orchestrationservice.model.KafkaEncodeRequest;
import com.example.stego.orchestrationservice.services.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${pqcstego.topics.request-encode}")
    private String encodeRequestTopic;

    @Value("${pqcstego.topics.request-decode}")
    private String decodeRequestTopic;

    @Override
    public void sendEncodeRequest(KafkaEncodeRequest kafkaEncodeRequest) {
        kafkaTemplate.send(encodeRequestTopic, kafkaEncodeRequest.getJobId(), kafkaEncodeRequest);
    }

    @Override
    public void sendDecodeRequest(KafkaDecodeRequest kafkaDecodeRequest) {
        kafkaTemplate.send(decodeRequestTopic, kafkaDecodeRequest.getJobId(), kafkaDecodeRequest);
    }

}
