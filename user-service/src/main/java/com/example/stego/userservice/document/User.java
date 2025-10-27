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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGithubId() {
        return githubId;
    }

    public void setGithubId(String githubId) {
        this.githubId = githubId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getPqcPublicKey() {
        return pqcPublicKey;
    }

    public void setPqcPublicKey(String pqcPublicKey) {
        this.pqcPublicKey = pqcPublicKey;
    }

    public String getPqcPrivateKey() {
        return pqcPrivateKey;
    }

    public void setPqcPrivateKey(String pqcPrivateKey) {
        this.pqcPrivateKey = pqcPrivateKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getKeyLastUpdatedAt() {
        return keyLastUpdatedAt;
    }

    public void setKeyLastUpdatedAt(Instant keyLastUpdatedAt) {
        this.keyLastUpdatedAt = keyLastUpdatedAt;
    }
}
