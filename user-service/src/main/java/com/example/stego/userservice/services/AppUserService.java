package com.example.stego.userservice.services;

import com.example.stego.userservice.model.UserDTO;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.util.List;

public interface AppUserService {

    UserDTO findOrCreateUser(OAuth2AuthenticationToken authenticationToken);

    List<UserDTO> findAllUsers();

}
