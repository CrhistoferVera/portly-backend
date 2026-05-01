package com.portly.controller;

import com.portly.domain.entity.DocumentoProyecto;
import com.portly.dto.DocumentoProyectoResponse;
import com.portly.service.DocumentoProyectoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/proyectos/documentos")
@RequiredArgsConstructor
public class DocumentoProyectoController {

    private final DocumentoProyectoService documentoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoProyectoResponse> subirDocumento(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {

        UUID idUsuario = (UUID) authentication.getPrincipal();
        DocumentoProyectoResponse response = documentoService.subirDocumento(idUsuario, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DocumentoProyectoResponse>> listarDocumentos(Authentication authentication) {
        UUID idUsuario = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(documentoService.listarDocumentos(idUsuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDocumento(
            Authentication authentication,
            @PathVariable Integer id) {

        UUID idUsuario = (UUID) authentication.getPrincipal();
        documentoService.eliminarDocumento(idUsuario, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/descargar")
    public ResponseEntity<Resource> descargarDocumento(@PathVariable Integer id) {
        Resource resource = documentoService.cargarDocumentoComoRecurso(id);
        DocumentoProyecto documento = documentoService.obtenerDocumentoEntity(id);

        String contentType = "application/octet-stream";
        if (documento.getFormato().equalsIgnoreCase("pdf")) {
            contentType = "application/pdf";
        } else if (documento.getFormato().equalsIgnoreCase("doc")) {
            contentType = "application/msword";
        } else if (documento.getFormato().equalsIgnoreCase("docx")) {
            contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + documento.getNombreOriginal() + "\"")
                .body(resource);
    }
}
