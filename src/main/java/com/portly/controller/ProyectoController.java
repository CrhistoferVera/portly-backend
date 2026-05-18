package com.portly.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.portly.dto.EvidenciaProyectoResponse;
import com.portly.dto.GitHubRepoDto;
import com.portly.dto.ProyectoRequest;
import com.portly.dto.ProyectoResponse;
import com.portly.service.EvidenciaProyectoService;
import com.portly.service.GitHubRepoService;
import com.portly.service.ProyectoPersonalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/proyectos")
@RequiredArgsConstructor
public class ProyectoController {

    private final ProyectoPersonalService proyectoService;
    private final EvidenciaProyectoService evidenciaService;
    private final GitHubRepoService gitHubRepoService;

    // ──────────────────────────────────────────────────────────────
    // 1. Proyectos CRUD
    // ──────────────────────────────────────────────────────────────

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProyectoResponse> crearProyecto(
            Authentication authentication,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "icono", required = false) MultipartFile iconoFile) throws IOException {
        
        UUID idUsuario = (UUID) authentication.getPrincipal();
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        ProyectoRequest request = mapper.readValue(dataJson, ProyectoRequest.class);

        ProyectoResponse response = proyectoService.crearProyecto(idUsuario, request, iconoFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProyectoResponse>> listarProyectos(Authentication authentication) {
        UUID idUsuario = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(proyectoService.listarProyectos(idUsuario));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProyectoResponse> obtenerProyecto(
            Authentication authentication,
            @PathVariable Long id) {
        UUID idUsuario = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(proyectoService.obtenerProyecto(idUsuario, id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProyectoResponse> actualizarProyecto(
            Authentication authentication,
            @PathVariable Long id,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "icono", required = false) MultipartFile iconoFile) throws IOException {
        
        UUID idUsuario = (UUID) authentication.getPrincipal();
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        ProyectoRequest request = mapper.readValue(dataJson, ProyectoRequest.class);

        ProyectoResponse response = proyectoService.actualizarProyecto(idUsuario, id, request, iconoFile);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProyecto(
            Authentication authentication,
            @PathVariable Long id) {
        UUID idUsuario = (UUID) authentication.getPrincipal();
        proyectoService.eliminarProyecto(idUsuario, id);
        return ResponseEntity.noContent().build();
    }

    // ──────────────────────────────────────────────────────────────
    // 2. Sincronización GitHub
    // ──────────────────────────────────────────────────────────────

    @GetMapping("/github/repos")
    public ResponseEntity<List<GitHubRepoDto>> listarReposGitHub(Authentication authentication) {
        UUID idUsuario = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(gitHubRepoService.listarReposDisponibles(idUsuario));
    }

    // ──────────────────────────────────────────────────────────────
    // 3. Evidencias (Galería)
    // ──────────────────────────────────────────────────────────────

    @PostMapping(value = "/evidencias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EvidenciaProyectoResponse> subirEvidencia(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {

        UUID idUsuario = (UUID) authentication.getPrincipal();
        EvidenciaProyectoResponse response = evidenciaService.subirEvidencia(idUsuario, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/evidencias")
    public ResponseEntity<List<EvidenciaProyectoResponse>> listarEvidencias(Authentication authentication) {
        UUID idUsuario = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(evidenciaService.listarEvidencias(idUsuario));
    }

    @DeleteMapping("/evidencias/{id}")
    public ResponseEntity<Void> eliminarEvidencia(
            Authentication authentication,
            @PathVariable Integer id) {

        UUID idUsuario = (UUID) authentication.getPrincipal();
        evidenciaService.eliminarEvidencia(idUsuario, id);
        return ResponseEntity.noContent().build();
    }
}
