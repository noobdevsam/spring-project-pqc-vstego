package com.example.stego.pqcservice.sevices;

import com.example.stego.pqcservice.document.PublicKey;
import com.example.stego.pqcservice.model.KeyPairDTO;
import com.example.stego.pqcservice.model.PublicKeyDTO;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Optional;

public interface PqcService {

    KeyPairDTO generateKeys() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException;

    PublicKey setPublicKey(String userId, PublicKeyDTO publicKeyDTO);

    Optional<PublicKey> getPublicKeyForUser(String userId);

}
