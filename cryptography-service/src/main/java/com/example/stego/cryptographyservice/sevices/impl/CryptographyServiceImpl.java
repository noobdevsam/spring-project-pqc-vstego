package com.example.stego.cryptographyservice.sevices.impl;

import com.example.stego.cryptographyservice.document.PublicKey;
import com.example.stego.cryptographyservice.model.KeyPairDTO;
import com.example.stego.cryptographyservice.model.PublicKeyDTO;
import com.example.stego.cryptographyservice.repo.PublicKeyRepo;
import com.example.stego.cryptographyservice.sevices.CryptographyService;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec;
import org.bouncycastle.pqc.jcajce.spec.KyberParameterSpec;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.security.*;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CryptographyServiceImpl implements CryptographyService {

    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final String AES_GCM_CIPHER = "AES/GCM/NoPadding";

    static {
        // Add the Bouncy Castle provider for traditional algorithms
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        // Add the Bouncy Castle PQC provider for post-quantum algorithms
        if (Security.getProvider(BouncyCastlePQCProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastlePQCProvider());
        }
    }

    private final PublicKeyRepo publicKeyRepo;

    @Override
    public KeyPairDTO generatePQCKeys() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        // Generate a key pair using a PQC algorithm
        var kemGenerator = KeyPairGenerator.getInstance("Kyber", BouncyCastlePQCProvider.PROVIDER_NAME);
        kemGenerator.initialize(KyberParameterSpec.kyber1024);
        var kemKeyPair = kemGenerator.generateKeyPair();

        // Generate Dilithium key pair
        var dsaGenerator = KeyPairGenerator.getInstance("Dilithium", BouncyCastlePQCProvider.PROVIDER_NAME);
        dsaGenerator.initialize(DilithiumParameterSpec.dilithium5);
        var dsaKeyPair = dsaGenerator.generateKeyPair();

        return new KeyPairDTO(
                Base64.getEncoder().encodeToString(kemKeyPair.getPublic().getEncoded()),
                Base64.getEncoder().encodeToString(kemKeyPair.getPrivate().getEncoded()),
                Base64.getEncoder().encodeToString(dsaKeyPair.getPublic().getEncoded()),
                Base64.getEncoder().encodeToString(dsaKeyPair.getPrivate().getEncoded())
        );

    }

    @Override
    @Transactional
    public PublicKey setPublicKey(String userId, PublicKeyDTO publicKeyDTO) {

        // Deactivate existing active keys for this user
        publicKeyRepo.findByUserIdAndIsActiveTrue(userId).ifPresent(
                oldKey -> {
                    oldKey.setActive(false);
                    publicKeyRepo.save(oldKey);
                }
        );

        // Create and save the new active public key
        var newKey = PublicKey.builder()
                .userId(userId)
                .kemPublicKey(publicKeyDTO.getKemPublicKey())
                .dsaPublicKey(publicKeyDTO.getDsaPublicKey())
                .build();
        return publicKeyRepo.save(newKey);

    }

    @Override
    public Optional<PublicKey> getPublicKeyForUser(String userId) {
        return publicKeyRepo.findByUserIdAndIsActiveTrue(userId);
    }

    @Override
    public SecretKey generateAESKey() {

        try {
            var keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(AES_KEY_SIZE, new SecureRandom());
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not generate AES key", e);
        }

    }

    @Override
    public InputStream encryptData(InputStream data, SecretKey secretKey) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv); // Generate a random IV
            var gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv); // GCM tag length and IV

            var cipher = Cipher.getInstance(AES_GCM_CIPHER, BouncyCastleProvider.PROVIDER_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);

            var cipherInputStream = new CipherInputStream(data, cipher);
            var ivInputStream = new ByteArrayInputStream(iv);

            return new SequenceInputStream(ivInputStream, cipherInputStream);
        } catch (Exception e) {
            throw new RuntimeException("Error during AES encryption", e);
        }
    }

    @Override
    public InputStream decryptData(InputStream encryptedData, SecretKey secretKey) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            var bytesRead = encryptedData.readNBytes(iv, 0, GCM_IV_LENGTH);
            if (bytesRead < GCM_IV_LENGTH) {
                throw new IllegalArgumentException("Invalid encrypted stream: missing IV.");
            }

            var gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            var cipher = Cipher.getInstance(AES_GCM_CIPHER, BouncyCastleProvider.PROVIDER_NAME);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);

            return new CipherInputStream(encryptedData, cipher);
        } catch (Exception e) {
            throw new RuntimeException("Error during AES decryption", e);
        }
    }

}
