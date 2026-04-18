package com.portly.controller;

import com.portly.dto.HabilidadBlandaRequest;
import com.portly.dto.HabilidadBlandaResponse;
import com.portly.service.HabilidadBlandaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/soft-skills")
@RequiredArgsConstructor
public class HabilidadBlandaController {

    private final HabilidadBlandaService habilidadBlandaService;

    // GET /api/soft-skills — obtener todas las habilidades blandas del usuario autenticado
    @GetMapping
    public ResponseEntity<List<HabilidadBlandaResponse>> getAll(Authentication authentication) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(habilidadBlandaService.getAll(usuarioId));
    }

    // POST /api/soft-skills — agregar nueva habilidad blanda
    @PostMapping
    public ResponseEntity<HabilidadBlandaResponse> create(
            Authentication authentication,
            @Valid @RequestBody HabilidadBlandaRequest request) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(habilidadBlandaService.create(usuarioId, request));
    }

    // DELETE /api/soft-skills/{id} — eliminar habilidad blanda
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            Authentication authentication,
            @PathVariable Integer id) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        habilidadBlandaService.delete(usuarioId, id);
        return ResponseEntity.noContent().build();
    }
}
