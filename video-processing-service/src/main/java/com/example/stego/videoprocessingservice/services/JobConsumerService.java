package com.example.stego.videoprocessingservice.services;

import com.example.stego.videoprocessingservice.model.KafkaDecodeRequest;
import com.example.stego.videoprocessingservice.model.KafkaEncodeRequest;

public interface JobConsumerService {

    void handleEncodeRequest(KafkaEncodeRequest kafkaEncodeRequest);

    void handleDecodeRequest(KafkaDecodeRequest kafkaDecodeRequest);

}
