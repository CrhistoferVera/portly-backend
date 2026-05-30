package com.portly.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portly.domain.entity.PerfilUsuario;
import com.portly.domain.entity.Usuario;
import com.portly.domain.entity.CodigoRecuperacion;
import com.portly.domain.entity.CodigoRegistro;
import com.portly.domain.repository.CodigoRecuperacionRepository;
import com.portly.domain.repository.CodigoRegistroRepository;
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
import com.portly.exception.SamePasswordException;
import com.portly.exception.UsernameAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository           usuarioRepository;
    private final PerfilUsuarioRepository     perfilUsuarioRepository;
    private final PasswordEncoder             passwordEncoder;
    private final JwtService                  jwtService;
    private final CodigoRecuperacionRepository codigoRecuperacionRepository;
    private final CodigoRegistroRepository    codigoRegistroRepository;
    private final EmailService                emailService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.getContrasena().equals(request.getConfirmarContrasena())) {
            throw new PasswordMismatchException();
        }

        if (usuarioRepository.existsByEmail(request.getCorreoElectronico())) {
            throw new EmailAlreadyExistsException(request.getCorreoElectronico());
        }

        String username = request.getUsername().toLowerCase();
        if (usuarioRepository.existsByUsernameIgnoreCase(username)) {
            throw new UsernameAlreadyExistsException(username);
        }

        Usuario usuario = Usuario.builder()
                .email(request.getCorreoElectronico())
                .username(username)
                .contrasenaEncriptada(passwordEncoder.encode(request.getContrasena()))
                .rol("usuario")
                .estado("activo")
                .correoVerificado(false)
                .perfilCompleto(true)
                .fechaCreacion(LocalDateTime.now())
                .build();
        usuario = usuarioRepository.save(usuario);
        log.info("Nuevo usuario registrado: email={}, username={}", request.getCorreoElectronico(), username);

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
        String identifier = body.getIdentifier().trim();
        Usuario usuario;

        if (identifier.contains("@")) {
            // Tratamos como correo electrónico
            usuario = usuarioRepository.findByEmail(identifier.toLowerCase())
                    .orElseThrow(() -> new EmailDoesNotExistException(identifier));
        } else {
            // Tratamos como nombre de usuario
            usuario = usuarioRepository.findByUsernameIgnoreCase(identifier)
                    .orElseThrow(() -> new RuntimeException("No se encontró ninguna cuenta con ese nombre de usuario"));
        }

        if (usuario.getContrasenaEncriptada() == null || usuario.getContrasenaEncriptada().isEmpty()) {
            throw new RuntimeException("Esta cuenta fue creada con un proveedor externo (Google, GitHub, LinkedIn). Usa ese método para ingresar.");
        }

        if (!passwordEncoder.matches(body.getContraseña(), usuario.getContrasenaEncriptada())) {
            log.warn("Intento de login fallido: identifier={}", identifier);
            throw new PasswordMismatchException();
        }
        log.info("Login exitoso: identifier={}", identifier);
        return generateTokenResponse(usuario);
    }

    public AuthResponse generateTokenResponse(Usuario usuario) {
        boolean perfilCompleto = usuario.getPerfilCompleto() == null || usuario.getPerfilCompleto();
        String token = jwtService.generateToken(usuario.getIdUsuario(), usuario.getEmail(), usuario.getUsername(), usuario.getRol(), perfilCompleto);
        return new AuthResponse(token, usuario.getIdUsuario(), usuario.getEmail(), usuario.getRol());
    }

    @Transactional
    public AuthResponse completarPerfilOAuth(java.util.UUID idUsuario, String username, String profesion, String resena) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String usernameLower = username.toLowerCase();
        // Verificar que el username no esté en uso por otro usuario
        usuarioRepository.findByUsernameIgnoreCase(usernameLower).ifPresent(existing -> {
            if (!existing.getIdUsuario().equals(idUsuario)) {
                throw new UsernameAlreadyExistsException(usernameLower);
            }
        });
        usuario.setUsername(usernameLower);

        PerfilUsuario perfil = perfilUsuarioRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));

        perfil.setTitularProfesional(profesion);
        perfil.setAcercaDeMi(resena);
        perfil.setFechaActualizacion(LocalDateTime.now());
        perfilUsuarioRepository.save(perfil);

        usuario.setPerfilCompleto(true);
        usuarioRepository.save(usuario);

        log.info("Perfil OAuth completado: idUsuario={}, username={}", idUsuario, usernameLower);
        return generateTokenResponse(usuario);
    }

    @Transactional
    public void solicitarRecuperacionPassword(String email) {
        Usuario usuario = buscarUsuarioPorEmail(email);

        codigoRecuperacionRepository.deleteByUsuario_IdUsuario(usuario.getIdUsuario());

        String codigo = generarCodigoSeisDigitos();

        CodigoRecuperacion codigoRecuperacion = CodigoRecuperacion.builder()
                .usuario(usuario)
                .codigo(codigo)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(10))
                .build();
        codigoRecuperacionRepository.save(codigoRecuperacion);
        emailService.enviarCodigoRecuperacion(usuario.getEmail(), codigo);
        log.info("Código de recuperación enviado: email={}", email);
    }

    @Transactional
    public AuthResponse verificarCodigo(String email, String codigo) {
        Usuario usuario = buscarUsuarioPorEmail(email);

        CodigoRecuperacion codigoGuardado = codigoRecuperacionRepository.findByCodigoAndUsuario_IdUsuario(codigo, usuario.getIdUsuario())
                .orElseThrow(() -> {
                    log.warn("Código de recuperación incorrecto: email={}", email);
                    return new InvalidCodeException();
                });

        if (codigoGuardado.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            log.warn("Código de recuperación expirado: email={}", email);
            throw new CodeExpiredException();
        }

        codigoRecuperacionRepository.deleteByUsuario_IdUsuario(usuario.getIdUsuario());

        return generateTokenResponse(usuario);
    }

    public boolean checkOAuthAccountWithoutPassword(String email) {
        Usuario usuario = buscarUsuarioPorEmail(email);
        return usuario.getContrasenaEncriptada() == null || usuario.getContrasenaEncriptada().isEmpty();
    }

    @Transactional
    public void restablecerPassword(String email, String codigo, String nuevaPassword) {
        verificarCodigo(email, codigo);
        Usuario usuario = buscarUsuarioPorEmail(email);

        if (passwordEncoder.matches(nuevaPassword, usuario.getContrasenaEncriptada())) {
            throw new SamePasswordException();
        }
        usuario.setContrasenaEncriptada(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
        log.info("Contraseña restablecida: email={}", email);
    }

    @Transactional
    public void cambiarPassword(String email, String contrasenaActual, String nuevaContrasena) {
        Usuario usuario = buscarUsuarioPorEmail(email);

        if (!passwordEncoder.matches(contrasenaActual, usuario.getContrasenaEncriptada())) {
            throw new IllegalArgumentException("Contraseña actual incorrecta");
        }

        if (passwordEncoder.matches(nuevaContrasena, usuario.getContrasenaEncriptada())) {
            throw new SamePasswordException();
        }

        usuario.setContrasenaEncriptada(passwordEncoder.encode(nuevaContrasena));
        usuarioRepository.save(usuario);
        log.info("Contraseña actualizada: email={}", email);
    }

    @Transactional
    public void enviarCodigoRegistro(String email) {
        codigoRegistroRepository.deleteByEmail(email);

        if (usuarioRepository.existsByEmail(email)) {
            emailService.enviarNotificacionEmailRegistrado(email);
            return;
        }

        String codigo = generarCodigoSeisDigitos();
        CodigoRegistro codigoRegistro = CodigoRegistro.builder()
                .email(email)
                .codigo(codigo)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(10))
                .build();
        codigoRegistroRepository.save(codigoRegistro);
        emailService.enviarCodigoRegistro(email, codigo);
        log.info("Código de registro enviado: email={}", email);
    }

    public void verificarCodigoRegistro(String email, String codigo) {
        CodigoRegistro codigoGuardado = codigoRegistroRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("No existe código de registro para: email={}", email);
                    return new InvalidCodeException();
                });

        if (!codigoGuardado.getCodigo().equals(codigo)) {
            log.warn("Código de registro incorrecto: email={}", email);
            throw new InvalidCodeException();
        }

        if (codigoGuardado.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            log.warn("Código de registro expirado: email={}", email);
            throw new CodeExpiredException();
        }
    }

    private Usuario buscarUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EmailDoesNotExistException(email));
    }

    private String generarCodigoSeisDigitos() {
        SecureRandom random = new SecureRandom();
        int numero = 100000 + random.nextInt(900000);
        return String.valueOf(numero);
    }
}
