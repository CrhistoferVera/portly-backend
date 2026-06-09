package com.portly.controller;

import com.portly.dto.PerfilProfesionalRequest;
import com.portly.dto.PerfilProfesionalResponse;
import com.portly.service.CloudinaryService;
import com.portly.service.PerfilProfesionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * API de perfiles profesionales del usuario autenticado.
 * Cada perfil contiene una identidad profesional alternativa (etiqueta,
 * titular, descripción y foto opcional) que se puede asignar a un portafolio.
 */
@Slf4j
@RestController
@RequestMapping("/api/perfiles-profesionales")
@RequiredArgsConstructor
public class PerfilProfesionalController {

    private final PerfilProfesionalService service;
    private final CloudinaryService cloudinaryService;

    @GetMapping
    public ResponseEntity<List<PerfilProfesionalResponse>> listar(
            @AuthenticationPrincipal UUID idUsuario) {
        return ResponseEntity.ok(service.listar(idUsuario));
    }

    @PostMapping
    public ResponseEntity<PerfilProfesionalResponse> crear(
            @AuthenticationPrincipal UUID idUsuario,
            @Valid @RequestBody PerfilProfesionalRequest request) {
        PerfilProfesionalResponse created = service.crear(idUsuario, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PerfilProfesionalResponse> actualizar(
            @AuthenticationPrincipal UUID idUsuario,
            @PathVariable UUID id,
            @Valid @RequestBody PerfilProfesionalRequest request) {
        return ResponseEntity.ok(service.actualizar(idUsuario, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @AuthenticationPrincipal UUID idUsuario,
            @PathVariable UUID id) {
        service.eliminar(idUsuario, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Sube una foto a Cloudinary y devuelve su URL.
     * Desacopla la subida de la existencia del perfil: el modal sube la
     * imagen primero, obtiene la URL y la envía en el create/update.
     */
    @PostMapping(value = "/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> subirFoto(
            @AuthenticationPrincipal UUID idUsuario,
            @RequestParam("file") MultipartFile file) {
        try {
            String url = cloudinaryService.uploadImage(file, "perfiles_profesionales");
            return ResponseEntity.ok(Map.of("fotoUrl", url));
        } catch (IOException e) {
            log.error("Error al subir foto de perfil profesional (usuario={})", idUsuario, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo subir la imagen");
        }
    }
}
