package com.example.campaignserver.dto;

import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

public class CampaignCreateRequest {

    private UUID tenantId;
    private String name;
    private String channel;           // EMAIL | SMS | PUSH
    private String messageTemplate;
    private boolean isTransactional;
    private Instant scheduledAt;       // null = send immediately
    private MultipartFile recipientsCsv;

    // Default constructor (needed for frameworks like Jackson)
    public CampaignCreateRequest() {}

    // All-args constructor
    public CampaignCreateRequest(UUID tenantId, String name, String channel, String messageTemplate,
                                 boolean isTransactional, Instant scheduledAt, MultipartFile recipientsCsv) {
        this.tenantId = tenantId;
        this.name = name;
        this.channel = channel;
        this.messageTemplate = messageTemplate;
        this.isTransactional = isTransactional;
        this.scheduledAt = scheduledAt;
        this.recipientsCsv = recipientsCsv;
    }

    // Getters & Setters
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getMessageTemplate() { return messageTemplate; }
    public void setMessageTemplate(String messageTemplate) { this.messageTemplate = messageTemplate; }

    public boolean isTransactional() { return isTransactional; }
    public void setTransactional(boolean transactional) { isTransactional = transactional; }

    public Instant getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; }

    public MultipartFile getRecipientsCsv() { return recipientsCsv; }
    public void setRecipientsCsv(MultipartFile recipientsCsv) { this.recipientsCsv = recipientsCsv; }

    // Optional: builder-style for convenience
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID tenantId;
        private String name;
        private String channel;
        private String messageTemplate;
        private boolean isTransactional;
        private Instant scheduledAt;
        private MultipartFile recipientsCsv;

        public Builder tenantId(UUID tenantId) { this.tenantId = tenantId; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder channel(String channel) { this.channel = channel; return this; }
        public Builder messageTemplate(String messageTemplate) { this.messageTemplate = messageTemplate; return this; }
        public Builder isTransactional(boolean isTransactional) { this.isTransactional = isTransactional; return this; }
        public Builder scheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; return this; }
        public Builder recipientsCsv(MultipartFile recipientsCsv) { this.recipientsCsv = recipientsCsv; return this; }

        public CampaignCreateRequest build() {
            return new CampaignCreateRequest(tenantId, name, channel, messageTemplate,
                isTransactional, scheduledAt, recipientsCsv);
        }
    }
}
