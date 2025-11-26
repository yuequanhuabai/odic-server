package com.oidc.server.service;

import com.oidc.server.dto.TokenResponse;
import com.oidc.server.entity.AuthorizationCode;
import com.oidc.server.entity.OAuthClient;
import com.oidc.server.entity.User;
import com.oidc.server.repository.AuthorizationCodeRepository;
import com.oidc.server.repository.OAuthClientRepository;
import com.oidc.server.repository.UserRepository;
import com.oidc.server.util.AuthorizationCodeGenerator;
import com.oidc.server.util.JwtTokenProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final OAuthClientRepository oauthClientRepository;
    private final AuthorizationCodeRepository authorizationCodeRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthorizationCodeGenerator authorizationCodeGenerator;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User registerUser(String username, String password, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        return userRepository.save(user);
    }

    public boolean authenticateUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }
        return passwordEncoder.matches(password, userOpt.get().getPassword());
    }

    public String generateAuthorizationCode(String clientId, Long userId, String redirectUri, String scope) {
        String code = authorizationCodeGenerator.generateCode();
        AuthorizationCode authCode = new AuthorizationCode();
        authCode.setCode(code);
        authCode.setClientId(clientId);
        authCode.setUserId(userId);
        authCode.setRedirectUri(redirectUri);
        authCode.setScope(scope);
        authCode.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        authorizationCodeRepository.save(authCode);
        return code;
    }

    public Optional<AuthorizationCode> validateAuthorizationCode(String code) {
        Optional<AuthorizationCode> codeOpt = authorizationCodeRepository.findByCode(code);
        if (codeOpt.isEmpty()) {
            return Optional.empty();
        }
        AuthorizationCode authCode = codeOpt.get();
        if (authCode.isExpired()) {
            authorizationCodeRepository.delete(authCode);
            return Optional.empty();
        }
        return codeOpt;
    }

    public Optional<OAuthClient> findClientByClientId(String clientId) {
        return oauthClientRepository.findByClientId(clientId);
    }

    public boolean validateClientSecret(String clientId, String clientSecret) {
        Optional<OAuthClient> clientOpt = oauthClientRepository.findByClientId(clientId);
        if (clientOpt.isEmpty()) {
            return false;
        }
        return clientOpt.get().getClientSecret().equals(clientSecret);
    }

    public TokenResponse exchangeCodeForToken(String code, String clientId, String clientSecret) {
        if (!validateClientSecret(clientId, clientSecret)) {
            return null;
        }

        Optional<AuthorizationCode> codeOpt = validateAuthorizationCode(code);
        if (codeOpt.isEmpty()) {
            return null;
        }

        AuthorizationCode authCode = codeOpt.get();
        if (!authCode.getClientId().equals(clientId)) {
            return null;
        }

        User user = userRepository.findById(authCode.getUserId()).orElse(null);
        if (user == null) {
            return null;
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), clientId);
        String idToken = jwtTokenProvider.generateIdToken(user.getId(), user.getUsername(), user.getEmail(), clientId);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        authorizationCodeRepository.delete(authCode);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .idToken(idToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .build();
    }
}
