package com.example.stego.userservice.controller;

import com.example.stego.userservice.model.KeyUpdateRequest;
import com.example.stego.userservice.model.UserDTO;
import com.example.stego.userservice.services.AppUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
