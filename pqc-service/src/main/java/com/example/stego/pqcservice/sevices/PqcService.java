package com.example.stego.pqcservice.sevices;

import com.example.stego.pqcservice.document.PublicKey;
import com.example.stego.pqcservice.model.KeyPairDTO;
import com.example.stego.pqcservice.model.PublicKeyDTO;

import java.util.Optional;

public interface PqcService {

    KeyPairDTO generateKeys();

    PublicKey setPublicKey(String userId, PublicKeyDTO publicKeyDTO);

    Optional<PublicKey> getPublicKeyForUser(String userId);

}
