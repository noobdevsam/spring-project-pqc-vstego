package com.example.stego.videoprocessingservice.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public interface SteganographyService {

    void embedPayload(
            InputStream carrierVideoStream,
            byte[] payloadData,
            OutputStream stegoVideoOutputStream
    ) throws IOException, InterruptedException;

    byte[] extractPayload(
            InputStream stegoVideoInputStream
    ) throws IOException, InterruptedException;

    Map<String, Object> getVideoInfo(
            InputStream videoStream
    ) throws IOException, InterruptedException;

    String uploadFile(
            InputStream fileStream,
            String fileName,
            String contentType,
            String ownerId
    );

}
