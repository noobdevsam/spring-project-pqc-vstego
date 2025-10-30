package com.example.stego.fileservice.services.impl;

import com.example.stego.fileservice.model.FileMetadata;
import com.example.stego.fileservice.services.FileService;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import org.bson.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileServiceImpl implements FileService {

    private final GridFsTemplate gridFsTemplate;
//    private GridFSBucket gridFSBucket; // For streaming large files

    public FileServiceImpl(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
//        this.gridFSBucket = gridFSBucket;
    }

    //  to get authenticated user ID (assuming or similar from API Gateway)
    @Override
    public String getAuthenticatedUserId() {
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
        var gridFSFile = gridFsTemplate.findOne(
                new Query(
                        Criteria.where("_id").is(fileId)
                )
        );

        if (gridFSFile == null) {
            return null;
        }

        // Check ownership if needed, but for internal API, we might trust the caller
        return new GridFsResource(gridFSFile);
    }

    @Override
    public List<FileMetadata> listFiles(String ownerUserId) {
        var files = new ArrayList<FileMetadata>();

        gridFsTemplate.find(
                new Query(
                        Criteria.where("metadata.ownerUserId").is(ownerUserId)
                )
        ).forEach(gridFSFile -> {
                    var metadata = new FileMetadata();
                    assert gridFSFile.getMetadata() != null;

                    metadata.setId(gridFSFile.getId().toString());
                    metadata.setFilename(gridFSFile.getFilename());
                    metadata.setContentType(gridFSFile.getMetadata().getString("_contentType"));
                    metadata.setLength(gridFSFile.getLength());
                    metadata.setUploadDate(gridFSFile.getUploadDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                    metadata.setOwnerUserId(gridFSFile.getMetadata().getString("ownerUserId"));

                    files.add(metadata);
                }
        );

        return files;
    }

    @Override
    public void deleteFile(String fileId, String ownerUserId) {
        var gridFSFile = gridFsTemplate.findOne(
                new Query(
                        Criteria.where("_id").is(fileId)
                )
        );

        if (gridFSFile == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found.");
        }

        assert gridFSFile.getMetadata() != null;

        // Ensure only the owner can delete the file
        if (!ownerUserId.equals(gridFSFile.getMetadata().getString("ownerUserId"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to delete this file");
        }

        gridFsTemplate.delete(new Query(Criteria.where("_id").is(fileId)));
    }

}
