package com.portly.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOAuthService implements OAuthProvider {

    @Override
    public String getProviderName() { return "google"; }

    private final RestTemplate restTemplate;

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    private static final String AUTH_URL     = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL    = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";


    public String getAuthorizationUrl() {
        return AUTH_URL
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&scope=openid%20profile%20email"
                + "&access_type=offline";
    }


    @SuppressWarnings("unchecked")
    public String exchangeCodeForToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                TOKEN_URL, new HttpEntity<>(body, headers), (Class<Map<String, Object>>) (Class<?>) Map.class);

        Map<String, Object> tokenResponse = response.getBody();
        if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
            log.error("Google no devolvió access_token. Respuesta: {}", tokenResponse);
            throw new RuntimeException("Google no devolvió access_token");
        }
        log.info("Token de Google obtenido correctamente");
        return (String) tokenResponse.get("access_token");
    }


    @SuppressWarnings("unchecked")
    public OAuthUserInfo fetchUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<Map<String, Object>> response;
        try {
            response = restTemplate.exchange(
                    USERINFO_URL, HttpMethod.GET, new HttpEntity<>(headers),
                    (Class<Map<String, Object>>) (Class<?>) Map.class);
        } catch (RestClientException ex) {
            log.error("Error al obtener datos de usuario de Google: {}", ex.getMessage());
            throw new RuntimeException("No se pudo obtener la información del usuario de Google", ex);
        }

        Map<String, Object> userData = Objects.requireNonNull(response.getBody(),
                "Google no devolvió datos del usuario");
        log.info("Datos de usuario obtenidos de Google: email={}", userData.get("email"));

        String sub       = (String) userData.get("sub");
        String email     = (String) userData.get("email");
        String nombres   = (String) userData.getOrDefault("given_name", "");
        String apellidos = (String) userData.getOrDefault("family_name", "");
        String fotoUrl   = (String) userData.getOrDefault("picture", null);

        return OAuthUserInfo.builder()
                .proveedor("google")
                .proveedorUserId(sub)
                .email(email)
                .nombres(nombres)
                .apellidos(apellidos)
                .fotoUrl(fotoUrl)
                .urlPerfil(null)
                .accessToken(accessToken)
                .build();
    }
}
