package com.portly.controller;

import com.portly.dto.ExperienceRequest;
import com.portly.dto.ExperienceResponse;
import com.portly.service.ExperienciaLaboralService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Adapter controller que expone la API de experiencias laborales
 * en la ruta que el frontend espera: /api/profile/experiencia
 */
@RestController
@RequestMapping("/api/profile/experiencia")
@RequiredArgsConstructor
public class ProfileExperienciaController {

    private final ExperienciaLaboralService service;

    /**
     * GET /api/profile/experiencia → lista todas las experiencias del usuario autenticado
     */
    @GetMapping
    public ResponseEntity<List<ExperienceResponse>> listar(@AuthenticationPrincipal UUID idUsuario) {
        return ResponseEntity.ok(service.listar(idUsuario));
    }

    /**
     * POST /api/profile/experiencia → crea una nueva experiencia
     */
    @PostMapping
    public ResponseEntity<ExperienceResponse> crear(
            @AuthenticationPrincipal UUID idUsuario,
            @RequestBody ExperienceRequest request) {
        ExperienceResponse created = service.crear(idUsuario, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/profile/experiencia/{id} → actualiza una experiencia existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<ExperienceResponse> actualizar(
            @AuthenticationPrincipal UUID idUsuario,
            @PathVariable Integer id,
            @RequestBody ExperienceRequest request) {
        return ResponseEntity.ok(service.actualizar(idUsuario, id, request));
    }

    /**
     * DELETE /api/profile/experiencia/{id} → elimina una experiencia
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @AuthenticationPrincipal UUID idUsuario,
            @PathVariable Integer id) {
        service.eliminar(idUsuario, id);
        return ResponseEntity.noContent().build();
    }
}
