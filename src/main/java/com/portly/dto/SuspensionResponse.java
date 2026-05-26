package com.portly.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuspensionResponse {

    private Long id;
    private String userId;
    private String userName;
    private String motivo;
    private LocalDateTime fechaSuspension;
    private String adminId;
}
