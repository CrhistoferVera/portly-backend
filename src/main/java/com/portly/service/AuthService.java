package com.portly.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portly.domain.entity.PerfilUsuario;
import com.portly.domain.entity.Usuario;
import com.portly.domain.entity.CodigoRecuperacion;
import com.portly.domain.repository.CodigoRecuperacionRepository;
import com.portly.domain.repository.PerfilUsuarioRepository;
import com.portly.domain.repository.UsuarioRepository;

import com.portly.dto.AuthResponse;
import com.portly.dto.LoginRequest;
import com.portly.dto.RegisterRequest;
import com.portly.exception.EmailAlreadyExistsException;
import com.portly.exception.EmailDoesNotExistException;
import com.portly.exception.PasswordMismatchException;
import com.portly.exception.InvalidCodeException;
import com.portly.exception.CodeExpiredException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository       usuarioRepository;
    private final PerfilUsuarioRepository perfilUsuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CodigoRecuperacionRepository codigoRecuperacionRepository;
    private final EmailService emailService;

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

    @Transactional
    public void solicitarRecuperacionPassword(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EmailDoesNotExistException(email));

      
        codigoRecuperacionRepository.deleteByUsuario_IdUsuario(usuario.getIdUsuario());

        String codigo = generarCodigoSeisDigitos();

        CodigoRecuperacion codigoRecuperacion = CodigoRecuperacion.builder()
                .usuario(usuario)
                .codigo(codigo)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(10))
                .build();
        codigoRecuperacionRepository.save(codigoRecuperacion);
        emailService.enviarCodigoRecuperacion(usuario.getEmail(), codigo);
    }

    public void verificarCodigo(String email, String codigo) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EmailDoesNotExistException(email));

        CodigoRecuperacion codigoGuardado = codigoRecuperacionRepository.findByCodigoAndUsuario_IdUsuario(codigo, usuario.getIdUsuario())
                .orElseThrow(() -> new InvalidCodeException());

        if (codigoGuardado.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new CodeExpiredException();
        }
    }

   @Transactional
    public void restablecerPassword(String email, String codigo, String nuevaPassword) {
        verificarCodigo(email, codigo);
        Usuario usuario = usuarioRepository.findByEmail(email).get();

        if (passwordEncoder.matches(nuevaPassword, usuario.getContrasenaEncriptada())) {
            throw new IllegalArgumentException("La nueva contraseña no puede ser igual a la actual."); 
        }
        usuario.setContrasenaEncriptada(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
        codigoRecuperacionRepository.deleteByUsuario_IdUsuario(usuario.getIdUsuario());
    }

    private String generarCodigoSeisDigitos() {
        SecureRandom random = new SecureRandom();
        int numero = 100000 + random.nextInt(900000); 
        return String.valueOf(numero);
    }
}
