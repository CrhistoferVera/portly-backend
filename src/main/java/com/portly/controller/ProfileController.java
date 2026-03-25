package com.portly.controller;

import com.portly.dto.UsuarioProfileResponse;
import com.portly.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/profile")
    public ResponseEntity<UsuarioProfileResponse> getProfile(Authentication authentication) {
        UUID usuarioId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(profileService.getProfile(usuarioId));
    }
}
