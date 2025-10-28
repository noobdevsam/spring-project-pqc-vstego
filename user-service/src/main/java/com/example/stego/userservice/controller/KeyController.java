package com.example.stego.userservice.controller;

import com.example.stego.userservice.services.AppUserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/keys")
public class KeyController {

    private final AppUserService appUserService;

    public KeyController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

}
