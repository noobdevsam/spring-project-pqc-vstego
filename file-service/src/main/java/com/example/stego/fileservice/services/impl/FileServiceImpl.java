package com.example.stego.fileservice.services.impl;

import com.example.stego.fileservice.model.FileMetadata;
import com.example.stego.fileservice.services.FileService;
import com.mongodb.client.gridfs.GridFSBucket;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private GridFsTemplate gridFsTemplate;
    private GridFSBucket gridFSBucket; // For streaming large files

    // Helper to get authenticated user ID (assuming or similar from API Gateway)
    private String getAuthenticatedUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {

        }
    }

    @Override
    public String storeFile(InputStream inputStream, String filename, String contentType, String ownerUserId) throws IOException {
        return "";
    }

    @Override
    public GridFsResource retrieveFile(String fileId) {
        return null;
    }

    @Override
    public List<FileMetadata> listFiles(String ownerUserId) {
        return List.of();
    }

    @Override
    public void deleteFile(String fileId, String ownerUserId) {

    }

}
