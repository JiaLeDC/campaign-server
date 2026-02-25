package com.example.campaignserver.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class TenantResponse {

    private UUID id;
    private String name;
    private int monthlyCampaignLimit;
    private int monthlyMessageLimit;
    private Instant createdAt;
}
