package com.example.campaignserver.controller;

import com.example.campaignserver.dto.request.CampaignCreateRequest;
import com.example.campaignserver.dto.response.ApiResponse;
import com.example.campaignserver.dto.response.CampaignResponse;
import com.example.campaignserver.dto.response.PagedResponse;
import com.example.campaignserver.service.CampaignService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

/**
 * Controller class for Campaign related endpoints.
 * This class handles the HTTP requests and calls the service layer.
 */
@Slf4j
@RestController
@RequestMapping("/campaigns")
@RequiredArgsConstructor
@Validated
public class CampaignController {

    private final CampaignService campaignService;

    /**
     * Endpoint for creating a new campaign.
     * It accepts various parameters as part of a multipart form.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CampaignResponse>> createCampaign(
            @RequestParam("tenantId") UUID tenantId,
            @RequestParam("name") String name,
            @RequestParam("channel") String channel,
            @RequestParam("messageTemplate") String messageTemplate,
            @RequestParam(value = "isTransactional", defaultValue = "false") boolean isTransactional,
            @RequestParam(value = "scheduledAt", required = false) Instant scheduledAt,
            @RequestParam(value = "recipients", required = false) MultipartFile recipientsCsv) {

        log.info("Received request to create campaign. Tenant: {}, Name: {}, Channel: {}", tenantId, name, channel);

        // Map the parameters to a request DTO
        CampaignCreateRequest request = CampaignCreateRequest.builder()
                .tenantId(tenantId)
                .name(name)
                .channel(channel.toUpperCase())
                .messageTemplate(messageTemplate)
                .isTransactional(isTransactional)
                .scheduledAt(scheduledAt)
                .recipientsCsv(recipientsCsv)
                .build();

        try {
            // Call service to handle the heavy lifting
            CampaignResponse response = campaignService.createCampaign(request);
            log.info("Campaign created successfully for tenant {}", tenantId);

            // Build the success response
            ApiResponse<CampaignResponse> apiResponse = ApiResponse.ok("Campaign created and queued for processing",
                    response);
            return new ResponseEntity<>(apiResponse, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            log.error("Failed to create campaign: {}", e.getMessage());
            // In a real junior scenario, they might just throw or return internal error
            throw e;
        }
    }

    /**
     * Endpoint to list campaigns for a tenant with pagination support.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<CampaignResponse>>> getCampaigns(
            @RequestParam @NotNull UUID tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Getting list of campaigns for tenant {} - page {}, size {}", tenantId, page, size);

        PagedResponse<CampaignResponse> result = campaignService.getCampaigns(tenantId, page, size);

        ApiResponse<PagedResponse<CampaignResponse>> response = ApiResponse.ok(result);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to get details of a specific campaign by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CampaignResponse>> getCampaignById(
            @PathVariable UUID id,
            @RequestParam @NotNull UUID tenantId) {

        log.info("Fetching details for campaign ID: {} (Tenant: {})", id, tenantId);

        CampaignResponse response = campaignService.getCampaignById(tenantId, id);

        if (response == null) {
            log.warn("No campaign found with ID {} for tenant {}", id, tenantId);
            // This case might be handled by service throwing exception, but junior might
            // add this
        }

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Endpoint to manually trigger a retry for failed jobs in a campaign.
     * Re-queues all FAILED jobs so they can be processed again.
     */
    @PostMapping("/{id}/retry-failures")
    public ResponseEntity<ApiResponse<String>> retryFailures(
            @PathVariable UUID id,
            @RequestParam @NotNull UUID tenantId) {

        log.info("Retry requested for failed jobs - Campaign: {}, Tenant: {}", id, tenantId);

        int count = campaignService.retryFailures(tenantId, id);
        log.info("Requeued {} jobs for campaign {}", count, id);

        String msg = "Requeued " + count + " failed jobs for campaign " + id;
        return ResponseEntity.ok(ApiResponse.ok(msg, null));
    }
}
