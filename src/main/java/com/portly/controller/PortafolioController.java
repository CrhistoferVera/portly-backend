package com.portly.controller;

import com.portly.dto.PortafolioRequest;
import com.portly.dto.PortafolioResponse;
import com.portly.dto.PortafolioPublicoResponse;
import com.portly.dto.VisibilidadItemsRequest;
import com.portly.dto.ExploreSearchResult;
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

    @PutMapping("/{id}/publicar")
    public ResponseEntity<PortafolioResponse> publicar(
            Authentication authentication,
            @PathVariable UUID id) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(portafolioService.publicar(usuarioId, id));
    }

    @PutMapping("/{id}/privatizar")
    public ResponseEntity<PortafolioResponse> privatizar(
            Authentication authentication,
            @PathVariable UUID id) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(portafolioService.privatizar(usuarioId, id));
    }

    @GetMapping("/{id}/publica")
    public ResponseEntity<PortafolioPublicoResponse> getPublico(
            Authentication authentication,
            @PathVariable("id") String id) {
        return ResponseEntity.ok(portafolioService.getPublico(id, authentication));
    }

    @GetMapping("/search")
    public ResponseEntity<ExploreSearchResult> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "recientes") String sort,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "12") int limit) {
        return ResponseEntity.ok(portafolioService.searchPortafolios(q, sort, page, limit));
    }
}
