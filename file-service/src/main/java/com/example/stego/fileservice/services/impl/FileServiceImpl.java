package com.example.stego.fileservice.services.impl;

import com.example.stego.fileservice.model.FileMetadata;
import com.example.stego.fileservice.services.FileService;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
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
            // In a real scenario, this would extract the userId from the JWT or OAuth2 token
            // For now, we'll use the principal name as placeholder or a dummy ID
            return authentication.getName(); // Assuming principal name is the userId
        }
        // Fallback for testing or if security context is not fully set up yet
        // In a production environment, this should throw exception or return null if not authenticated
        return "anonymous"; // Placeholder
    }

    @Override
    public String storeFile(
            InputStream inputStream,
            String filename,
            String contentType,
            String ownerUserId
    ) throws IOException {
        var metadata = new Document();

        metadata.put("ownerUserId", ownerUserId); // Store ownerUserId as custom metadata
        metadata.put("uploadDate", LocalDateTime.now());

        var options = new GridFSUploadOptions()
                .metadata(metadata)
                .chunkSizeBytes(255 * 1024); // Default chunk size

        var fileId = gridFsTemplate.store(inputStream, filename, contentType, options);
        return fileId.toString();
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
