package com.oidc.server.controller;

import com.oidc.server.dto.TokenResponse;
import com.oidc.server.service.AuthService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/oidc")
@AllArgsConstructor
@Slf4j
public class OidcTokenController {

    private final AuthService authService;

    @PostMapping("/token")
    public ResponseEntity<?> token(@RequestParam("grant_type") String grantType,
                                   @RequestParam(required = false) String code,
                                   @RequestParam("redirect_uri") String redirectUri,
                                   @RequestParam("client_id") String clientId,
                                   @RequestParam("client_secret") String clientSecret,
                                   @RequestParam(value = "refresh_token", required = false) String refreshToken) {

        if (!"authorization_code".equals(grantType)) {
            log.warn("Unsupported grant type: {}", grantType);
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"unsupported_grant_type\"}");
        }

        if (code == null || code.isEmpty()) {
            log.warn("Missing authorization code");
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"invalid_request\"}");
        }

        log.info("Token exchange request for code: {}, client: {}", code, clientId);
        TokenResponse tokenResponse = authService.exchangeCodeForToken(code, clientId, clientSecret);

        if (tokenResponse == null) {
            log.warn("Token exchange failed for code: {}", code);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"invalid_grant\"}");
        }

        log.info("✓ Token issued successfully for client: {}", clientId);
        return ResponseEntity.ok(tokenResponse);
    }

    @GetMapping("/userinfo")
    public ResponseEntity<?> userinfo(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"missing_token\"}");
        }

        String token = authHeader.substring(7);
        // Token validation would be done here with JwtTokenProvider
        return ResponseEntity.ok("{\"error\": \"invalid_token\"}");
    }
}
