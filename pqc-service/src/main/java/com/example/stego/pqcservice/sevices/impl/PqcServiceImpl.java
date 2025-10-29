package com.example.stego.pqcservice.sevices.impl;

import com.example.stego.pqcservice.document.PublicKey;
import com.example.stego.pqcservice.model.KeyPairDTO;
import com.example.stego.pqcservice.model.PublicKeyDTO;
import com.example.stego.pqcservice.repo.PublicKeyRepo;
import com.example.stego.pqcservice.sevices.PqcService;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec;
import org.bouncycastle.pqc.jcajce.spec.KyberParameterSpec;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.*;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PqcServiceImpl implements PqcService {

    static {
        // Add the general Bouncy Castle provider for PQC algorithms
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
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
}
