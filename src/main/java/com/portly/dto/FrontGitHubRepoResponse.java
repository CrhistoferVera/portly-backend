package com.portly.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO que coincide exactamente con la interfaz GitHubRepo del frontend.
 * Usa @JsonProperty para generar los nombres snake_case que el front espera.
 */
@Data
@Builder
public class FrontGitHubRepoResponse {

    private Integer id;

    private String name;

    @JsonProperty("full_name")
    private String fullName;

    private String description;

    @JsonProperty("html_url")
    private String htmlUrl;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("stargazers_count")
    private Integer stargazersCount;

    private List<String> languages;

    private List<String> topics;
}
