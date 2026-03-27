package com.portly.controller;

import com.portly.domain.entity.Usuario;
import com.portly.dto.AuthResponse;
import com.portly.dto.RegisterRequest;
import com.portly.service.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.portly.dto.LoginRequest;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LinkedInOAuthService linkedInService;
    private final GitHubOAuthService   gitHubService;
    private final GoogleOAuthService   googleService;
    private final UsuarioService       usuarioService;
    private final AuthService          authService;
    private final JwtService           jwtService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // ─── Registro con email y contraseña ─────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid  @RequestBody LoginRequest body) {
        return entity;
    }
    

    // ─── LinkedIn ────────────────────────────────────────────────────

    /** Redirige al usuario a la pantalla de autorización de LinkedIn. */
    @GetMapping("/linkedin")
    public void linkedInLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect(linkedInService.getAuthorizationUrl());
    }

    /** Recibe el code de LinkedIn, lo procesa y redirige al frontend con el JWT. */
    @GetMapping("/linkedin/callback")
    public void linkedInCallback(@RequestParam("code") String code,
                                 @RequestParam(value = "error", required = false) String error,
                                 HttpServletResponse response) throws IOException {
        if (error != null) {
            response.sendRedirect(frontendUrl + "/auth/error?reason=" + error);
            return;
        }
        try {
            String accessToken     = linkedInService.exchangeCodeForToken(code);
            OAuthUserInfo userInfo = linkedInService.fetchUserInfo(accessToken);
            Usuario usuario        = usuarioService.findOrCreate(userInfo);
            String jwt             = jwtService.generateToken(
                    usuario.getUsuarioId(), usuario.getEmail(), usuario.getRol());

            response.sendRedirect(frontendUrl + "/auth/callback?token=" + jwt);
        } catch (Exception e) {
            response.sendRedirect(frontendUrl + "/auth/error?reason=linkedin_error");
        }
    }

    // ─── GitHub ──────────────────────────────────────────────────────

    /** Redirige al usuario a la pantalla de autorización de GitHub. */
    @GetMapping("/github")
    public void gitHubLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect(gitHubService.getAuthorizationUrl());
    }

    /** Recibe el code de GitHub, lo procesa y redirige al frontend con el JWT. */
    @GetMapping("/github/callback")
    public void gitHubCallback(@RequestParam("code") String code,
                               @RequestParam(value = "error", required = false) String error,
                               HttpServletResponse response) throws IOException {
        if (error != null) {
            response.sendRedirect(frontendUrl + "/auth/error?reason=" + error);
            return;
        }
        try {
            String accessToken     = gitHubService.exchangeCodeForToken(code);
            OAuthUserInfo userInfo = gitHubService.fetchUserInfo(accessToken);
            Usuario usuario        = usuarioService.findOrCreate(userInfo);
            String jwt             = jwtService.generateToken(
                    usuario.getUsuarioId(), usuario.getEmail(), usuario.getRol());

            response.sendRedirect(frontendUrl + "/auth/callback?token=" + jwt);
        } catch (Exception e) {
            response.sendRedirect(frontendUrl + "/auth/error?reason=github_error");
        }
    }

    // ─── Google ──────────────────────────────────────────────────────

    /** Redirige al usuario a la pantalla de autorización de Google. */
    @GetMapping("/google")
    public void googleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect(googleService.getAuthorizationUrl());
    }

    /** Recibe el code de Google, lo procesa y redirige al frontend con el JWT. */
    @GetMapping("/google/callback")
    public void googleCallback(@RequestParam("code") String code,
                               @RequestParam(value = "error", required = false) String error,
                               HttpServletResponse response) throws IOException {
        if (error != null) {
            response.sendRedirect(frontendUrl + "/auth/error?reason=" + error);
            return;
        }
        try {
            String accessToken     = googleService.exchangeCodeForToken(code);
            OAuthUserInfo userInfo = googleService.fetchUserInfo(accessToken);
            Usuario usuario        = usuarioService.findOrCreate(userInfo);
            String jwt             = jwtService.generateToken(
                    usuario.getUsuarioId(), usuario.getEmail(), usuario.getRol());

            response.sendRedirect(frontendUrl + "/auth/callback?token=" + jwt);
        } catch (Exception e) {
            response.sendRedirect(frontendUrl + "/auth/error?reason=google_error");
        }
    }
}
