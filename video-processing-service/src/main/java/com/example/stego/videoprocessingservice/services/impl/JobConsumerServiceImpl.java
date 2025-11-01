package com.example.stego.videoprocessingservice.services.impl;

import com.example.stego.videoprocessingservice.model.KafkaDecodeRequest;
import com.example.stego.videoprocessingservice.model.KafkaEncodeRequest;
import com.example.stego.videoprocessingservice.services.JobConsumerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobConsumerServiceImpl implements JobConsumerService {

    @Override
    public void handleEncodeRequest(KafkaEncodeRequest kafkaEncodeRequest) {

    }

    @Override
    public void handleDecodeRequest(KafkaDecodeRequest kafkaDecodeRequest) {

    }

}
