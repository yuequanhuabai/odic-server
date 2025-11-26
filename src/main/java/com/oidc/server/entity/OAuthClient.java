package com.oidc.server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "oauth2_clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuthClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String clientId;

    @Column(nullable = false)
    private String clientSecret;

    @Column(nullable = false, length = 500)
    private String redirectUris;

    @Column(length = 100)
    private String clientName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
