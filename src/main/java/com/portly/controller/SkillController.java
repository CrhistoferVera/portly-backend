package com.portly.controller;

import com.portly.dto.SkillRequest;
import com.portly.dto.SkillResponse;
import com.portly.service.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    // GET /api/skills — obtener todas las habilidades del usuario autenticado
    @GetMapping
    public ResponseEntity<List<SkillResponse>> getAll(Authentication authentication) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(skillService.getAll(usuarioId));
    }

    // POST /api/skills — agregar nueva habilidad
    @PostMapping
    public ResponseEntity<SkillResponse> create(
            Authentication authentication,
            @Valid @RequestBody SkillRequest request) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(skillService.create(usuarioId, request));
    }

    // PUT /api/skills/{id} — editar nivel de dominio
    @PutMapping("/{id}")
    public ResponseEntity<SkillResponse> update(
            Authentication authentication,
            @PathVariable Integer id,
            @Valid @RequestBody SkillRequest request) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(skillService.update(usuarioId, id, request));
    }

    // DELETE /api/skills/{id} — eliminar habilidad
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            Authentication authentication,
            @PathVariable Integer id) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        skillService.delete(usuarioId, id);
        return ResponseEntity.noContent().build();
    }
}
