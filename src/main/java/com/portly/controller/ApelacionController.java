package com.portly.controller;

import com.portly.dto.CrearApelacionRequest;
import com.portly.service.ApelacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/appeal")
@RequiredArgsConstructor
public class ApelacionController {

    private final ApelacionService apelacionService;

    @PostMapping
    public ResponseEntity<Map<String, String>> crearApelacion(
            @Valid @RequestBody CrearApelacionRequest request) {
        
        apelacionService.crearApelacion(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Apelación registrada con éxito"));
    }
}
