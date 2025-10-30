package com.example.stego.fileservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {
    private String id;
    private String filename;
    private String contentType;
    private Long length;
    private LocalDateTime uploadDate;
    private String ownerUserId; // As per SRS: fs.files will include custom metadata like ownerUserId
}