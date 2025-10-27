package com.example.stego.userservice.controller;

import com.example.stego.userservice.model.UserDTO;
import com.example.stego.userservice.services.AppUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final AppUserService appUserService;

    public UserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(
            @AuthenticationPrincipal OAuth2AuthenticationToken authenticationToken
    ) {
        if (authenticationToken == null) {
            // This should not happen as the endpoint is secured, but just in case
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var userDTO = appUserService.findOrCreateUser(authenticationToken);
        return ResponseEntity.ok(userDTO);
    }

    public ResponseEntity<List<UserDTO>> getAllUsers() {
        var users = appUserService.findAllUsers();
        return ResponseEntity.ok(users);
    }

}
