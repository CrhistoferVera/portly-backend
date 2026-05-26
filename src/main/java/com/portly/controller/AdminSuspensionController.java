package com.portly.controller;

import com.portly.dto.SuspenderUsuarioRequest;
import com.portly.dto.SuspensionResponse;
import com.portly.service.SuspensionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminSuspensionController {

    private final SuspensionService suspensionService;

    /**
     * GET /api/admin/usuarios-suspendidos
     * Lista todos los usuarios con suspensiones activas.
     */
    @GetMapping("/usuarios-suspendidos")
    public ResponseEntity<List<SuspensionResponse>> listarSuspendidos() {
        return ResponseEntity.ok(suspensionService.listarSuspendidos());
    }

    /**
     * POST /api/admin/usuarios/{userId}/suspender
     * Suspende la cuenta de un usuario.
     */
    @PostMapping("/usuarios/{userId}/suspender")
    public ResponseEntity<Map<String, Object>> suspenderUsuario(
            @PathVariable UUID userId,
            @Valid @RequestBody SuspenderUsuarioRequest request,
            Authentication authentication) {

        UUID adminId = (UUID) authentication.getPrincipal();
        SuspensionResponse suspension = suspensionService.suspenderUsuario(
                userId, request.getMotivo(), adminId);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Usuario suspendido exitosamente",
                "suspension", suspension
        ));
    }

    /**
     * POST /api/admin/usuarios/{userId}/reactivar
     * Reactiva la cuenta de un usuario suspendido.
     */
    @PostMapping("/usuarios/{userId}/reactivar")
    public ResponseEntity<Map<String, String>> reactivarUsuario(@PathVariable UUID userId) {
        suspensionService.reactivarUsuario(userId);
        return ResponseEntity.ok(Map.of("message", "Cuenta reactivada exitosamente"));
    }
}
