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

import javax.crypto.SecretKey;
import java.io.InputStream;
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
    public KeyPairDTO generateKeys() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        // Generate a key pair using a PQC algorithm
        var kemGenerator = KeyPairGenerator.getInstance("Kyber", BouncyCastleProvider.PROVIDER_NAME);
        kemGenerator.initialize(KyberParameterSpec.kyber1024);
        var kemKeyPair = kemGenerator.generateKeyPair();

        // Generate Dilithium key pair
        var dsaGenerator = KeyPairGenerator.getInstance("Dilithium", BouncyCastleProvider.PROVIDER_NAME);
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
        return null;
    }

    @Override
    public InputStream encryptData(InputStream data, SecretKey secretKey) {
        return null;
    }

    @Override
    public InputStream decryptData(InputStream encryptedData, SecretKey secretKey) {
        return null;
    }

}
