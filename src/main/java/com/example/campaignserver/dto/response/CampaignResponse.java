package com.example.campaignserver.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Campaign response DTO including aggregated delivery statistics and computed
 * deliveryRate.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CampaignResponse {

    private UUID id;
    private UUID tenantId;
    private String name;
    private String channel;
    private String status;
    private boolean isTransactional;
    private Instant scheduledAt;
    private Instant createdAt;

    // Aggregated stats
    private long totalRecipients;
    private long sentCount;
    private long failedCount;
    private long skippedCount;
    private long pendingCount;
    private long delayedCount;

    /**
     * Delivery rate = sentCount / totalRecipients * 100, rounded to 2 decimal
     * places.
     * Returns 0.0 if no recipients.
     */
    public double getDeliveryRate() {
        if (totalRecipients == 0)
            return 0.0;
        return Math.round((sentCount * 100.0 / totalRecipients) * 100.0) / 100.0;
    }
}
