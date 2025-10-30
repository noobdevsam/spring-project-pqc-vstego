package com.example.stego.fileservice.services;

import com.example.stego.fileservice.model.FileMetadata;
import org.springframework.data.mongodb.gridfs.GridFsResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface FileService {

    String getAuthenticatedUserId();

    String storeFile(
            InputStream inputStream,
            String filename,
            String contentType,
            String ownerUserId
    ) throws IOException;

    GridFsResource retrieveFile(String fileId);

    List<FileMetadata> listFiles(String ownerUserId);

    void deleteFile(String fileId, String ownerUserId);

}
