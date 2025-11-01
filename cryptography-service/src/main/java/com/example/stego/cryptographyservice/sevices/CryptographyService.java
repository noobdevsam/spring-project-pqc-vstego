package com.example.stego.cryptographyservice.sevices;

import com.example.stego.cryptographyservice.document.PublicKey;
import com.example.stego.cryptographyservice.model.KeyPairDTO;
import com.example.stego.cryptographyservice.model.PublicKeyDTO;

import javax.crypto.SecretKey;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Optional;

public interface CryptographyService {

    KeyPairDTO generateKeys() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException;

    PublicKey setPublicKey(String userId, PublicKeyDTO publicKeyDTO);

    Optional<PublicKey> getPublicKeyForUser(String userId);

    SecretKey generateAESKey();

    InputStream encryptData(InputStream data, SecretKey secretKey);

    InputStream decryptData(InputStream encryptedData, SecretKey secretKey);

}
