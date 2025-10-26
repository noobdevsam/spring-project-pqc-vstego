package com.example.stego.userservice.repo;

import com.example.stego.userservice.document.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepo extends MongoRepository<User, String> {

    Optional<User> findByGithubId(String githubId);

}
