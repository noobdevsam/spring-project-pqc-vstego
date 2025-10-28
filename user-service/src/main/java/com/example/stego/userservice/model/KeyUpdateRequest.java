package com.example.stego.userservice.model;

import jakarta.validation.constraints.NotBlank;

public record KeyUpdateRequest(
        @NotBlank
        String publicKey,

        @NotBlank
        String privateKey
) {
}
