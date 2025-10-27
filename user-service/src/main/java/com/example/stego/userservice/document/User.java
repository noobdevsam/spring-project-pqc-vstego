package com.example.stego.userservice.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String githubId; // As per SRS 3.3

    private String username; // As per SRS 3.3
    private String avatarUrl; // As per SRS 3.3

    private String pqcPublicKey;
    private String pqcPrivateKey;

    @CreatedDate
    private Instant createdAt; // As per SRS 3.3
    private Instant keyLastUpdatedAt;

    public User() {
    }

    public User(String githubId, String username, String avatarUrl) {
        this.githubId = githubId;
        this.username = username;
        this.avatarUrl = avatarUrl;
    }

}
