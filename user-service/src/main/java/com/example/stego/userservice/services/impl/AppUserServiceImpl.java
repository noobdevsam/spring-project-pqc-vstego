package com.example.stego.userservice.services.impl;

import com.example.stego.userservice.model.UserDTO;
import com.example.stego.userservice.services.AppUserService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppUserServiceImpl implements AppUserService {

    @Override
    public UserDTO findOrCreateUser(OAuth2AuthenticationToken authenticationToken) {
        return null;
    }

    @Override
    public List<UserDTO> findAllUsers() {
        return List.of();
    }
}
