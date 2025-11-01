package com.example.stego.cryptographyservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KeyPairDTO {

    private String kemPublicKey;
    private String kemPrivateKey;
    private String dsaPublicKey;
    private String dsaPrivateKey;

}
