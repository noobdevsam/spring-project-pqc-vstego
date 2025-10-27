package com.example.stego.userservice.services.impl;

import com.example.stego.userservice.document.User;
import com.example.stego.userservice.model.UserDTO;
import com.example.stego.userservice.repo.UserRepo;
import com.example.stego.userservice.services.AppUserService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
        var attributes = principal.getAttributes();
        var githubId = String.valueOf(attributes.get("id"));
        var existingUser = userRepo.findByGithubId(githubId);

        if (existingUser.isPresent()) {
            return UserDTO.fromUserToUserDTO(existingUser.get());
        } else {
            // Create new user
            var username = (String) attributes.get("login");
            var avatarUrl = (String) attributes.get("avatar_url");

            var newUser = new User(githubId, username, avatarUrl);
            userRepo.save(newUser);

            return UserDTO.fromUserToUserDTO(newUser);
        }
    }

    // Retrieves all registered users
    @Override
    public List<UserDTO> findAllUsers() {
        return userRepo.findAll()
                .stream()
                .map(UserDTO::fromUserToUserDTO)
                .collect(Collectors.toList());
    }
}
