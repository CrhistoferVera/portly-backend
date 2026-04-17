package com.portly.controller;

import com.portly.dto.EvidenciaProyectoResponse;
import com.portly.dto.FrontProjectRequest;
import com.portly.dto.FrontProjectResponse;
import com.portly.service.CloudinaryService;
import com.portly.service.EvidenciaProyectoService;
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
import java.util.Map;
import java.util.UUID;

/**
 * Controlador adaptador que expone los endpoints en la ruta que el frontend espera:
 * /api/profile/proyectos (en vez de /api/proyectos del controlador original).
 * Acepta JSON plano (no multipart) para CRUD de proyectos.
 */
@RestController
@RequestMapping("/api/profile/proyectos")
@RequiredArgsConstructor
public class ProfileProyectoController {

    private final ProyectoPersonalService proyectoService;
    private final EvidenciaProyectoService evidenciaService;
    private final CloudinaryService cloudinaryService;

    // ── Proyectos CRUD ──────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<FrontProjectResponse>> listar(Authentication auth) {
        UUID idUsuario = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(proyectoService.listarProyectosJson(idUsuario));
    }

    @PostMapping
    public ResponseEntity<FrontProjectResponse> crear(
            Authentication auth,
            @RequestBody FrontProjectRequest request) {
        UUID idUsuario = (UUID) auth.getPrincipal();
        FrontProjectResponse response = proyectoService.crearProyectoJson(idUsuario, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FrontProjectResponse> actualizar(
            Authentication auth,
            @PathVariable Long id,
            @RequestBody FrontProjectRequest request) {
        UUID idUsuario = (UUID) auth.getPrincipal();
        FrontProjectResponse response = proyectoService.actualizarProyectoJson(idUsuario, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            Authentication auth,
            @PathVariable Long id) {
        UUID idUsuario = (UUID) auth.getPrincipal();
        proyectoService.eliminarProyecto(idUsuario, id);
        return ResponseEntity.noContent().build();
    }

    // ── Evidencias (subida de imágenes) ─────────────────────────────

    @PostMapping(value = "/evidencias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EvidenciaProyectoResponse> subirEvidencia(
            Authentication auth,
            @RequestParam("file") MultipartFile file) {
        UUID idUsuario = (UUID) auth.getPrincipal();
        EvidenciaProyectoResponse response = evidenciaService.subirEvidencia(idUsuario, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/evidencias/{id}")
    public ResponseEntity<Void> eliminarEvidencia(
            Authentication auth,
            @PathVariable Integer id) {
        UUID idUsuario = (UUID) auth.getPrincipal();
        evidenciaService.eliminarEvidencia(idUsuario, id);
        return ResponseEntity.noContent().build();
    }

    // ── Ícono (subida de imagen) ────────────────────────────────────

    @PostMapping(value = "/icono", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> subirIcono(
            Authentication auth,
            @RequestParam("file") MultipartFile file) throws IOException {
        String url = cloudinaryService.uploadImage(file, "portly/proyectos/iconos");
        return ResponseEntity.ok(Map.of("url", url));
    }
}
