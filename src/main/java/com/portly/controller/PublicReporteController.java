package com.portly.controller;

import com.portly.dto.ReportarDenunciaRequest;
import com.portly.service.DenunciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicReporteController {

    private final DenunciaService denunciaService;

    /**
     * POST /api/public/reportar
     * Endpoint público para reportar un portafolio.
     * No requiere autenticación de administrador.
     */
    @PostMapping("/reportar")
    public ResponseEntity<Map<String, String>> reportarPortafolio(
            @Valid @RequestBody ReportarDenunciaRequest request) {

        denunciaService.reportarPortafolio(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Tu reporte fue enviado. Lo revisaremos a la brevedad."));
    }
}
