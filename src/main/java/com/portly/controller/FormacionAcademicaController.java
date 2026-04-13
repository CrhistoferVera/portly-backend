package com.portly.controller;

import com.portly.dto.FormacionAcademicaRequest;
import com.portly.dto.FormacionAcademicaResponse;
import com.portly.service.FormacionAcademicaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile/formacion")
@RequiredArgsConstructor
public class FormacionAcademicaController {

    private final FormacionAcademicaService formacionService;

    // GET /api/profile/formacion
    // Listar todas las formaciones académicas del usuario autenticado
    @GetMapping
    public ResponseEntity<List<FormacionAcademicaResponse>> listarFormaciones(Authentication authentication) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(formacionService.listarFormaciones(usuarioId));
    }

    // POST /api/profile/formacion
    // Agregar una nueva formación académica
    @PostMapping
    public ResponseEntity<FormacionAcademicaResponse> agregarFormacion(
            Authentication authentication,
            @Valid @RequestBody FormacionAcademicaRequest request) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(formacionService.agregarFormacion(usuarioId, request));
    }

    // PUT /api/profile/formacion/{id}
    // Editar una formación académica existente (solo si pertenece al usuario autenticado)
    @PutMapping("/{id}")
    public ResponseEntity<FormacionAcademicaResponse> actualizarFormacion(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody FormacionAcademicaRequest request) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(formacionService.actualizarFormacion(usuarioId, id, request));
    }

    // DELETE /api/profile/formacion/{id}
    // Eliminar una formación académica (solo si pertenece al usuario autenticado)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarFormacion(
            Authentication authentication,
            @PathVariable Long id) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        formacionService.eliminarFormacion(usuarioId, id);
        return ResponseEntity.noContent().build();
    }
}
