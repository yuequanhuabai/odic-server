package com.oidc.server.controller;

import com.oidc.server.entity.User;
import com.oidc.server.service.AuthService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
@AllArgsConstructor
@Slf4j
public class LoginController {

    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String username,
                              @RequestParam String password,
                              @RequestParam(required = false) String clientId,
                              @RequestParam(required = false) String redirectUri,
                              @RequestParam(required = false) String state,
                              @RequestParam(required = false) String scope,
                              HttpSession session) {

        if (!authService.authenticateUser(username, password)) {
            return "redirect:/login?error=true";
        }

        User user = authService.findUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login?error=true";
        }

        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());

        if (clientId != null && redirectUri != null) {
            return String.format("redirect:/oidc/authorize?client_id=%s&redirect_uri=%s&response_type=code&scope=%s&state=%s",
                    clientId, redirectUri, scope != null ? scope : "openid profile email", state);
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Object userId = session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("username", session.getAttribute("username"));
        return "dashboard";
    }
}
