package com.portly.service;

import com.portly.domain.entity.PerfilUsuario;
import com.portly.domain.entity.ProveedorOauth;
import com.portly.domain.entity.Usuario;
import com.portly.domain.repository.EnlaceProfesionalRepository;
import com.portly.domain.repository.PerfilUsuarioRepository;
import com.portly.domain.repository.ProveedorOauthRepository;
import com.portly.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService - flujo OAuth Google (findOrCreate)")
class UsuarioServiceGoogleTest {

    @Mock private UsuarioRepository           usuarioRepository;
    @Mock private ProveedorOauthRepository    proveedorRepository;
    @Mock private PerfilUsuarioRepository     perfilRepository;
    @Mock private EnlaceProfesionalRepository enlaceRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private static final String PROVEEDOR    = "google";
    private static final String PROVEEDOR_ID = "google-uid-123";
    private static final String EMAIL        = "juan@gmail.com";
    private static final UUID   USER_ID      = UUID.randomUUID();

    private OAuthUserInfo googleInfo() {
        return OAuthUserInfo.builder()
                .proveedor(PROVEEDOR)
                .proveedorUserId(PROVEEDOR_ID)
                .email(EMAIL)
                .nombres("Juan")
                .apellidos("Perez")
                .fotoUrl("https://photo.google.com/juan.jpg")
                .accessToken("ya29.token")
                .build();
    }

