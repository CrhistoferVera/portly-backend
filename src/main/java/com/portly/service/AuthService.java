package com.portly.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portly.domain.entity.PerfilUsuario;
import com.portly.domain.entity.Usuario;
import com.portly.domain.repository.PerfilUsuarioRepository;
import com.portly.domain.repository.UsuarioRepository;
import com.portly.dto.AuthResponse;
import com.portly.dto.LoginRequest;
import com.portly.dto.RegisterRequest;
import com.portly.exception.EmailAlreadyExistsException;
import com.portly.exception.EmailDoesNotExistException;
import com.portly.exception.PasswordMismatchException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository       usuarioRepository;
    private final PerfilUsuarioRepository perfilUsuarioRepository;
    private final PasswordEncoder         passwordEncoder;
    private final JwtService              jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.getContrasena().equals(request.getConfirmarContrasena())) {
            throw new PasswordMismatchException();
        }

        if (usuarioRepository.existsByEmail(request.getCorreoElectronico())) {
            throw new EmailAlreadyExistsException(request.getCorreoElectronico());
        }

        Usuario usuario = Usuario.builder()
                .email(request.getCorreoElectronico())
                .contrasenaEncriptada(passwordEncoder.encode(request.getContrasena()))
                .rol("usuario")
                .estado("activo")
                .correoVerificado(false)
                .fechaCreacion(LocalDateTime.now())
                .build();
        usuario = usuarioRepository.save(usuario);

        PerfilUsuario perfil = PerfilUsuario.builder()
                .usuario(usuario)
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .titularProfesional(request.getProfesion())
                .acercaDeMi(request.getBiografia())
                .fechaActualizacion(LocalDateTime.now())
                .build();
        perfilUsuarioRepository.save(perfil);

        return generateTokenResponse(usuario);
    }

    public AuthResponse login(LoginRequest body) {
        Usuario usuario = usuarioRepository.findByEmail(body.getCorreoElectronico())
            .orElseThrow(() -> new EmailDoesNotExistException(body.getCorreoElectronico()));
        if (!passwordEncoder.matches(body.getContraseña(), usuario.getContrasenaEncriptada())) {
            throw new PasswordMismatchException();
        }
        return generateTokenResponse(usuario);
    }

    public AuthResponse generateTokenResponse(Usuario usuario) {
        String token = jwtService.generateToken(usuario.getIdUsuario(), usuario.getEmail(), usuario.getRol());
        return new AuthResponse(token, usuario.getIdUsuario(), usuario.getEmail(), usuario.getRol());
    }
}
