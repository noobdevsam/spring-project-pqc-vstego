package com.example.stego.videoprocessingservice.services.impl;

import com.example.stego.videoprocessingservice.services.SteganographyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SteganographyServiceImpl implements SteganographyService {

    private static final byte[] PAYLOAD_TERMINATOR = "PQCSTEGO_END".getBytes();
    private final RestClient fileServiceRestClient;
    private final String FILE_SERVICE_UPLOAD_URI = "/api/v1/files/upload";


    @Override
    public void embedPayload(InputStream carrierVideoStream, byte[] payloadData, OutputStream stegoVideoOutputStream) throws IOException, InterruptedException {

    }

    @Override
    public byte[] extractPayload(InputStream stegoVideoInputStream) throws IOException, InterruptedException {
        return new byte[0];
    }

    @Override
    public Map<String, Object> getVideoInfo(InputStream videoStream) throws IOException, InterruptedException {
        return Map.of();
    }

    @Override
    public String uploadFile(InputStream fileStream, String fileName, String contentType, String ownerId) {
        return "";
    }

}
