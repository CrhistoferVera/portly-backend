package com.portly.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractOAuthService implements OAuthProvider {

    protected final RestTemplate restTemplate;

    protected AbstractOAuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    protected String doExchangeCodeForToken(String tokenUrl, String code, String redirectUri,
            String clientId, String clientSecret) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                tokenUrl, new HttpEntity<>(body, headers), (Class<Map<String, Object>>) (Class<?>) Map.class);

        Map<String, Object> tokenResponse = response.getBody();
        if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
            throw new RuntimeException(getProviderName() + " no devolvió access_token");
        }
        return (String) tokenResponse.get("access_token");
    }
}
