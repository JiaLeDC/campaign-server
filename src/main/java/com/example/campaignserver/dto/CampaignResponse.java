package com.example.campaignserver.dto;

import java.time.Instant;
import java.util.UUID;

public class CampaignResponse {

    private UUID id;
    private UUID tenantId;
    private String name;
    private String channel;
    private String status;
    private boolean isTransactional;
    private Instant scheduledAt;
    private Instant createdAt;

    private Long totalRecipients;
    private Long sentCount;
    private Long failedCount;
    private Long skippedCount;
    private Long pendingCount;

    // Default constructor (needed by Jackson for JSON serialization)
    public CampaignResponse() {}

    // All-args constructor (optional, can help if you want to manually construct)
    public CampaignResponse(UUID id, UUID tenantId, String name, String channel, String status,
                            boolean isTransactional, Instant scheduledAt, Instant createdAt,
                            Long totalRecipients, Long sentCount, Long failedCount,
                            Long skippedCount, Long pendingCount) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.channel = channel;
        this.status = status;
        this.isTransactional = isTransactional;
        this.scheduledAt = scheduledAt;
        this.createdAt = createdAt;
        this.totalRecipients = totalRecipients;
        this.sentCount = sentCount;
        this.failedCount = failedCount;
        this.skippedCount = skippedCount;
        this.pendingCount = pendingCount;
    }

    // Getters & Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isTransactional() { return isTransactional; }
    public void setTransactional(boolean transactional) { isTransactional = transactional; }

    public Instant getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Long getTotalRecipients() { return totalRecipients; }
    public void setTotalRecipients(Long totalRecipients) { this.totalRecipients = totalRecipients; }

    public Long getSentCount() { return sentCount; }
    public void setSentCount(Long sentCount) { this.sentCount = sentCount; }

    public Long getFailedCount() { return failedCount; }
    public void setFailedCount(Long failedCount) { this.failedCount = failedCount; }

    public Long getSkippedCount() { return skippedCount; }
    public void setSkippedCount(Long skippedCount) { this.skippedCount = skippedCount; }

    public Long getPendingCount() { return pendingCount; }
    public void setPendingCount(Long pendingCount) { this.pendingCount = pendingCount; }
}
