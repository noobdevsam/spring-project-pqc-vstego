package com.example.stego.fileservice.controller;

import com.example.stego.fileservice.model.FileMetadata;
import com.example.stego.fileservice.services.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {

        try {
            var ownerUserId = fileService.getAuthenticatedUserId();
            // Get owner from security context
            var fileId = fileService.storeFile(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    ownerUserId
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(fileId);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store the file:" + e.getMessage(), e);
        }
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String fileId) {
        var gridFsResource = fileService.retrieveFile(fileId);

        if (gridFsResource == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(gridFsResource.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename =\"" + gridFsResource.getFilename() + "\"")
                    .body(new InputStreamResource(gridFsResource.getInputStream()));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error streaming file " + e.getMessage(), e);
        }
    }

    @GetMapping
    public ResponseEntity<List<FileMetadata>> listUserFiles() {
        var ownerUserId = fileService.getAuthenticatedUserId();
        var files = fileService.listFiles(ownerUserId);
        return ResponseEntity.ok(files);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable String fileId) {
        var ownerUserId = fileService.getAuthenticatedUserId();
        fileService.deleteFile(fileId, ownerUserId);
        return ResponseEntity.noContent().build();
    }

}
