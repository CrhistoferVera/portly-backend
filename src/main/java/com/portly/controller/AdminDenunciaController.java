package com.portly.controller;

import com.portly.dto.DenunciaAgrupadaResponse;
import com.portly.dto.RevisarDenunciaRequest;
import com.portly.service.DenunciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/denuncias")
@RequiredArgsConstructor
public class AdminDenunciaController {

    private final DenunciaService denunciaService;

    /**
     * GET /api/admin/denuncias
     * Lista todas las denuncias agrupadas con sus denuncias individuales.
     */
    @GetMapping
    public ResponseEntity<List<DenunciaAgrupadaResponse>> listarDenuncias() {
        return ResponseEntity.ok(denunciaService.listarDenuncias());
    }

    /**
     * GET /api/admin/denuncias/{id}
     * Detalle de una denuncia agrupada.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DenunciaAgrupadaResponse> obtenerDenuncia(@PathVariable Long id) {
        return ResponseEntity.ok(denunciaService.obtenerDenuncia(id));
    }

    /**
     * GET /api/admin/denuncias/usuario/{userId}/historial
     * Historial de denuncias de un usuario.
     */
    @GetMapping("/usuario/{userId}/historial")
    public ResponseEntity<List<DenunciaAgrupadaResponse>> obtenerHistorialUsuario(@PathVariable UUID userId) {
        return ResponseEntity.ok(denunciaService.obtenerHistorialUsuario(userId));
    }

    /**
     * PATCH /api/admin/denuncias/{id}/revisar
     * Marca una denuncia como revisada.
     */
    @PatchMapping("/{id}/revisar")
    public ResponseEntity<DenunciaAgrupadaResponse> revisarDenuncia(
            @PathVariable Long id,
            @Valid @RequestBody RevisarDenunciaRequest request,
            Authentication authentication) {

        UUID adminId = (UUID) authentication.getPrincipal();
        DenunciaAgrupadaResponse response = denunciaService.revisarDenuncia(id, request.getResultado(), adminId);
        return ResponseEntity.ok(response);
    }
}
