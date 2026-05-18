package com.portly.controller;

import com.portly.dto.ActualizacionAcademicaRequest;
import com.portly.dto.ActualizacionAcademicaResponse;
import com.portly.service.ActualizacionAcademicaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile/actualizacion-academica")
@RequiredArgsConstructor
public class ActualizacionAcademicaController {

    private final ActualizacionAcademicaService actualizacionService;

    // GET /api/profile/actualizacion-academica
    @GetMapping
    public ResponseEntity<List<ActualizacionAcademicaResponse>> listarActualizaciones(Authentication authentication) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(actualizacionService.listarActualizaciones(usuarioId));
    }

    // POST /api/profile/actualizacion-academica
    @PostMapping
    public ResponseEntity<ActualizacionAcademicaResponse> agregarActualizacion(
            Authentication authentication,
            @Valid @RequestBody ActualizacionAcademicaRequest request) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(actualizacionService.agregarActualizacion(usuarioId, request));
    }

    // PUT /api/profile/actualizacion-academica/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ActualizacionAcademicaResponse> actualizarActualizacion(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ActualizacionAcademicaRequest request) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(actualizacionService.actualizarActualizacion(usuarioId, id, request));
    }

    // DELETE /api/profile/actualizacion-academica/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarActualizacion(
            Authentication authentication,
            @PathVariable Long id) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        actualizacionService.eliminarActualizacion(usuarioId, id);
        return ResponseEntity.noContent().build();
    }
}
