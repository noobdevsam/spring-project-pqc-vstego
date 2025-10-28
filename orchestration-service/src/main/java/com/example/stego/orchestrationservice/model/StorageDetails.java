package com.example.stego.orchestrationservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageDetails {
    private String inputFileGridFsId;
    private String secretFileGridFsId; // Only for ENCODE jobs
    private String outputFileGridFsId;// For COMPLETED jobs
}
