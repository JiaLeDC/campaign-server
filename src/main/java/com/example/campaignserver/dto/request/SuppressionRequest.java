package com.example.campaignserver.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SuppressionRequest {

    @NotNull
    private UUID tenantId;

    @NotBlank
    private String recipientRef;

    @NotBlank
    private String channel;
}
