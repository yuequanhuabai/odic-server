package com.oidc.server.config;

import com.oidc.server.entity.OAuthClient;
import com.oidc.server.entity.User;
import com.oidc.server.repository.OAuthClientRepository;
import com.oidc.server.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@AllArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final OAuthClientRepository oauthClientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeUsers();
        initializeOAuthClients();
    }

    private void initializeUsers() {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@example.com");
            userRepository.save(admin);
            log.info("✓ Created admin user");

            User testUser = new User();
            testUser.setUsername("testuser");
            testUser.setPassword(passwordEncoder.encode("test123"));
            testUser.setEmail("testuser@example.com");
            userRepository.save(testUser);
            log.info("✓ Created test user");
        }
    }

    private void initializeOAuthClients() {
        if (oauthClientRepository.count() == 0) {
            OAuthClient client = new OAuthClient();
            client.setClientId("my-app");
            client.setClientSecret("secret123");
            client.setRedirectUris("http://localhost:5173/callback,http://localhost:3000/callback");
            client.setClientName("My OIDC Client");
            oauthClientRepository.save(client);
            log.info("✓ Created OAuth2 client");
        }
    }
}
