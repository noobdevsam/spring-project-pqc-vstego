package com.example.stego.orchestrationservice.model;

public record StorageDetails(
        String inputFileGridFsId,
        String secretFileGridFsId, // Only for ENCODE jobs
        String outputFileGridFsId // For COMPLETED jobs
) {
}