    private Usuario usuarioGuardado() {
        return Usuario.builder()
                .idUsuario(USER_ID)
                .email(EMAIL)
                .rol("usuario")
                .estado("activo")
                .correoVerificado(true)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    // ─── Proveedor ya vinculado (usuario existente) ───────────────────────────

    @Nested
    @DisplayName("Proveedor OAuth ya vinculado")
    class ProveedorExistente {

        @Test
        @DisplayName("Retorna el usuario existente sin crear uno nuevo")
        void retornaUsuarioDelProveedorExistente() {
            Usuario usuario = usuarioGuardado();
            ProveedorOauth proveedor = ProveedorOauth.builder()
                    .usuario(usuario)
                    .nombreProveedor(PROVEEDOR)
                    .idUsuarioProveedor(PROVEEDOR_ID)
                    .claveAccesoProveedor("old-token")
                    .fechaCreacion(LocalDateTime.now())
                    .build();

            when(proveedorRepository.findByNombreProveedorAndIdUsuarioProveedor(PROVEEDOR, PROVEEDOR_ID))
                    .thenReturn(Optional.of(proveedor));
            when(proveedorRepository.save(any())).thenReturn(proveedor);
            when(usuarioRepository.save(any())).thenReturn(usuario);

            Usuario result = usuarioService.findOrCreate(googleInfo());

            assertThat(result.getEmail()).isEqualTo(EMAIL);
            verify(usuarioRepository, never()).findByEmail(anyString());
            verify(perfilRepository, never()).save(any());
        }

        @Test
        @DisplayName("Actualiza el access_token del proveedor en cada login")
        void actualizaAccessToken() {
            Usuario usuario = usuarioGuardado();
            ProveedorOauth proveedor = ProveedorOauth.builder()
                    .usuario(usuario)
                    .nombreProveedor(PROVEEDOR)
                    .idUsuarioProveedor(PROVEEDOR_ID)
                    .claveAccesoProveedor("old-token")
                    .fechaCreacion(LocalDateTime.now())
                    .build();

            when(proveedorRepository.findByNombreProveedorAndIdUsuarioProveedor(PROVEEDOR, PROVEEDOR_ID))
                    .thenReturn(Optional.of(proveedor));
            ArgumentCaptor<ProveedorOauth> captor = ArgumentCaptor.forClass(ProveedorOauth.class);
            when(proveedorRepository.save(captor.capture())).thenReturn(proveedor);
            when(usuarioRepository.save(any())).thenReturn(usuario);

            usuarioService.findOrCreate(googleInfo());

            assertThat(captor.getValue().getClaveAccesoProveedor()).isEqualTo("ya29.token");
        }
    }

    // ─── Email no existe → crea usuario nuevo ────────────────────────────────

    @Nested
    @DisplayName("Email nuevo - creación de usuario")
    class UsuarioNuevo {

        @Test
        @DisplayName("Crea usuario, perfil y proveedor cuando el email no existe")
        void creaUsuarioYPerfil() {
            when(proveedorRepository.findByNombreProveedorAndIdUsuarioProveedor(PROVEEDOR, PROVEEDOR_ID))
                    .thenReturn(Optional.empty());
            when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            Usuario nuevoUsuario = usuarioGuardado();
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(nuevoUsuario);
            when(perfilRepository.save(any(PerfilUsuario.class))).thenReturn(new PerfilUsuario());
            when(proveedorRepository.save(any(ProveedorOauth.class))).thenAnswer(i -> i.getArgument(0));

            Usuario result = usuarioService.findOrCreate(googleInfo());

            assertThat(result.getEmail()).isEqualTo(EMAIL);
            verify(perfilRepository).save(any(PerfilUsuario.class));
            verify(proveedorRepository).save(any(ProveedorOauth.class));
        }

        @Test
        @DisplayName("El usuario creado tiene correoVerificado=true y rol=usuario")
        void atributosDelNuevoUsuario() {
            when(proveedorRepository.findByNombreProveedorAndIdUsuarioProveedor(PROVEEDOR, PROVEEDOR_ID))
                    .thenReturn(Optional.empty());
            when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            when(usuarioRepository.save(captor.capture())).thenAnswer(inv -> {
                Usuario u = inv.getArgument(0);
                // simula que JPA asigna el ID
                return Usuario.builder()
                        .idUsuario(USER_ID)
                        .email(u.getEmail())
                        .rol(u.getRol())
                        .estado(u.getEstado())
                        .correoVerificado(u.getCorreoVerificado())
                        .fechaCreacion(u.getFechaCreacion())
                        .build();
            });
            when(perfilRepository.save(any())).thenReturn(new PerfilUsuario());
            when(proveedorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            usuarioService.findOrCreate(googleInfo());

            Usuario capturado = captor.getAllValues().get(0);
            assertThat(capturado.getCorreoVerificado()).isTrue();
            assertThat(capturado.getRol()).isEqualTo("usuario");
            assertThat(capturado.getEstado()).isEqualTo("activo");
        }
    }

    // ─── Email ya existe (sin proveedor) → vincula proveedor ─────────────────

    @Nested
    @DisplayName("Email existente sin proveedor vinculado")
    class EmailExistenteNuevoProveedor {

        @Test
        @DisplayName("Vincula el proveedor Google al usuario existente sin crear uno nuevo")
        void vinculaProveedorAUsuarioExistente() {
            Usuario usuarioExistente = usuarioGuardado();
            PerfilUsuario perfil = PerfilUsuario.builder()
                    .usuario(usuarioExistente)
                    .nombre("Juan")
                    .fechaActualizacion(LocalDateTime.now())
                    .build();

            when(proveedorRepository.findByNombreProveedorAndIdUsuarioProveedor(PROVEEDOR, PROVEEDOR_ID))
                    .thenReturn(Optional.empty());
            when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioExistente));
            when(perfilRepository.findByUsuario_IdUsuario(USER_ID)).thenReturn(Optional.of(perfil));
            when(perfilRepository.save(any())).thenReturn(perfil);
            when(proveedorRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(usuarioRepository.save(any())).thenReturn(usuarioExistente);

            Usuario result = usuarioService.findOrCreate(googleInfo());

            assertThat(result.getIdUsuario()).isEqualTo(USER_ID);
            // No se creó un usuario nuevo
            verify(usuarioRepository, times(1)).save(any());
        }
    }

    // ─── linkProviderToUser() - conflicto ─────────────────────────────────────

    @Nested
    @DisplayName("linkProviderToUser()")
    class LinkProvider {

        @Test
        @DisplayName("Lanza excepción si el proveedor ya está vinculado a otro usuario")
        void lanzaExcepcionSiProveedorYaVinculado() {
            UUID otroUserId = UUID.randomUUID();
            Usuario otroUsuario = Usuario.builder().idUsuario(otroUserId).build();
            ProveedorOauth proveedorDeOtro = ProveedorOauth.builder()
                    .usuario(otroUsuario)
                    .nombreProveedor(PROVEEDOR)
                    .idUsuarioProveedor(PROVEEDOR_ID)
                    .build();

            when(usuarioRepository.findById(USER_ID)).thenReturn(Optional.of(usuarioGuardado()));
            when(proveedorRepository.findByNombreProveedorAndIdUsuarioProveedor(PROVEEDOR, PROVEEDOR_ID))
                    .thenReturn(Optional.of(proveedorDeOtro));

            assertThatThrownBy(() -> usuarioService.linkProviderToUser(USER_ID, googleInfo()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("ya está vinculada a otro usuario");
        }

        @Test
        @DisplayName("Actualiza tokens si el proveedor ya pertenece al mismo usuario")
        void actualizaTokensSiMismoUsuario() {
            Usuario usuario = usuarioGuardado();
            ProveedorOauth propio = ProveedorOauth.builder()
                    .usuario(usuario)
                    .nombreProveedor(PROVEEDOR)
                    .idUsuarioProveedor(PROVEEDOR_ID)
                    .claveAccesoProveedor("old-token")
                    .fechaCreacion(LocalDateTime.now())
                    .build();

            when(usuarioRepository.findById(USER_ID)).thenReturn(Optional.of(usuario));
            when(proveedorRepository.findByNombreProveedorAndIdUsuarioProveedor(PROVEEDOR, PROVEEDOR_ID))
                    .thenReturn(Optional.of(propio));
            when(proveedorRepository.save(any())).thenReturn(propio);

            usuarioService.linkProviderToUser(USER_ID, googleInfo());

            assertThat(propio.getClaveAccesoProveedor()).isEqualTo("ya29.token");
            verify(proveedorRepository).save(propio);
        }
    }
}
