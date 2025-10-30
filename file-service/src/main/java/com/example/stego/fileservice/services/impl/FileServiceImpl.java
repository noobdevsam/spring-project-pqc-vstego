package com.example.stego.fileservice.services.impl;

import com.example.stego.fileservice.model.FileMetadata;
import com.example.stego.fileservice.services.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

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
