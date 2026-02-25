package com.example.campaignserver.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TenantCreateRequest {

    @NotBlank
    private String name;

    private Integer monthlyCampaignLimit = 100;

    private Integer monthlyMessageLimit = 1_000_000;
}
