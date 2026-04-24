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
import com.portly.dto.ChangePasswordRequest;
import com.portly.dto.SendRegistrationCodeRequest;
import com.portly.dto.VerifyRegistrationCodeRequest;

import com.portly.service.AuthService;
import com.portly.service.GitHubOAuthService;
import com.portly.service.GoogleOAuthService;
import com.portly.service.JwtService;
import com.portly.service.LinkedInOAuthService;
import com.portly.service.OAuthProvider;
import com.portly.service.OAuthUserInfo;
import com.portly.service.UsuarioService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@Slf4j
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

    @PostMapping("/register/send-code")
    public ResponseEntity<?> sendRegistrationCode(@Valid @RequestBody SendRegistrationCodeRequest request) {
        authService.enviarCodigoRegistro(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Código enviado al correo."));
    }

    @PostMapping("/register/verify-code")
    public ResponseEntity<?> verifyRegistrationCode(@Valid @RequestBody VerifyRegistrationCodeRequest request) {
        authService.verificarCodigoRegistro(request.getEmail(), request.getCodigo());
        return ResponseEntity.ok(Map.of("message", "Código verificado correctamente."));
    }
    //Prueba autodeploy
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
    public void linkedInCallback(@RequestParam(value = "code", required = false) String code,
                                 @RequestParam(value = "error", required = false) String error,
                                 @RequestParam(value = "state", required = false) String state,
                                 HttpServletResponse response) throws IOException {
        handleOAuthCallback(code, error, state, response, linkedInService);
    }

    // ─── GitHub ──────────────────────────────────────────────────────

    /** Redirige al usuario a la pantalla de autorización de GitHub. */
    @GetMapping("/github")
    public void gitHubLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect(gitHubService.getAuthorizationUrl());
    }

    /** Recibe el code de GitHub, lo procesa y redirige al frontend con el JWT. */
    @GetMapping("/github/callback")
    public void gitHubCallback(@RequestParam(value = "code", required = false) String code,
                               @RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "state", required = false) String state,
                               HttpServletResponse response) throws IOException {
        handleOAuthCallback(code, error, state, response, gitHubService);
    }

    // ─── Google ──────────────────────────────────────────────────────

    /** Redirige al usuario a la pantalla de autorización de Google. */
    @GetMapping("/google")
    public void googleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect(googleService.getAuthorizationUrl());
    }

    /** Recibe el code de Google, lo procesa y redirige al frontend con el JWT. */
    @GetMapping("/google/callback")
    public void googleCallback(@RequestParam(value = "code", required = false) String code,
                               @RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "state", required = false) String state,
                               HttpServletResponse response) throws IOException {
        handleOAuthCallback(code, error, state, response, googleService);
    }

    // ─── Vinculación de proveedores (desde perfil) ──────────────────

    /** Vincula LinkedIn al usuario actual. Requiere ?token=JWT */
    @GetMapping("/link/linkedin")
    public void linkLinkedIn(@RequestParam("token") String token,
                             HttpServletResponse response) throws IOException {
        handleOAuthLink(token, response, linkedInService);
    }

    /** Vincula GitHub al usuario actual. Requiere ?token=JWT */
    @GetMapping("/link/github")
    public void linkGitHub(@RequestParam("token") String token,
                           HttpServletResponse response) throws IOException {
        handleOAuthLink(token, response, gitHubService);
    }

    /** Vincula Google al usuario actual. Requiere ?token=JWT */
    @GetMapping("/link/google")
    public void linkGoogle(@RequestParam("token") String token,
                           HttpServletResponse response) throws IOException {
        handleOAuthLink(token, response, googleService);
    }

    // ─── Métodos privados compartidos ────────────────────────────────

    private void handleOAuthCallback(String code, String error, String state,
                                     HttpServletResponse response, OAuthProvider provider) throws IOException {
        if (error != null) {
            log.warn("OAuth {} rechazado: reason={}", provider.getProviderName(), error);
            if (state != null && state.startsWith("LINK:")) {
                response.sendRedirect(frontendUrl + "/profile?error=access_denied");
            } else {
                // El proveedor no devolvió el state (comportamiento de GitHub/LinkedIn al cancelar)
                // Si hay token en sesión el usuario estaba logueado, lo mandamos al perfil
                response.sendRedirect(frontendUrl + "/profile?error=access_denied");
            }
            return;
        }
        try {
            String accessToken     = provider.exchangeCodeForToken(code);
            OAuthUserInfo userInfo = provider.fetchUserInfo(accessToken);

            if (state != null && state.startsWith("LINK:")) {
                java.util.UUID userId = java.util.UUID.fromString(state.substring(5));
                try {
                    usuarioService.linkProviderToUser(userId, userInfo);
                    response.sendRedirect(frontendUrl + "/profile?linked=" + provider.getProviderName());
                } catch (RuntimeException re) {
                    log.warn("Error vinculando cuenta a {}: {}", provider.getProviderName(), re.getMessage());
                    response.sendRedirect(frontendUrl + "/profile?error=already_linked");
                }
                return;
            }

            Usuario usuario = usuarioService.findOrCreate(userInfo);
            boolean perfilCompleto = usuario.getPerfilCompleto() == null || usuario.getPerfilCompleto();
            String jwt = jwtService.generateToken(
                    usuario.getIdUsuario(), usuario.getEmail(), usuario.getRol(), perfilCompleto);
            response.sendRedirect(frontendUrl + "/auth/callback?token=" + jwt);
        } catch (Exception e) {
            log.error("Error en callback OAuth {}: {}", provider.getProviderName(), e.getMessage());
            response.sendRedirect(frontendUrl + "/login");
        }
    }

    private void handleOAuthLink(String token, HttpServletResponse response,
                                 OAuthProvider provider) throws IOException {
        java.util.UUID userId = jwtService.extractUsuarioId(token);
        response.sendRedirect(provider.getAuthorizationUrl() + "&state=LINK:" + userId);
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

    @PostMapping("/verify-account-link")
    public ResponseEntity<Map<String, Boolean>> checkAccountStatus(@Valid @RequestBody ForgotPasswordRequest request) {
        boolean isOAuthWithoutPassword = authService.checkOAuthAccountWithoutPassword(request.getEmail());
        return ResponseEntity.ok(Map.of("isOAuthWithoutPassword", isOAuthWithoutPassword));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            authService.cambiarPassword(request.getEmail(), request.getContrasenaActual(), request.getNuevaContrasena());
            return ResponseEntity.ok(Map.of("message", "Actualizacion de contraseña"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Contraseña actual incorrecta"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
