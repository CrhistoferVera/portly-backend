package com.portly.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DenunciaAgrupadaResponse {

    private Long id;
    private String portfolioId;
    private String portfolioTitle;
    private String portfolioPublicUrl;
    private String ownerUserId;
    private String ownerUserName;
    private String ownerUserStatus;
    private String status;
    private List<ComplaintItemResponse> complaints;
    private RevisionResponse revision;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ComplaintItemResponse {
        private Long id;
        private String reason;
        private String description;
        private LocalDateTime createdAt;
        private String reportedBy;
        private String reporterName;
        private String reporterAvatar;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RevisionResponse {
        private String resultado;
        private LocalDateTime fecha;
        private String adminId;
    }
}
