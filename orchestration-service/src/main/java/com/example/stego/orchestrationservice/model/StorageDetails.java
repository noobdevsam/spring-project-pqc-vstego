package com.example.stego.orchestrationservice.model;

import java.util.Objects;

public class StorageDetails {
    private String inputFileGridFsId;
    private String secretFileGridFsId; // Only for ENCODE jobs
    private String outputFileGridFsId;// For COMPLETED jobs

    public String getSecretFileGridFsId() {
        return secretFileGridFsId;
    }

    public void setSecretFileGridFsId(String secretFileGridFsId) {
        this.secretFileGridFsId = secretFileGridFsId;
    }

    public String getInputFileGridFsId() {
        return inputFileGridFsId;
    }

    public void setInputFileGridFsId(String inputFileGridFsId) {
        this.inputFileGridFsId = inputFileGridFsId;
    }

    public String getOutputFileGridFsId() {
        return outputFileGridFsId;
    }

    public void setOutputFileGridFsId(String outputFileGridFsId) {
        this.outputFileGridFsId = outputFileGridFsId;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof StorageDetails that)) return false;

        return Objects.equals(inputFileGridFsId, that.inputFileGridFsId) && Objects.equals(secretFileGridFsId, that.secretFileGridFsId) && Objects.equals(outputFileGridFsId, that.outputFileGridFsId);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(inputFileGridFsId);
        result = 31 * result + Objects.hashCode(secretFileGridFsId);
        result = 31 * result + Objects.hashCode(outputFileGridFsId);
        return result;
    }
}
