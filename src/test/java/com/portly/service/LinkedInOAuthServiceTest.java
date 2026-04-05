package com.portly.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LinkedInOAuthServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private LinkedInOAuthService linkedInOAuthService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(linkedInOAuthService, "clientId", "lk_client_id");
        ReflectionTestUtils.setField(linkedInOAuthService, "clientSecret", "lk_client_secret");
        ReflectionTestUtils.setField(linkedInOAuthService, "redirectUri", "http://localhost/callback");
        ReflectionTestUtils.setField(linkedInOAuthService, "scope", "openid profile email");
    }

    @Nested
    @DisplayName("getProviderName()")
    class GetProviderName {
        @Test
        @DisplayName("Retorna 'linkedin'")
        void retornaNombreCorrecto() {
            assertThat(linkedInOAuthService.getProviderName()).isEqualTo("linkedin");
        }
    }

    @Nested
    @DisplayName("getAuthorizationUrl()")
    class GetAuthorizationUrl {
        @Test
        @DisplayName("Construye la URL con parametros encodeados y requeridos")
        void construyeUrl() {
            String url = linkedInOAuthService.getAuthorizationUrl();
            assertThat(url).contains("https://www.linkedin.com/oauth/v2/authorization");
            assertThat(url).contains("response_type=code");
            assertThat(url).contains("client_id=lk_client_id");
            assertThat(url).contains("redirect_uri=http://localhost/callback");
            assertThat(url).contains("scope=openid%20profile%20email");
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Nested
    @DisplayName("exchangeCodeForToken()")
    class ExchangeCodeForToken {
        @Test
        @DisplayName("Extrae access_token del Map devuelto")
        void extraeAccessToken() {
            Map<String, Object> body = new HashMap<>();
            body.put("access_token", "linkedin_access_token_abc");

            ResponseEntity<Map<String, Object>> res = new ResponseEntity<>(body, HttpStatus.OK);
            when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), any(Class.class)))
                    .thenReturn((ResponseEntity) res);

            String token = linkedInOAuthService.exchangeCodeForToken("code123");
            assertThat(token).isEqualTo("linkedin_access_token_abc");
        }

        @Test
        @DisplayName("Falla si no existe access_token")
        void fallaSinAccessToken() {
            Map<String, Object> body = new HashMap<>(); // sin access_token
            ResponseEntity<Map<String, Object>> res = new ResponseEntity<>(body, HttpStatus.OK);

            when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), any(Class.class)))
                    .thenReturn((ResponseEntity) res);

            assertThatThrownBy(() -> linkedInOAuthService.exchangeCodeForToken("code123"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("LinkedIn no devolvió access_token");
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Nested
    @DisplayName("fetchUserInfo()")
    class FetchUserInfo {

        @Test
        @DisplayName("Extrae datos del response normal")
        void extraeDatosExito() {
            Map<String, Object> userData = new HashMap<>();
            userData.put("sub", "lk_sub_0101");
            userData.put("email", "john@linkedin.com");
            userData.put("given_name", "John");
            userData.put("family_name", "Doe");
            userData.put("picture", "https://linkedin.com/pic.jpg");

            ResponseEntity<Map<String, Object>> res = new ResponseEntity<>(userData, HttpStatus.OK);
            when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq((Class) Map.class)))
                    .thenReturn((ResponseEntity) res);

            OAuthUserInfo info = linkedInOAuthService.fetchUserInfo("token");

            assertThat(info.getProveedor()).isEqualTo("linkedin");
            assertThat(info.getProveedorUserId()).isEqualTo("lk_sub_0101");
            assertThat(info.getEmail()).isEqualTo("john@linkedin.com");
            assertThat(info.getNombres()).isEqualTo("John");
            assertThat(info.getApellidos()).isEqualTo("Doe");
            assertThat(info.getFotoUrl()).isEqualTo("https://linkedin.com/pic.jpg");
            assertThat(info.getUrlPerfil()).isNull();
            assertThat(info.getAccessToken()).isEqualTo("token");
        }

        @Test
        @DisplayName("Mapea correctamnte null a campos vacios opcionales")
        void mapeaCamposNulos() {
            Map<String, Object> userData = new HashMap<>();
            userData.put("sub", "lk_001");
            // No viene ni given_name, family_name o picture.

            ResponseEntity<Map<String, Object>> res = new ResponseEntity<>(userData, HttpStatus.OK);
            when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq((Class) Map.class)))
                    .thenReturn((ResponseEntity) res);

            OAuthUserInfo info = linkedInOAuthService.fetchUserInfo("token");
            assertThat(info.getNombres()).isEmpty();
            assertThat(info.getApellidos()).isEmpty();
            assertThat(info.getFotoUrl()).isNull();
        }

        @Test
        @DisplayName("Falla si LinkedIn body es nulo")
        void fallaSiNulo() {
            ResponseEntity<Map<String, Object>> res = new ResponseEntity<>(null, HttpStatus.OK);
            when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq((Class) Map.class)))
                    .thenReturn((ResponseEntity) res);

            assertThatThrownBy(() -> linkedInOAuthService.fetchUserInfo("token"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("LinkedIn no devolvió datos del usuario");
        }
    }
}
