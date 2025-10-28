package com.example.stego.orchestrationservice.controllers;
// This controller is intentionally left blank for now.
// The `JobService` needs to be expanded with `getFilesForUser` and `deleteFile`
// methods, which will then call the file-service.
// We can implement these after the file-service is built.

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {
    // TODO: Implement GET /api/v1/files (List files)
    // TODO: Implement DELETE /api/v1/files/{fileId} (Delete file)
}