package com.example.stego.fileservice.services.impl;

import com.example.stego.fileservice.model.FileMetadata;
import com.example.stego.fileservice.services.FileService;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FileServiceImpl implements FileService {

    private final GridFsTemplate gridFsTemplate;
    private final GridFSBucket gridFSBucket;

    public FileServiceImpl(GridFsTemplate gridFsTemplate, GridFSBucket gridFSBucket) {
        this.gridFsTemplate = gridFsTemplate;
        this.gridFSBucket = gridFSBucket;
    }

    @Override
    public String getAuthenticatedUserId() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(authentication -> {
                    if (authentication.getPrincipal() instanceof Jwt) {
                        Jwt jwt = (Jwt) authentication.getPrincipal();
                        // Assuming the 'sub' claim in the JWT holds the user ID
                        return jwt.getSubject();
                    }
                    // Fallback for other authentication types or testing
                    return authentication.getName();
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated"));
    }

    @Override
    public String storeFile(
            InputStream inputStream,
            String filename,
            String contentType,
            String ownerUserId
    ) throws IOException {
        var metadata = new Document();
        metadata.put("ownerUserId", ownerUserId);
        metadata.put("uploadDate", LocalDateTime.now());
        metadata.put("_contentType", contentType);

        var options = new GridFSUploadOptions()
                .chunkSizeBytes(1024 * 1024) // 1MB chunk size
                .metadata(metadata);

        ObjectId fileId = gridFSBucket.uploadFromStream(filename, inputStream, options);
        return fileId.toHexString();
    }

    @Override
    public GridFsResource retrieveFile(String fileId) {
        var ownerUserId = getAuthenticatedUserId();
        var gridFSFile = gridFsTemplate.findOne(
                new Query(
                        Criteria.where("_id").is(fileId)
                )
        );

        if (gridFSFile == null) {
            return null;
        }

        assert gridFSFile.getMetadata() != null;
        // Ensure only the owner can retrieve the file
        if (!ownerUserId.equals(gridFSFile.getMetadata().getString("ownerUserId"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to retrieve this file");
        }

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
