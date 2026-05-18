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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitHubOAuthServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GitHubOAuthService gitHubOAuthService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(gitHubOAuthService, "clientId", "test-github-client-id");
        ReflectionTestUtils.setField(gitHubOAuthService, "clientSecret", "test-github-client-secret");
        ReflectionTestUtils.setField(gitHubOAuthService, "redirectUri", "http://localhost:8080/auth/github/callback");
    }

    @Nested
    @DisplayName("getProviderName()")
    class GetProviderName {
        @Test
        @DisplayName("Retorna 'github'")
        void retornaNombreCorrecto() {
            assertThat(gitHubOAuthService.getProviderName()).isEqualTo("github");
        }
    }

    @Nested
    @DisplayName("getAuthorizationUrl()")
    class GetAuthorizationUrl {
        @Test
        @DisplayName("Construye la URL con client_id y scope necesarios")
        void construyeUrl() {
            String url = gitHubOAuthService.getAuthorizationUrl();
            assertThat(url).contains("https://github.com/login/oauth/authorize");
            assertThat(url).contains("client_id=test-github-client-id");
            assertThat(url).contains("redirect_uri=http://localhost:8080/auth/github/callback");
            assertThat(url).contains("scope=read:user%20user:email%20public_repo");
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
            body.put("access_token", "gho_access_token_123");

            ResponseEntity<Map<String, Object>> res = new ResponseEntity<>(body, HttpStatus.OK);
            when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), any(Class.class)))
                    .thenReturn((ResponseEntity) res);

            String token = gitHubOAuthService.exchangeCodeForToken("some-code");
            assertThat(token).isEqualTo("gho_access_token_123");
        }

        @Test
        @DisplayName("Falla si no existe access_token en la respuesta")
        void fallaSinAccessToken() {
            Map<String, Object> body = new HashMap<>(); // sin access_token
            ResponseEntity<Map<String, Object>> res = new ResponseEntity<>(body, HttpStatus.OK);

            when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), any(Class.class)))
                    .thenReturn((ResponseEntity) res);

            assertThatThrownBy(() -> gitHubOAuthService.exchangeCodeForToken("invalid-code"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("access_token");
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Nested
    @DisplayName("fetchUserInfo()")
    class FetchUserInfo {

        @Test
        @DisplayName("Mapea datos completos con email directo")
        void mapeaDatosCompletos() {
            // Mapeamos el /user HTTP call
            Map<String, Object> userBody = new HashMap<>();
            userBody.put("id", 12345);
            userBody.put("login", "octocat");
            userBody.put("email", "octocat@github.com"); // Email publico existe
            userBody.put("name", "The Octocat");
            userBody.put("avatar_url", "http://github.com/avatar.png");
            userBody.put("bio", "I am a cat");

            ResponseEntity<Map<String, Object>> userRes = new ResponseEntity<>(userBody, HttpStatus.OK);

            // Mapeamos el /user/repos HTTP call (vacio pero necesario mock)
            ResponseEntity<List<Map<String, Object>>> repoRes = new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);

            when(restTemplate.exchange(contains("/user"), eq(HttpMethod.GET), any(HttpEntity.class), eq((Class) Map.class)))
                    .thenReturn((ResponseEntity) userRes);
            when(restTemplate.exchange(contains("/user/repos"), eq(HttpMethod.GET), any(HttpEntity.class), eq((Class) List.class)))
                    .thenReturn((ResponseEntity) repoRes);

            OAuthUserInfo info = gitHubOAuthService.fetchUserInfo("gho_123");

            assertThat(info.getProveedor()).isEqualTo("github");
            assertThat(info.getProveedorUserId()).isEqualTo("12345");
            assertThat(info.getUsernameExterno()).isEqualTo("octocat");
            assertThat(info.getEmail()).isEqualTo("octocat@github.com");
            assertThat(info.getNombres()).isEqualTo("The");
            assertThat(info.getApellidos()).isEqualTo("Octocat");
            assertThat(info.getFotoUrl()).isEqualTo("http://github.com/avatar.png");
            assertThat(info.getTitularProfesional()).isEqualTo("I am a cat");
        }

        @Test
        @DisplayName("El correo es nulo, llamando al endpoint primario /user/emails")
        void usaEmailSecundarioEndpoint() {
            // /user HTTP call
            Map<String, Object> userBody = new HashMap<>();
            userBody.put("id", 12345);
            userBody.put("login", "octocat");
            // NO EMAIL

            ResponseEntity<Map<String, Object>> userRes = new ResponseEntity<>(userBody, HttpStatus.OK);

            // /user/emails HTTP call
            Map<String, Object> email1 = new HashMap<>();
            email1.put("email", "private@nomail.com");
            email1.put("primary", false);
            email1.put("verified", true);

            Map<String, Object> email2 = new HashMap<>();
            email2.put("email", "real@github.com");
            email2.put("primary", true);
            email2.put("verified", true);

            ResponseEntity<List<Map<String, Object>>> emailRes = new ResponseEntity<>(List.of(email1, email2), HttpStatus.OK);
            ResponseEntity<List<Map<String, Object>>> repoRes = new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);

            when(restTemplate.exchange(contains("api.github.com/user"), eq(HttpMethod.GET), any(HttpEntity.class), eq((Class) Map.class)))
                    .thenReturn((ResponseEntity) userRes);
            when(restTemplate.exchange(contains("/user/emails"), eq(HttpMethod.GET), any(HttpEntity.class), eq((Class) List.class)))
                    .thenReturn((ResponseEntity) emailRes);
            when(restTemplate.exchange(contains("/user/repos"), eq(HttpMethod.GET), any(HttpEntity.class), eq((Class) List.class)))
                    .thenReturn((ResponseEntity) repoRes);

            OAuthUserInfo info = gitHubOAuthService.fetchUserInfo("gho_123");

            assertThat(info.getEmail()).isEqualTo("real@github.com");
        }

        @Test
        @DisplayName("Fallo al obtener emails de fallback, retorna nulo y no rompe test")
        void falloFetchEmails() {
            Map<String, Object> userBody = new HashMap<>();
            userBody.put("id", 12345);
            userBody.put("login", "octocat");
            ResponseEntity<Map<String, Object>> userRes = new ResponseEntity<>(userBody, HttpStatus.OK);
            ResponseEntity<List<Map<String, Object>>> repoRes = new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);

            when(restTemplate.exchange(contains("api.github.com/user"), eq(HttpMethod.GET), any(HttpEntity.class), eq((Class) Map.class)))
                    .thenReturn((ResponseEntity) userRes);
            when(restTemplate.exchange(contains("/user/emails"), eq(HttpMethod.GET), any(), eq((Class) List.class)))
                    .thenThrow(new RestClientException("Forbidden"));
            when(restTemplate.exchange(contains("/user/repos"), eq(HttpMethod.GET), any(), eq((Class) List.class)))
                    .thenReturn((ResponseEntity) repoRes);

            OAuthUserInfo info = gitHubOAuthService.fetchUserInfo("token");
            assertThat(info.getEmail()).isNull();
        }

        @Test
        @DisplayName("Obtención de metadata (JSON) serializa los Top 6 repos correctamente")
        void formateaReposJson() {
            Map<String, Object> userBody = new HashMap<>();
            userBody.put("id", 1);
            userBody.put("login", "octocat");
            userBody.put("email", "octocat@github.com"); 

            // repos
            Map<String, Object> repo1 = new HashMap<>();
            repo1.put("name", "Repo1");
            repo1.put("html_url", "url1");
            repo1.put("stargazers_count", 50);

            Map<String, Object> repo2 = new HashMap<>();
            repo2.put("name", "RepoHighStars");
            repo2.put("html_url", "url2");
            repo2.put("stargazers_count", 200);

            ResponseEntity<Map<String, Object>> userRes = new ResponseEntity<>(userBody, HttpStatus.OK);
            ResponseEntity<List<Map<String, Object>>> repoRes = new ResponseEntity<>(List.of(repo1, repo2), HttpStatus.OK);

            when(restTemplate.exchange(contains("api.github.com/user"), eq(HttpMethod.GET), any(HttpEntity.class), eq((Class) Map.class)))
                    .thenReturn((ResponseEntity) userRes);
            when(restTemplate.exchange(contains("/user/repos"), eq(HttpMethod.GET), any(HttpEntity.class), eq((Class) List.class)))
                    .thenReturn((ResponseEntity) repoRes);

            OAuthUserInfo info = gitHubOAuthService.fetchUserInfo("gho_123");
            
            // Should be sorted descending by stars
            assertThat(info.getMetadatos()).contains("RepoHighStars");
            assertThat(info.getMetadatos()).contains("200");
            assertThat(info.getMetadatos()).startsWith("[{");
            assertThat(info.getMetadatos()).endsWith("}]");
            
            // Check RepoHighStars (200) comes before Repo1 (50)
            int indexHigh = info.getMetadatos().indexOf("RepoHighStars");
            int indexLow = info.getMetadatos().indexOf("Repo1");
            assertThat(indexHigh).isLessThan(indexLow);
        }
    }
}
