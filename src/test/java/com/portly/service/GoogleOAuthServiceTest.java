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
class GoogleOAuthServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GoogleOAuthService googleOAuthService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(googleOAuthService, "clientId",     "test-client-id");
        ReflectionTestUtils.setField(googleOAuthService, "clientSecret", "test-client-secret");
        ReflectionTestUtils.setField(googleOAuthService, "redirectUri",  "http://localhost:8080/auth/google/callback");
    }

    // ─── getAuthorizationUrl() ────────────────────────────────────────────────

    @Nested
    @DisplayName("getAuthorizationUrl()")
    class GetAuthorizationUrl {

        @Test
        @DisplayName("Construye la URL con todos los parámetros requeridos")
        void urlContieneParametrosCorrectos() {
            String url = googleOAuthService.getAuthorizationUrl();

            assertThat(url).contains("response_type=code");
            assertThat(url).contains("client_id=test-client-id");
            assertThat(url).contains("redirect_uri=http://localhost:8080/auth/google/callback");
            assertThat(url).contains("scope=openid%20profile%20email");
            assertThat(url).contains("access_type=offline");
        }
    }

    // ─── exchangeCodeForToken() ───────────────────────────────────────────────

    @Nested
    @DisplayName("exchangeCodeForToken()")
    class ExchangeCodeForToken {

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Retorna el access_token cuando Google responde correctamente")
        void retornaAccessToken() {
            Map<String, Object> tokenBody = new HashMap<>();
            tokenBody.put("access_token", "ya29.google-token");

            ResponseEntity<Map<String, Object>> responseEntity =
                    new ResponseEntity<>(tokenBody, HttpStatus.OK);

            when(restTemplate.postForEntity(anyString(), any(), any(Class.class)))
                    .thenReturn((ResponseEntity) responseEntity);

            String token = googleOAuthService.exchangeCodeForToken("auth-code-123");

            assertThat(token).isEqualTo("ya29.google-token");
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Lanza RuntimeException si Google no devuelve access_token")
        void lanzaExcepcionSinAccessToken() {
            Map<String, Object> tokenBody = new HashMap<>();
            // Respuesta vacía, sin access_token

            ResponseEntity<Map<String, Object>> responseEntity =
                    new ResponseEntity<>(tokenBody, HttpStatus.OK);

            when(restTemplate.postForEntity(anyString(), any(), any(Class.class)))
                    .thenReturn((ResponseEntity) responseEntity);

            assertThatThrownBy(() -> googleOAuthService.exchangeCodeForToken("bad-code"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("access_token");
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Lanza RuntimeException si el body de respuesta es null")
        void lanzaExcepcionConBodyNull() {
            ResponseEntity<Map<String, Object>> responseEntity =
                    new ResponseEntity<>(null, HttpStatus.OK);

            when(restTemplate.postForEntity(anyString(), any(), any(Class.class)))
                    .thenReturn((ResponseEntity) responseEntity);

            assertThatThrownBy(() -> googleOAuthService.exchangeCodeForToken("bad-code"))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    // ─── fetchUserInfo() ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("fetchUserInfo()")
    class FetchUserInfo {

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Mapea correctamente los campos del usuario de Google")
        void mapeaCamposCorrectamente() {
            Map<String, Object> userData = new HashMap<>();
            userData.put("sub",         "google-uid-999");
            userData.put("email",       "juan@gmail.com");
            userData.put("given_name",  "Juan");
            userData.put("family_name", "Perez");
            userData.put("picture",     "https://photo.google.com/juan.jpg");

            ResponseEntity<Map<String, Object>> responseEntity =
                    new ResponseEntity<>(userData, HttpStatus.OK);

            when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(Class.class)))
                    .thenReturn((ResponseEntity) responseEntity);

            OAuthUserInfo info = googleOAuthService.fetchUserInfo("ya29.access-token");

            assertThat(info.getProveedor()).isEqualTo("google");
            assertThat(info.getProveedorUserId()).isEqualTo("google-uid-999");
            assertThat(info.getEmail()).isEqualTo("juan@gmail.com");
            assertThat(info.getNombres()).isEqualTo("Juan");
            assertThat(info.getApellidos()).isEqualTo("Perez");
            assertThat(info.getFotoUrl()).isEqualTo("https://photo.google.com/juan.jpg");
            assertThat(info.getAccessToken()).isEqualTo("ya29.access-token");
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Mapea campos opcionales como null cuando no vienen en la respuesta")
        void camposOpcionalesNulos() {
            Map<String, Object> userData = new HashMap<>();
            userData.put("sub",   "google-uid-000");
            userData.put("email", "anon@gmail.com");
            // sin given_name, family_name ni picture

            ResponseEntity<Map<String, Object>> responseEntity =
                    new ResponseEntity<>(userData, HttpStatus.OK);

            when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(Class.class)))
                    .thenReturn((ResponseEntity) responseEntity);

            OAuthUserInfo info = googleOAuthService.fetchUserInfo("ya29.token");

            assertThat(info.getNombres()).isEmpty();
            assertThat(info.getApellidos()).isEmpty();
            assertThat(info.getFotoUrl()).isNull();
        }
    }
}
