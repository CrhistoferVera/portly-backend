package com.portly.controller;

import com.portly.dto.AppealResponse;
import com.portly.service.ApelacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/apelaciones")
@RequiredArgsConstructor
public class AdminApelacionController {

    private final ApelacionService apelacionService;

    @GetMapping
    public ResponseEntity<List<AppealResponse>> listarApelaciones() {
        return ResponseEntity.ok(apelacionService.listarApelaciones());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppealResponse> obtenerApelacion(@PathVariable Long id) {
        return ResponseEntity.ok(apelacionService.obtenerApelacion(id));
    }

    @PostMapping("/{id}/aprobar")
    public ResponseEntity<Map<String, String>> aprobarApelacion(
            @PathVariable Long id,
            Authentication authentication) {
        UUID adminId = (UUID) authentication.getPrincipal();
        apelacionService.aprobarApelacion(id, adminId);
        return ResponseEntity.ok(Map.of("message", "Apelación aprobada y usuario reactivado con éxito"));
    }

    @PostMapping("/{id}/rechazar")
    public ResponseEntity<Map<String, String>> rechazarApelacion(
            @PathVariable Long id,
            Authentication authentication) {
        UUID adminId = (UUID) authentication.getPrincipal();
        apelacionService.rechazarApelacion(id, adminId);
        return ResponseEntity.ok(Map.of("message", "Apelación rechazada con éxito"));
    }
}
