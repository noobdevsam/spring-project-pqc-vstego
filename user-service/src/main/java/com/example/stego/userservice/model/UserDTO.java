package com.example.stego.userservice.model;

import com.example.stego.userservice.document.User;

public record UserDTO(
        String userId,
        String username,
        String avatarUrl,
        String pqcPublicKey
) {
    public static UserDTO fromUserToUserDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getAvatarUrl(),
                user.getPqcPublicKey()
        );
    }
}
