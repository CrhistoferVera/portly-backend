package com.portly.controller;

import com.portly.dto.AdminUserResponse;
import com.portly.domain.entity.Usuario;
import com.portly.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<List<AdminUserResponse>> getRegisteredUsers() {
        List<Usuario> users = usuarioRepository.findAll();
        
        List<AdminUserResponse> response = users.stream().map(u -> {
            String nombreCompleto = u.getPerfil() != null ? 
                u.getPerfil().getNombre() + " " + u.getPerfil().getApellido() : "Sin Perfil";
            
            if (nombreCompleto.trim().isEmpty() || nombreCompleto.equals(" ")) {
                nombreCompleto = "Sin Nombre";
            }
            
            boolean hasPublicPortfolio = u.getMisPortafolios() != null && u.getMisPortafolios().stream()
                .anyMatch(p -> "PUBLICO".equalsIgnoreCase(p.getVisibilidad()));

            return AdminUserResponse.builder()
                .idUsuario(u.getIdUsuario())
                .email(u.getEmail())
                .nombreCompleto(nombreCompleto.trim())
                .fechaCreacion(u.getFechaCreacion())
                .estado(u.getEstado() != null ? u.getEstado() : "activo")
                .hasPublicPortfolio(hasPublicPortfolio)
                .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
