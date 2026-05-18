package com.portly.service;

import com.portly.domain.entity.*;
import com.portly.domain.repository.*;
import com.portly.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PerfilUsuarioRepository perfilRepository;
    @Mock
    private ProveedorOauthRepository proveedorRepository;
    @Mock
    private EnlaceProfesionalRepository enlaceRepository;
    @Mock
    private ExperienciaLaboralRepository experienciaRepository;
    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private ProfileService profileService;

    private UUID userId;
    private Usuario usuario;
    private PerfilUsuario perfil;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        
        usuario = Usuario.builder()
                .idUsuario(userId)
                .email("test@example.com")
                .rol("USER")
                .estado("ACTIVO")
                .build();
                
        perfil = PerfilUsuario.builder()
                .idPerfilUsuario(1)
                .usuario(usuario)
                .nombre("Juan")
                .apellido("Perez")
                .build();
    }

    @Nested
    @DisplayName("getProfile()")
    class GetProfile {
        @Test
        @DisplayName("Retorna perfil completo con todas sus relaciones")
        void retornaPerfilCompleto() {
            when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
            when(perfilRepository.findByUsuario_IdUsuario(userId)).thenReturn(Optional.of(perfil));
            
            ProveedorOauth prov = ProveedorOauth.builder().nombreProveedor("github").build();
            when(proveedorRepository.findByUsuario_IdUsuario(userId)).thenReturn(List.of(prov));
            
            EnlaceProfesional enlace = EnlaceProfesional.builder().plataformaProfesional("Portfolio").esVisible(true).build();
            when(enlaceRepository.findByUsuario_IdUsuario(userId)).thenReturn(List.of(enlace));
            
            ExperienciaLaboral exp = ExperienciaLaboral.builder().empresa("Empresa X").build();
            when(experienciaRepository.findByUsuario_IdUsuario(userId)).thenReturn(List.of(exp));

            UsuarioProfileResponse response = profileService.getProfile(userId);

            assertThat(response.getIdUsuario()).isEqualTo(userId);
            assertThat(response.getNombre()).isEqualTo("Juan");
            assertThat(response.getProveedores()).hasSize(1);
            assertThat(response.getEnlaces()).hasSize(1);
            assertThat(response.getExperiencias()).hasSize(1);
        }

        @Test
        @DisplayName("Lanza excepcion si usuario no existe")
        void fallaUsuarioNoExiste() {
            when(usuarioRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> profileService.getProfile(userId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Usuario no encontrado");
        }
    }

    @Nested
    @DisplayName("actualizarPerfil()")
    class ActualizarPerfil {
        @Test
        @DisplayName("Actualiza correctamente los campos provistos")
        void actualizaCampos() {
            ActualizarPerfilRequest req = new ActualizarPerfilRequest();
            req.setNombre("Nuevo");
            req.setApellido("Ape");
            req.setPais("ES");

            when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
            when(perfilRepository.findByUsuario_IdUsuario(userId)).thenReturn(Optional.of(perfil));
            
            when(proveedorRepository.findByUsuario_IdUsuario(userId)).thenReturn(Collections.emptyList());
            when(enlaceRepository.findByUsuario_IdUsuario(userId)).thenReturn(Collections.emptyList());
            when(experienciaRepository.findByUsuario_IdUsuario(userId)).thenReturn(Collections.emptyList());

            UsuarioProfileResponse res = profileService.actualizarPerfil(userId, req);

            verify(perfilRepository).save(perfil);
            assertThat(perfil.getNombre()).isEqualTo("Nuevo");
            assertThat(perfil.getPais()).isEqualTo("ES");
            assertThat(res.getNombre()).isEqualTo("Nuevo");
        }
        
        @Test
        @DisplayName("Falla si perfil no existe")
        void perfilNoExiste() {
            ActualizarPerfilRequest req = new ActualizarPerfilRequest();
            when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
            when(perfilRepository.findByUsuario_IdUsuario(userId)).thenReturn(Optional.empty());
            
            assertThatThrownBy(() -> profileService.actualizarPerfil(userId, req))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Perfil no encontrado");
        }
    }

    @Nested
    @DisplayName("subirAvatar()")
    class SubirAvatar {
        @Test
        @DisplayName("Sube imagen a cloudinary y actualiza perfil")
        void subeImagenCorrectamente() throws IOException {
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "imageContent".getBytes());
            
            when(perfilRepository.findByUsuario_IdUsuario(userId)).thenReturn(Optional.of(perfil));
            when(cloudinaryService.uploadImage(any(), eq("portly/avatars"))).thenReturn("http://cloudinary.com/test.jpg");
            when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));

            UsuarioProfileResponse res = profileService.subirAvatar(userId, file);

            verify(perfilRepository).save(perfil);
            assertThat(perfil.getEnlaceFoto()).isEqualTo("http://cloudinary.com/test.jpg");
            assertThat(res.getEnlaceFoto()).isEqualTo("http://cloudinary.com/test.jpg");
        }

        @Test
        @DisplayName("Captura IOException de Cloudinary y retorna 500")
        void manejaCloudinaryException() throws IOException {
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "imageContent".getBytes());
            
            when(perfilRepository.findByUsuario_IdUsuario(userId)).thenReturn(Optional.of(perfil));
            when(cloudinaryService.uploadImage(any(), anyString())).thenThrow(new IOException("Simulated Cloudinary Error"));

            assertThatThrownBy(() -> profileService.subirAvatar(userId, file))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Error al subir imagen");
        }
    }

    @Nested
    @DisplayName("Experiencias Laborales")
    class ExperienciasLaborales {
        @Test
        @DisplayName("Agrega nueva experiencia")
        void agregaExperiencia() {
            ExperienciaRequest req = new ExperienciaRequest();
            req.setEmpresa("Empresa Test");
            
            when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));

            UsuarioProfileResponse.ExperienciaDto res = profileService.agregarExperiencia(userId, req);

            verify(experienciaRepository).save(any(ExperienciaLaboral.class));
            assertThat(res.getEmpresa()).isEqualTo("Empresa Test");
        }

        @Test
        @DisplayName("Actualiza experiencia valida del usuario")
        void actualizaExperiencia() {
            ExperienciaRequest req = new ExperienciaRequest();
            req.setEmpresa("Empresa Updated");
            
            ExperienciaLaboral exp = ExperienciaLaboral.builder().idExperienciaLaboral(1).usuario(usuario).build();
            when(experienciaRepository.findById(1)).thenReturn(Optional.of(exp));

            UsuarioProfileResponse.ExperienciaDto res = profileService.actualizarExperiencia(userId, 1, req);

            verify(experienciaRepository).save(exp);
            assertThat(res.getEmpresa()).isEqualTo("Empresa Updated");
        }

        @Test
        @DisplayName("Falla al actualizar experiencia de otro usuario (Ownership check)")
        void rechazaEdicionAjena() {
            Usuario otroUsuario = Usuario.builder().idUsuario(UUID.randomUUID()).build();
            ExperienciaLaboral expAjena = ExperienciaLaboral.builder().idExperienciaLaboral(2).usuario(otroUsuario).build();
            
            when(experienciaRepository.findById(2)).thenReturn(Optional.of(expAjena));

            assertThatThrownBy(() -> profileService.actualizarExperiencia(userId, 2, new ExperienciaRequest()))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("No tienes permiso");
        }

        @Test
        @DisplayName("Elimina experiencia propia")
        void eliminaExperiencia() {
            ExperienciaLaboral exp = ExperienciaLaboral.builder().idExperienciaLaboral(1).usuario(usuario).build();
            when(experienciaRepository.findById(1)).thenReturn(Optional.of(exp));

            profileService.eliminarExperiencia(userId, 1);

            verify(experienciaRepository).delete(exp);
        }
    }

    @Nested
    @DisplayName("Enlaces Profesionales")
    class EnlacesProfesionales {
        @Test
        @DisplayName("Agrega enlace visible por defecto")
        void agregaEnlace() {
            EnlaceRequest req = new EnlaceRequest();
            req.setPlataformaProfesional("Twitter");
            
            when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));

            UsuarioProfileResponse.EnlaceDto dto = profileService.agregarEnlace(userId, req);

            verify(enlaceRepository).save(any(EnlaceProfesional.class));
            assertThat(dto.getPlataformaProfesional()).isEqualTo("Twitter");
            assertThat(dto.getEsVisible()).isTrue();
        }

        @Test
        @DisplayName("Elimina enlace propio")
        void eliminaEnlace() {
            EnlaceProfesional enlace = EnlaceProfesional.builder().idEnlaceProfesional(5).usuario(usuario).build();
            when(enlaceRepository.findById(5)).thenReturn(Optional.of(enlace));

            profileService.eliminarEnlace(userId, 5);

            verify(enlaceRepository).delete(enlace);
        }
    }
}
