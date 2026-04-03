package com.portly.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GitHubOAuthService {

    private final RestTemplate restTemplate;

    @Value("${github.client-id}")
    private String clientId;

    @Value("${github.client-secret}")
    private String clientSecret;

    @Value("${github.redirect-uri}")
    private String redirectUri;

    private static final String AUTH_URL   = "https://github.com/login/oauth/authorize";
    private static final String TOKEN_URL  = "https://github.com/login/oauth/access_token";
    private static final String API_BASE   = "https://api.github.com";


    public String getAuthorizationUrl() {
        return AUTH_URL
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&scope=read:user%20user:email%20public_repo";
    }


    @SuppressWarnings("unchecked")
    public String exchangeCodeForToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id",     clientId);
        body.add("client_secret", clientSecret);
        body.add("code",          code);
        body.add("redirect_uri",  redirectUri);

        ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                TOKEN_URL, new HttpEntity<>(body, headers), (Class<Map<String, Object>>) (Class<?>) Map.class);

        Map<String, Object> tokenResponse = response.getBody();
        if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
            throw new RuntimeException("GitHub no devolvió access_token: " + tokenResponse);
        }
        return (String) tokenResponse.get("access_token");
    }


    @SuppressWarnings("unchecked")
    public OAuthUserInfo fetchUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("X-GitHub-Api-Version", "2022-11-28");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>> userResp = restTemplate.exchange(
                API_BASE + "/user", HttpMethod.GET, entity, (Class<Map<String, Object>>) (Class<?>) Map.class);
        Map<String, Object> user = Objects.requireNonNull(userResp.getBody());

        String githubId  = String.valueOf(user.get("id"));
        String login     = (String) user.get("login");
        String email     = (String) user.get("email");
        String name      = (String) user.getOrDefault("name", login);
        String avatarUrl = (String) user.getOrDefault("avatar_url", null);
        String bio       = (String) user.getOrDefault("bio", null);

        if (email == null || email.isBlank()) {
            email = fetchPrimaryEmail(accessToken, headers);
        }

        String metadatosJson = fetchTopReposJson(accessToken, headers, 6);


        String nombres   = name;
        String apellidos = "";
        if (name != null && name.contains(" ")) {
            int idx     = name.indexOf(' ');
            nombres     = name.substring(0, idx);
            apellidos   = name.substring(idx + 1);
        }

        return OAuthUserInfo.builder()
                .proveedor("github")
                .proveedorUserId(githubId)
                .usernameExterno(login)
                .email(email)
                .nombres(nombres)
                .apellidos(apellidos)
                .titularProfesional(bio)
                .fotoUrl(avatarUrl)
                .urlPerfil("https://github.com/" + login)
                .accessToken(accessToken)
                .metadatos(metadatosJson)
                .build();
    }


    @SuppressWarnings("unchecked")
    private String fetchPrimaryEmail(String accessToken, HttpHeaders headers) {
        try {
            ResponseEntity<List<Map<String, Object>>> resp = restTemplate.exchange(
                    API_BASE + "/user/emails", HttpMethod.GET, new HttpEntity<>(headers),
                    (Class<List<Map<String, Object>>>) (Class<?>) List.class);
            List<Map<String, Object>> emails = resp.getBody();
            if (emails == null) return null;
            return emails.stream()
                    .filter(e -> Boolean.TRUE.equals(e.get("primary")) && Boolean.TRUE.equals(e.get("verified")))
                    .map(e -> (String) e.get("email"))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private String fetchTopReposJson(String accessToken, HttpHeaders headers, int top) {
        try {
            ResponseEntity<List<Map<String, Object>>> resp = restTemplate.exchange(
                    API_BASE + "/user/repos?sort=pushed&per_page=100&type=owner",
                    HttpMethod.GET, new HttpEntity<>(headers),
                    (Class<List<Map<String, Object>>>) (Class<?>) List.class);

            List<Map<String, Object>> repos = resp.getBody();
            if (repos == null || repos.isEmpty()) return null;

            List<Map<String, Object>> topRepos = repos.stream()
                    .sorted((a, b) -> {
                        int starsA = ((Number) a.getOrDefault("stargazers_count", 0)).intValue();
                        int starsB = ((Number) b.getOrDefault("stargazers_count", 0)).intValue();
                        return Integer.compare(starsB, starsA);
                    })
                    .limit(top)
                    .map(r -> {
                        Map<String, Object> repo = new java.util.HashMap<>();
                        repo.put("name",        r.getOrDefault("name", ""));
                        repo.put("description", r.get("description"));
                        repo.put("url",         r.getOrDefault("html_url", ""));
                        repo.put("stars",       r.getOrDefault("stargazers_count", 0));
                        repo.put("language",    r.get("language"));
                        return repo;
                    })
                    .collect(Collectors.toList());

            // Serializar manualmente a JSON sin depender de ObjectMapper como bean
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < topRepos.size(); i++) {
                Map<String, Object> r = topRepos.get(i);
                sb.append("{");
                sb.append("\"name\":").append(jsonStr(r.get("name"))).append(",");
                sb.append("\"description\":").append(jsonStr(r.get("description"))).append(",");
                sb.append("\"url\":").append(jsonStr(r.get("url"))).append(",");
                sb.append("\"stars\":").append(r.get("stars")).append(",");
                sb.append("\"language\":").append(jsonStr(r.get("language")));
                sb.append("}");
                if (i < topRepos.size() - 1) sb.append(",");
            }
            sb.append("]");
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private String jsonStr(Object value) {
        if (value == null) return "null";
        return "\"" + value.toString().replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
