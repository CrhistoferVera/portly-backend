package com.portly.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LinkedInOAuthService {

    private final RestTemplate restTemplate;

    @Value("${linkedin.client-id}")
    private String clientId;

    @Value("${linkedin.client-secret}")
    private String clientSecret;

    @Value("${linkedin.redirect-uri}")
    private String redirectUri;

    @Value("${linkedin.scope}")
    private String scope;

    private static final String AUTH_URL      = "https://www.linkedin.com/oauth/v2/authorization";
    private static final String TOKEN_URL     = "https://www.linkedin.com/oauth/v2/accessToken";
    private static final String USERINFO_URL  = "https://api.linkedin.com/v2/userinfo";


    public String getAuthorizationUrl() {
        return AUTH_URL
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&scope=" + scope.replace(" ", "%20");
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
            throw new RuntimeException("LinkedIn no devolvió access_token");
        }
        return (String) tokenResponse.get("access_token");
    }


    @SuppressWarnings("unchecked")
    public OAuthUserInfo fetchUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                USERINFO_URL, HttpMethod.GET, new HttpEntity<>(headers), (Class<Map<String, Object>>) (Class<?>) Map.class);

        Map<String, Object> userData = response.getBody();
        if (userData == null) {
            throw new RuntimeException("LinkedIn no devolvió datos del usuario");
        }

        String sub        = (String) userData.get("sub");             // ID único
        String email      = (String) userData.get("email");
        String nombres    = (String) userData.getOrDefault("given_name", "");
        String apellidos  = (String) userData.getOrDefault("family_name", "");
        String fotoUrl    = (String) userData.getOrDefault("picture", null);
        String titular    = (String) userData.getOrDefault("headline", null);

        return OAuthUserInfo.builder()
                .proveedor("linkedin")
                .proveedorUserId(sub)
                .email(email)
                .nombres(nombres)
                .apellidos(apellidos)
                .titularProfesional(titular)
                .fotoUrl(fotoUrl)
                .urlPerfil("https://www.linkedin.com/in/" + sub)
                .accessToken(accessToken)
                .build();
    }
}
