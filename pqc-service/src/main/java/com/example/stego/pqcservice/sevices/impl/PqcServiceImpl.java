package com.example.stego.pqcservice.sevices.impl;

import com.example.stego.pqcservice.document.PublicKey;
import com.example.stego.pqcservice.model.KeyPairDTO;
import com.example.stego.pqcservice.model.PublicKeyDTO;
import com.example.stego.pqcservice.sevices.PqcService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PqcServiceImpl implements PqcService {

    @Override
    public KeyPairDTO generateKeys() {
        return null;
    }

    @Override
    public PublicKey setPublicKey(String userId, PublicKeyDTO publicKeyDTO) {
        return null;
    }

    @Override
    public Optional<PublicKey> getPublicKeyForUser(String userId) {
        return Optional.empty();
    }
}
