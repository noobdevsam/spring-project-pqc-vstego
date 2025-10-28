package com.example.stego.orchestrationservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class StorageDetails {
    private String inputFileGridFsId;
    private String secretFileGridFsId; // Only for ENCODE jobs
    private String outputFileGridFsId;// For COMPLETED jobs
}
