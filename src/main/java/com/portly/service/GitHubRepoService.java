package com.portly.service;

import com.portly.domain.entity.ProveedorOauth;
import com.portly.domain.repository.ProveedorOauthRepository;
import com.portly.dto.GitHubRepoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubRepoService {

    private final ProveedorOauthRepository proveedorRepository;
    private final RestTemplate restTemplate;

    /**
     * Obtiene los repositorios disponibles del usuario desde GitHub.
     */
    public List<GitHubRepoDto> listarReposDisponibles(UUID idUsuario) {
        // Buscar si el usuario tiene cuenta de GitHub vinculada
        ProveedorOauth githubAuth = proveedorRepository.findByUsuario_IdUsuarioAndNombreProveedor(idUsuario, "github")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usted no tiene una cuenta de GitHub vinculada"));

        String token = githubAuth.getClaveAccesoProveedor();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.set("X-GitHub-Api-Version", "2022-11-28");
            headers.set("Accept", "application/vnd.github.v3+json");

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    "https://api.github.com/user/repos?sort=updated&per_page=100&type=owner",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            List<Map<String, Object>> reposData = response.getBody();
            if (reposData == null || reposData.isEmpty()) {
                return Collections.emptyList();
            }

            return reposData.stream().map(repo -> {
                String fullName = (String) repo.get("full_name");
                
                // Extraer el array principal de lenguajes usando la URL de languages_url
                List<String> lenguajesDetectados = Collections.emptyList();
                String languagesUrl = (String) repo.get("languages_url");
                if (languagesUrl != null && !languagesUrl.isEmpty()) {
                    try {
                        ResponseEntity<Map<String, Object>> langResponse = restTemplate.exchange(
                            languagesUrl, HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<Map<String, Object>>() {}
                        );
                        Map<String, Object> langMap = langResponse.getBody();
                        if (langMap != null) {
                            lenguajesDetectados = langMap.keySet().stream().collect(Collectors.toList());
                        }
                    } catch (Exception e) {
                        log.warn("No se pudieron extraer lenguajes para el repo {}: {}", fullName, e.getMessage());
                    }
                }

                return GitHubRepoDto.builder()
                        .repoId(String.valueOf(repo.get("id")))
                        .nombre((String) repo.get("name"))
                        .nombreCompleto(fullName)
                        .descripcion((String) repo.get("description"))
                        .fechaCreacion((String) repo.get("created_at"))
                        .fechaActualizacion((String) repo.get("updated_at"))
                        .urlHtml((String) repo.get("html_url"))
                        .esPrivado((Boolean) repo.get("private"))
                        .stargazersCount(repo.get("stargazers_count") != null ? ((Number) repo.get("stargazers_count")).intValue() : 0)
                        .lenguajes(lenguajesDetectados)
                        .build();
            }).collect(Collectors.toList());

        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Token de GitHub expirado o revocado para usuario {}", idUsuario);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El acceso a GitHub ha expirado. Vuelva a vincular su cuenta.");
        } catch (Exception e) {
            log.error("Error obteniendo repositorios de GitHub para usuario {}", idUsuario, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudieron obtener los repositorios de GitHub.");
        }
    }
}
