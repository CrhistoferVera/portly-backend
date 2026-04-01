package com.portly.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.portly.domain.entity.Usuario;
import com.portly.dto.AuthResponse;
import com.portly.dto.LoginRequest;
import com.portly.dto.RegisterRequest;
import com.portly.dto.ForgotPasswordRequest;
import com.portly.dto.VerifyCodeRequest;
import com.portly.dto.ResetPasswordRequest;

import com.portly.service.AuthService;
import com.portly.service.GitHubOAuthService;
import com.portly.service.GoogleOAuthService;
import com.portly.service.JwtService;
import com.portly.service.LinkedInOAuthService;
import com.portly.service.OAuthUserInfo;
import com.portly.service.UsuarioService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.Map;

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

    // ─── Registro y loggeo con email y contraseña ─────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest body) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(body));  
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

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.solicitarRecuperacionPassword(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Código de recuperación enviado al correo."));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        authService.verificarCodigo(request.getEmail(), request.getCodigo());
        return ResponseEntity.ok(Map.of("message", "Código verificado correctamente."));
    }

        @PostMapping("/reset-password")
        public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
            authService.restablecerPassword(request.getEmail(), request.getCodigo(), request.getNuevaPassword());
            return ResponseEntity.ok(Map.of("message", "Contraseña restablecida correctamente."));
        }
}
