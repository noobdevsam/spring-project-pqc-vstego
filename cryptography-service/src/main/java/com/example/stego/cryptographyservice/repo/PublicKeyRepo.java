package com.example.stego.cryptographyservice.repo;

import com.example.stego.pqcservice.document.PublicKey;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PublicKeyRepo extends MongoRepository<PublicKey, String> {

    Optional<PublicKey> findByUserIdAndIsActiveTrue(String userId);

}
