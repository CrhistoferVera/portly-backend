package com.portly.service;

import com.portly.domain.entity.CodigoRecuperacion;
import com.portly.domain.entity.Usuario;
import com.portly.domain.repository.CodigoRecuperacionRepository;
import com.portly.domain.repository.UsuarioRepository;
import com.portly.exception.EmailDoesNotExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PasswordRecoveryService {

    private final UsuarioRepository usuarioRepository;
    private final CodigoRecuperacionRepository codigoRepository;
    private final PasswordEncoder passwordEncoder;

    // Paso 1: Generar y guardar un codigo de 6 digitos
    @Transactional
    public String generarCodigo(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EmailDoesNotExistException(email));

        // Borrar codigos anteriores de este usuario
        codigoRepository.deleteByUsuario_IdUsuario(usuario.getIdUsuario());

        // Generar codigo de 6 digitos
        String codigo = String.format("%06d", new Random().nextInt(999999));

        CodigoRecuperacion registro = CodigoRecuperacion.builder()
                .usuario(usuario)
                .codigo(codigo)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(15))
                .build();
        codigoRepository.save(registro);

        // TODO: Enviar el codigo por email (por ahora se devuelve en la respuesta para pruebas)
        return codigo;
    }

    // Paso 2: Verificar que el codigo sea valido y no haya expirado
    @Transactional(readOnly = true)
    public void verificarCodigo(String email, String codigo) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EmailDoesNotExistException(email));

        CodigoRecuperacion registro = codigoRepository
                .findByCodigoAndUsuario_IdUsuario(codigo, usuario.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("El codigo es incorrecto o ha expirado."));

        if (registro.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El codigo ha expirado.");
        }
    }

    // Paso 3: Cambiar la contrasena
    @Transactional
    public void resetearContrasena(String email, String codigo, String nuevaPassword) {
        verificarCodigo(email, codigo);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EmailDoesNotExistException(email));

        usuario.setContrasenaEncriptada(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);

        // Borrar el codigo usado
        codigoRepository.deleteByUsuario_IdUsuario(usuario.getIdUsuario());
    }
}
