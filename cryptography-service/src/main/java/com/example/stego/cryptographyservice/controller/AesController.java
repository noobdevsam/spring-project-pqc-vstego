package com.example.stego.cryptographyservice.controller;

import com.example.stego.cryptographyservice.sevices.CryptographyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/crypto/aes")
@RequiredArgsConstructor
public class AesController {

    private CryptographyService cryptographyService;


}
