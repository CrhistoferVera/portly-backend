package com.portly.controller;

import com.portly.dto.*;
import com.portly.service.AuthService;
import com.portly.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final AuthService authService;

    // POST /api/complete-profile
    // Completa profesión y reseña para usuarios registrados via OAuth
    @PostMapping("/complete-profile")
    public ResponseEntity<AuthResponse> completeOAuthProfile(
            Authentication authentication,
            @Valid @RequestBody CompleteOAuthProfileRequest request) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(authService.completarPerfilOAuth(usuarioId, request.getProfesion(), request.getResena()));
    }

    // POST /api/redes-sociales
    @PostMapping("/redes-sociales")
    public ResponseEntity<Void> actualizarRedesSociales(
            Authentication authentication,
            @RequestBody RedesSocialesRequest request) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        profileService.actualizarRedesSociales(usuarioId, request);
        return ResponseEntity.ok().build();
    }

    // POST /api/redes-sociales/user
    @PostMapping("/redes-sociales/user")
    public ResponseEntity<RedesSocialesRequest> obtenerRedesSocialesUser(@RequestBody ForgotPasswordRequest request) {
        RedesSocialesRequest response = profileService.obtenerRedesSocialesPorEmail(request.getEmail());
        return ResponseEntity.ok(response);
    }

    // GET /api/profile
    // Obtener el perfil completo del usuario autenticado
    @GetMapping("/profile")
    public ResponseEntity<UsuarioProfileResponse> getProfile(Authentication authentication) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(profileService.getProfile(usuarioId));
    }

    // PUT /api/profile
    // Actualizar datos del perfil: nombre, apellido, titularProfesional, acercaDeMi, enlaceFoto, pais, ciudad
    @PutMapping("/profile")
    public ResponseEntity<UsuarioProfileResponse> actualizarPerfil(
            Authentication authentication,
            @Valid @RequestBody ActualizarPerfilRequest request) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(profileService.actualizarPerfil(usuarioId, request));
    }

    @PostMapping(value = "/profile/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> subirAvatar(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        UsuarioProfileResponse response = profileService.subirAvatar(usuarioId, file);
        return ResponseEntity.ok(Map.of("avatarUrl", response.getEnlaceFoto()));
    }

    // POST /api/profile/experiencia
    // Agregar una nueva experiencia laboral al perfil del usuario autenticado
    @PostMapping("/profile/experiencia")
    public ResponseEntity<UsuarioProfileResponse.ExperienciaDto> agregarExperiencia(
            Authentication authentication,
            @Valid @RequestBody ExperienciaRequest request) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.agregarExperiencia(usuarioId, request));
    }

    // PUT /api/profile/experiencia/{id}
    // Editar una experiencia laboral existente (solo si pertenece al usuario autenticado)
    @PutMapping("/profile/experiencia/{id}")
    public ResponseEntity<UsuarioProfileResponse.ExperienciaDto> actualizarExperiencia(
            Authentication authentication,
            @PathVariable Integer id,
            @Valid @RequestBody ExperienciaRequest request) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(profileService.actualizarExperiencia(usuarioId, id, request));
    }

    // DELETE /api/profile/experiencia/{id}
    // Eliminar una experiencia laboral (solo si pertenece al usuario autenticado)
    @DeleteMapping("/profile/experiencia/{id}")
    public ResponseEntity<Void> eliminarExperiencia(
            Authentication authentication,
            @PathVariable Integer id) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        profileService.eliminarExperiencia(usuarioId, id);
        return ResponseEntity.noContent().build();
    }

    // POST /api/profile/enlace
    // Agregar un nuevo enlace profesional (LinkedIn, GitHub, portfolio, etc.)
    @PostMapping("/profile/enlace")
    public ResponseEntity<UsuarioProfileResponse.EnlaceDto> agregarEnlace(
            Authentication authentication,
            @Valid @RequestBody EnlaceRequest request) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.agregarEnlace(usuarioId, request));
    }

    // DELETE /api/profile/enlace/{id}
    // Eliminar un enlace profesional (solo si pertenece al usuario autenticado)
    @DeleteMapping("/profile/enlace/{id}")
    public ResponseEntity<Void> eliminarEnlace(
            Authentication authentication,
            @PathVariable Integer id) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        profileService.eliminarEnlace(usuarioId, id);
        return ResponseEntity.noContent().build();
    }
}
