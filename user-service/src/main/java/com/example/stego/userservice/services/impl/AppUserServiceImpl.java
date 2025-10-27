package com.example.stego.userservice.services.impl;

import com.example.stego.userservice.model.UserDTO;
import com.example.stego.userservice.repo.UserRepo;
import com.example.stego.userservice.services.AppUserService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppUserServiceImpl implements AppUserService {

    private final UserRepo userRepo;

    public AppUserServiceImpl(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    // Finds an existing user or creates a new one based on OAuth2 authentication token
    // This is a core logic in the SRS file
    @Override
    public UserDTO findOrCreateUser(OAuth2AuthenticationToken authenticationToken) {
        var principal = authenticationToken.getPrincipal();
        var attributes = pirncipal.getAttributes();
        var githubId = String.valueOf(attributes.get("id"));
        var existingUser = userRepo.findByGithubId(githubId);


        return null;
    }

    @Override
    public List<UserDTO> findAllUsers() {
        return List.of();
    }
}
