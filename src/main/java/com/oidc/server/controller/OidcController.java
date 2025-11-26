package com.oidc.server.controller;

import com.oidc.server.entity.OAuthClient;
import com.oidc.server.service.AuthService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Controller
@RequestMapping("/oidc")
@AllArgsConstructor
@Slf4j
public class OidcController {

    private final AuthService authService;

    @GetMapping("/authorize")
    public String authorize(@RequestParam(name = "client_id") String clientId,
                            @RequestParam(name = "redirect_uri") String redirectUri,
                            @RequestParam(name = "response_type") String responseType,
                            @RequestParam(defaultValue = "openid profile email") String scope,
                            @RequestParam String state,
                            HttpSession session,
                            Model model) {

        // 验证客户端
        Optional<OAuthClient> clientOpt = authService.findClientByClientId(clientId);
        if (clientOpt.isEmpty()) {
            model.addAttribute("error", "Invalid client ID");
            return "error";
        }

        OAuthClient client = clientOpt.get();
        if (!client.getRedirectUris().contains(redirectUri)) {
            model.addAttribute("error", "Invalid redirect URI");
            return "error";
        }

        if (!"code".equals(responseType)) {
            model.addAttribute("error", "Unsupported response type");
            return "error";
        }

        // 检查用户是否已登录
        Object userId = session.getAttribute("userId");
        if (userId == null) {
            // 未登录，重定向到登录页面，并保存 OIDC 参数
            log.info("User not logged in, redirecting to login");
            model.addAttribute("clientId", clientId);
            model.addAttribute("redirectUri", redirectUri);
            model.addAttribute("scope", scope);
            model.addAttribute("state", state);
            model.addAttribute("responseType", responseType);
            return "redirect:/login?clientId=" + clientId + "&redirectUri=" + redirectUri + "&scope=" + scope + "&state=" + state + "&responseType=" + responseType;
        }

        // 用户已登录，生成授权码并重定向到客户端
        String code = authService.generateAuthorizationCode(clientId, (Long) userId, redirectUri, scope);
        log.info("✓ Authorization code generated: {} for user: {}", code, session.getAttribute("username"));
        return "redirect:" + redirectUri + "?code=" + code + "&state=" + state;
    }

}
