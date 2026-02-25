package com.example.campaignserver.dto.request;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for campaign creation — populated from multipart/form-data.
 */
@Data
@Builder
public class CampaignCreateRequest {

    private UUID tenantId;
    private String name;
    private String channel;
    private String messageTemplate;
    private boolean isTransactional;
    private Instant scheduledAt;
    private MultipartFile recipientsCsv;
}
