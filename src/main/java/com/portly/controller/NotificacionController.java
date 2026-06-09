package com.portly.controller;

import com.portly.dto.NotificacionResponse;

import com.portly.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping
    public ResponseEntity<List<NotificacionResponse>> obtenerNotificaciones(Authentication authentication) {
        UUID userId = extractUserId(authentication);
        if (userId == null) return ResponseEntity.status(401).build();

        return ResponseEntity.ok(notificacionService.obtenerNotificacionesUsuario(userId));
    }

    @GetMapping("/no-leidas")
    public ResponseEntity<Map<String, Long>> contarNoLeidas(Authentication authentication) {
        UUID userId = extractUserId(authentication);
        if (userId == null) return ResponseEntity.status(401).build();

        long count = notificacionService.obtenerCantidadNoLeidas(userId);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/leer")
    public ResponseEntity<Void> marcarComoLeidas(Authentication authentication) {
        UUID userId = extractUserId(authentication);
        if (userId == null) return ResponseEntity.status(401).build();

        notificacionService.marcarComoLeidas(userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/leer")
    public ResponseEntity<Void> marcarUnaComoLeida(@PathVariable UUID id, Authentication authentication) {
        UUID userId = extractUserId(authentication);
        if (userId == null) return ResponseEntity.status(401).build();

        notificacionService.marcarUnaComoLeida(userId, id);
        return ResponseEntity.ok().build();
    }

    private UUID extractUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UUID) {
            return (UUID) authentication.getPrincipal();
        }
        return null;
    }
}
