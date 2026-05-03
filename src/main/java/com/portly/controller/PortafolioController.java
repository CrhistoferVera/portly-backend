package com.portly.controller;

import com.portly.dto.PortafolioRequest;
import com.portly.dto.PortafolioResponse;
import com.portly.dto.PortafolioPublicoResponse;
import com.portly.dto.VisibilidadItemsRequest;
import com.portly.service.PortafolioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/portafolios")
@RequiredArgsConstructor
public class PortafolioController {

    private final PortafolioService portafolioService;

    @GetMapping
    public ResponseEntity<List<PortafolioResponse>> getAll(Authentication authentication) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(portafolioService.getAll(usuarioId));
    }

    @PostMapping
    public ResponseEntity<PortafolioResponse> create(
            Authentication authentication,
            @Valid @RequestBody PortafolioRequest request) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(portafolioService.create(usuarioId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            Authentication authentication,
            @PathVariable UUID id) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        portafolioService.delete(usuarioId, id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/visibilidad")
    public ResponseEntity<PortafolioResponse> updateVisibilidad(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestBody VisibilidadItemsRequest request) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(portafolioService.updateVisibilidad(usuarioId, id, request));
    }

    @GetMapping("/{id}/publica")
    public ResponseEntity<PortafolioPublicoResponse> getPublico(
            Authentication authentication,
            @PathVariable("id") String id) {
        return ResponseEntity.ok(portafolioService.getPublico(id, authentication));
    }
}
