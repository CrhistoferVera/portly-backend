package com.portly.service;

import com.portly.domain.entity.PerfilUsuario;
import com.portly.domain.entity.Usuario;
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
import com.portly.exception.SamePasswordException;
import com.portly.domain.entity.CodigoRecuperacion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PerfilUsuarioRepository perfilUsuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private CodigoRecuperacionRepository codigoRecuperacionRepository;
    @Mock private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private static final String EMAIL    = "test@portly.com";
    private static final String PASSWORD = "Password1!";
    private static final UUID   ID       = UUID.randomUUID();

    private Usuario usuarioBase() {
        return Usuario.builder()
                .idUsuario(ID)
                .email(EMAIL)
                .contrasenaEncriptada("$encoded$")
                .rol("usuario")
                .estado("activo")
                .correoVerificado(false)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    // ─── register() ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("register()")
    class Register {

        private RegisterRequest request() {
            RegisterRequest r = new RegisterRequest();
            r.setNombre("Juan");
            r.setApellido("Perez");
            r.setProfesion("Desarrollador");
            r.setCorreoElectronico(EMAIL);
            r.setBiografia("Bio de prueba");
            r.setContrasena(PASSWORD);
            r.setConfirmarContrasena(PASSWORD);
            return r;
        }

        @Test
        @DisplayName("Registra un usuario nuevo y devuelve token")
        void registroExitoso() {
            RegisterRequest req = request();
            when(usuarioRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(PASSWORD)).thenReturn("$encoded$");
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioBase());
            when(perfilUsuarioRepository.save(any(PerfilUsuario.class))).thenReturn(new PerfilUsuario());
            when(jwtService.generateToken(ID, EMAIL, "usuario")).thenReturn("jwt-token");

            AuthResponse response = authService.register(req);

            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getEmail()).isEqualTo(EMAIL);
            verify(usuarioRepository).save(any(Usuario.class));
            verify(perfilUsuarioRepository).save(any(PerfilUsuario.class));
        }

        @Test
        @DisplayName("Lanza EmailAlreadyExistsException si el email ya existe")
        void emailDuplicado() {
            RegisterRequest req = request();
            when(usuarioRepository.existsByEmail(EMAIL)).thenReturn(true);

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(EmailAlreadyExistsException.class);

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Lanza PasswordMismatchException si las contraseñas no coinciden")
        void contrasenasNoCoinciden() {
            RegisterRequest req = request();
            req.setConfirmarContrasena("OtraPassword1!");

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(PasswordMismatchException.class);

            verify(usuarioRepository, never()).existsByEmail(anyString());
        }
    }

    // ─── login() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("login()")
    class Login {

        private LoginRequest request() {
            LoginRequest r = new LoginRequest();
            r.setCorreoElectronico(EMAIL);
            r.setContraseña(PASSWORD);
            return r;
        }

        @Test
        @DisplayName("Login exitoso devuelve token y datos del usuario")
        void loginExitoso() {
            when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioBase()));
            when(passwordEncoder.matches(PASSWORD, "$encoded$")).thenReturn(true);
            when(jwtService.generateToken(ID, EMAIL, "usuario")).thenReturn("jwt-token");

            AuthResponse response = authService.login(request());

            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getEmail()).isEqualTo(EMAIL);
            assertThat(response.getRol()).isEqualTo("usuario");
        }

        @Test
        @DisplayName("Lanza EmailDoesNotExistException si el email no existe")
        void emailNoRegistrado() {
            when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request()))
                    .isInstanceOf(EmailDoesNotExistException.class);
        }

        @Test
        @DisplayName("Lanza PasswordMismatchException si la contraseña es incorrecta")
        void contrasenaIncorrecta() {
            when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioBase()));
            when(passwordEncoder.matches(PASSWORD, "$encoded$")).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request()))
                    .isInstanceOf(PasswordMismatchException.class);
        }
    }

    // ─── checkOAuthAccountWithoutPassword() ──────────────────────────────────

    @Nested
    @DisplayName("checkOAuthAccountWithoutPassword()")
    class CheckOAuthAccount {

        @Test
        @DisplayName("Devuelve true si el usuario no tiene contraseña (cuenta puramente OAuth)")
        void devuelveTrueSinContrasena() {
            Usuario usuario = usuarioBase();
            usuario.setContrasenaEncriptada(null); // Simulamos sin contraseña
            when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));

            boolean result = authService.checkOAuthAccountWithoutPassword(EMAIL);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Devuelve false si el usuario sí tiene contraseña")
        void devuelveFalseConContrasena() {
            Usuario usuario = usuarioBase();
            usuario.setContrasenaEncriptada("$encoded$"); // Simulamos con contraseña
            when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));

            boolean result = authService.checkOAuthAccountWithoutPassword(EMAIL);

            assertThat(result).isFalse();
        }
    }

    // ─── solicitarRecuperacionPassword() ─────────────────────────────────────

    @Nested
    @DisplayName("solicitarRecuperacionPassword()")
    class SolicitarRecuperacionPassword {

        @Test
        @DisplayName("Genera y guarda el código, luego envía el correo")
        void solicitarConExito() {
            when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioBase()));

            authService.solicitarRecuperacionPassword(EMAIL);

            verify(codigoRecuperacionRepository).deleteByUsuario_IdUsuario(ID);
            verify(codigoRecuperacionRepository).save(any(CodigoRecuperacion.class));
            verify(emailService).enviarCodigoRecuperacion(eq(EMAIL), anyString());
        }

        @Test
        @DisplayName("Lanza EmailDoesNotExistException si el correo no existe")
        void correoInexistente() {
            when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.solicitarRecuperacionPassword(EMAIL))
                    .isInstanceOf(EmailDoesNotExistException.class);
        }
    }

    // ─── verificarCodigo() ───────────────────────────────────────────────────

    @Nested
    @DisplayName("verificarCodigo()")
    class VerificarCodigo {

        @Test
        @DisplayName("Verifica correctamente un código válido y vigente")
        void codigoValido() {
            CodigoRecuperacion codigo = CodigoRecuperacion.builder()
                    .codigo("123456")
                    .fechaExpiracion(LocalDateTime.now().plusMinutes(5)) // Código vigente
                    .build();

            when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioBase()));
            when(codigoRecuperacionRepository.findByCodigoAndUsuario_IdUsuario("123456", ID))
                    .thenReturn(Optional.of(codigo));

            authService.verificarCodigo(EMAIL, "123456"); // No debe lanzar excepción
        }

        @Test
        @DisplayName("Lanza InvalidCodeException si el código es incorrecto")
        void codigoIncorrecto() {
            when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioBase()));
            when(codigoRecuperacionRepository.findByCodigoAndUsuario_IdUsuario("123456", ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.verificarCodigo(EMAIL, "123456"))
                    .isInstanceOf(InvalidCodeException.class);
        }

        @Test
        @DisplayName("Lanza CodeExpiredException si el código ya expiró")
        void codigoExpirado() {
            CodigoRecuperacion codigo = CodigoRecuperacion.builder()
                    .codigo("123456")
                    .fechaExpiracion(LocalDateTime.now().minusMinutes(5)) // Código expirado
                    .build();

            when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioBase()));
            when(codigoRecuperacionRepository.findByCodigoAndUsuario_IdUsuario("123456", ID))
                    .thenReturn(Optional.of(codigo));

            assertThatThrownBy(() -> authService.verificarCodigo(EMAIL, "123456"))
                    .isInstanceOf(CodeExpiredException.class);
        }
    }

    // ─── restablecerPassword() ───────────────────────────────────────────────

    @Nested
    @DisplayName("restablecerPassword()")
    class RestablecerPassword {

        private void mockValidarCodigo() {
            CodigoRecuperacion codigo = CodigoRecuperacion.builder()
                    .codigo("123456")
                    .fechaExpiracion(LocalDateTime.now().plusMinutes(5))
                    .build();
            when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioBase()));
            when(codigoRecuperacionRepository.findByCodigoAndUsuario_IdUsuario("123456", ID))
                    .thenReturn(Optional.of(codigo));
        }

        @Test
        @DisplayName("Restablece la contraseña exitosamente")
        void restablecimientoExitoso() {
            mockValidarCodigo();
            when(passwordEncoder.matches("NuevaPass123!", "$encoded$")).thenReturn(false);
            when(passwordEncoder.encode("NuevaPass123!")).thenReturn("$nuevaEncoded$");

            authService.restablecerPassword(EMAIL, "123456", "NuevaPass123!");

            verify(usuarioRepository).save(any(Usuario.class));
            verify(codigoRecuperacionRepository).deleteByUsuario_IdUsuario(ID);
        }

        @Test
        @DisplayName("Lanza SamePasswordException si la nueva contraseña es igual a la anterior")
        void mismaContrasena() {
            mockValidarCodigo();
            // passwordEncoder devuelve true simulando que "MismaPass123!" es igual a "$encoded$"
            when(passwordEncoder.matches("MismaPass123!", "$encoded$")).thenReturn(true);

            assertThatThrownBy(() -> authService.restablecerPassword(EMAIL, "123456", "MismaPass123!"))
                    .isInstanceOf(SamePasswordException.class);

            verify(usuarioRepository, never()).save(any());
        }
    }
}
