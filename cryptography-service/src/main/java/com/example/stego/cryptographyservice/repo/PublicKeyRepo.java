package com.example.stego.cryptographyservice.repo;

import com.example.stego.cryptographyservice.document.PublicKey;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PublicKeyRepo extends MongoRepository<PublicKey, String> {

    Optional<PublicKey> findByUserIdAndIsActiveTrue(String userId);

}
