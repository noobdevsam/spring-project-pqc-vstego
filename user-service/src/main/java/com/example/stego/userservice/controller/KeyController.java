package com.example.stego.userservice.controller;

import com.example.stego.userservice.model.KeyUpdateRequest;
import com.example.stego.userservice.model.UserDTO;
import com.example.stego.userservice.services.AppUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/keys")
public class KeyController {

    private final AppUserService appUserService;

    public KeyController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @PostMapping
    public ResponseEntity<UserDTO> updateKeys(
            @AuthenticationPrincipal OAuth2AuthenticationToken authenticationToken,
            @RequestBody KeyUpdateRequest keyUpdateRequest
    ) {
        var updatedUser = appUserService.updatePqcKeys(authenticationToken, keyUpdateRequest);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/private")
    public ResponseEntity<String> getPrivateKey(
            @AuthenticationPrincipal OAuth2AuthenticationToken authenticationToken
    ) {
        var currentUser = appUserService.findOrCreateUser(authenticationToken);

        if (currentUser.pqcPublicKey() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(currentUser.pqcPublicKey());
    }

}
