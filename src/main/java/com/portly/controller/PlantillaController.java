package com.portly.controller;

import com.portly.dto.PlantillaResponse;
import com.portly.service.PlantillaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/plantillas")
@RequiredArgsConstructor
public class PlantillaController {

    private final PlantillaService plantillaService;

    // GET /api/plantillas — obtener todas las plantillas disponibles
    @GetMapping
    public ResponseEntity<List<PlantillaResponse>> getAll() {
        return ResponseEntity.ok(plantillaService.getAll());
    }
}
