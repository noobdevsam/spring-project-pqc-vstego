package com.example.stego.orchestrationservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class StorageDetails {
    private String inputFileGridFsId;
    private String secretFileGridFsId; // Only for ENCODE jobs
    private String outputFileGridFsId;// For COMPLETED jobs
}
