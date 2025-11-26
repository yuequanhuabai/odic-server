package com.oidc.server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "authorization_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationCode {

    @Id
    private String code;

    @Column(nullable = false, length = 100)
    private String clientId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 255)
    private String redirectUri;

    @Column(length = 255)
    private String scope;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
