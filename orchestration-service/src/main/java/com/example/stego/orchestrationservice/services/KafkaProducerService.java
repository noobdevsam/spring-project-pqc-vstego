package com.example.stego.orchestrationservice.services;

import com.example.stego.orchestrationservice.model.KafkaDecodeRequest;
import com.example.stego.orchestrationservice.model.KafkaEncodeRequest;

public interface KafkaProducerService {

    void sendEncodeRequest(KafkaEncodeRequest kafkaEncodeRequest);

    void sendDecodeRequest(KafkaDecodeRequest kafkaDecodeRequest);

}
