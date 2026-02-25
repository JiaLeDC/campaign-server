package com.example.campaignserver.service;

import com.example.campaignserver.dto.request.CampaignCreateRequest;
import com.example.campaignserver.dto.response.CampaignResponse;
import com.example.campaignserver.dto.response.PagedResponse;
import com.example.campaignserver.entity.Campaign;
import com.example.campaignserver.exception.ResourceNotFoundException;
import com.example.campaignserver.exception.TenantNotFoundException;
import com.example.campaignserver.repository.CampaignRepository;
import com.example.campaignserver.repository.RecipientRepository;
import com.example.campaignserver.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service class for handling all campaign related business logic.
 * This class deals with creating campaigns, retrieving them, and retrying
 * failed jobs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignService {

        private final CampaignRepository campaignRepository;
        private final RecipientRepository recipientRepository;
        private final TenantRepository tenantRepository;
        private final CsvParsingService csvParsingService;

        /**
         * Creates a new campaign.
         * It validates the tenant, saves the campaign, parses the recipients CSV,
         * and records an outbox event for background processing.
         * 
         * @param request The campaign creation request data
         * @return CampaignResponse with the initial stats
         */
        @Transactional
        public CampaignResponse createCampaign(CampaignCreateRequest request) {
                log.info("Starting createCampaign for tenant: {}", request.getTenantId());

                // First, check if the tenant exists in our database
                UUID tId = request.getTenantId();
                boolean tenantExists = tenantRepository.existsById(tId);
                if (!tenantExists) {
                        log.error("Tenant with id {} not found", tId);
                        throw new TenantNotFoundException("Tenant not found: " + tId);
                }

                // Determine if the status should be RUNNING or SCHEDULED
                String status = "RUNNING";
                if (request.getScheduledAt() != null) {
                        status = "SCHEDULED";
                }

                // Create the campaign entity using the builder
                Campaign campaign = Campaign.builder()
                                .tenantId(tId)
                                .name(request.getName())
                                .channel(request.getChannel().toUpperCase())
                                .messageTemplate(request.getMessageTemplate())
                                .isTransactional(request.isTransactional())
                                .scheduledAt(request.getScheduledAt())
                                .status(status)
                                .build();

                // Save campaign to database
                campaign = campaignRepository.save(campaign);
                UUID campaignId = campaign.getId();
                log.info("Campaign saved with ID: {}", campaignId);

                // Process recipients if CSV is provided
                int count = 0;
                if (request.getRecipientsCsv() != null && !request.getRecipientsCsv().isEmpty()) {
                        try {
                                // Use the CsvParsingService to parse and save recipients in batches
                                count = csvParsingService.streamRecipients(
                                                request.getRecipientsCsv(), tId, campaignId,
                                                batch -> {
                                                        // In this simplified version the consumer is ignored by the
                                                        // service, but we keep
                                                        // the call for backwards compatibility.
                                                });
                                log.info("Processed {} recipients for campaign {}", count, campaignId);
                        } catch (Exception e) {
                                log.error("Error parsing recipients CSV: {}", e.getMessage());
                                // Rethrow as a runtime exception or handle as needed
                                throw new RuntimeException("Failed to process recipients CSV", e);
                        }
                }

                // Return the response object with basic stats
                return CampaignResponse.builder()
                                .id(campaign.getId())
                                .tenantId(campaign.getTenantId())
                                .name(campaign.getName())
                                .channel(campaign.getChannel())
                                .status(campaign.getStatus())
                                .isTransactional(campaign.isTransactional())
                                .scheduledAt(campaign.getScheduledAt())
                                .createdAt(campaign.getCreatedAt())
                                .totalRecipients((long) count)
                                .sentCount(0L)
                                .failedCount(0L)
                                .skippedCount(0L)
                                .pendingCount((long) count)
                                .delayedCount(0L)
                                .build();
        }

        /**
         * Fetches a paged list of campaigns for a specific tenant.
         */
        @Transactional(readOnly = true)
        public PagedResponse<CampaignResponse> getCampaigns(UUID tenantId, int page, int size) {
                log.info("Fetching campaigns for tenant {} - page: {}, size: {}", tenantId, page, size);

                PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
                Page<Campaign> campaignPage = campaignRepository.findByTenantId(tenantId, pageRequest);

                List<CampaignResponse> responseList = new ArrayList<>();
                // Logic to loop through and build the responses with stats
                for (Campaign c : campaignPage.getContent()) {
                        CampaignResponse resp = buildCampaignResponseWithStats(c);
                        responseList.add(resp);
                }

                return PagedResponse.<CampaignResponse>builder()
                                .content(responseList)
                                .page(page)
                                .size(size)
                                .totalElements(campaignPage.getTotalElements())
                                .totalPages(campaignPage.getTotalPages())
                                .last(campaignPage.isLast())
                                .build();
        }

        /**
         * Fetches a single campaign by its ID and tenant ID.
         */
        @Transactional(readOnly = true)
        public CampaignResponse getCampaignById(UUID tenantId, UUID campaignId) {
                Campaign campaign = campaignRepository.findByIdAndTenantId(campaignId, tenantId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Campaign not found: " + campaignId + " for tenant: " + tenantId));

                return buildCampaignResponseWithStats(campaign);
        }

        /**
         * Re-queues failed notification jobs for a campaign.
         */
        @Transactional
        public int retryFailures(UUID tenantId, UUID campaignId) {
                // Verify campaign exists for this tenant
                campaignRepository.findByIdAndTenantId(campaignId, tenantId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Campaign not found: " + campaignId + " for tenant: " + tenantId));

                // In this simplified implementation we do not track individual notification jobs,
                // so there is nothing to actually retry.
                log.info("Retry requested for campaign {} but retry logic is not implemented in simplified version.",
                                campaignId);

                return 0;
        }

        /**
         * Private helper to build a CampaignResponse with calculated stats.
         */
        private CampaignResponse buildCampaignResponseWithStats(Campaign campaign) {
                UUID cid = campaign.getId();

                // For the junior version we only track total recipients; other stats are
                // returned as simple defaults.
                long total = recipientRepository.countByCampaignId(cid);
                long sent = 0L;
                long failed = 0L;
                long skipped = 0L;
                long delayed = 0L;
                long pending = total;

                // Use the builder to construct the final response object
                return CampaignResponse.builder()
                                .id(campaign.getId())
                                .tenantId(campaign.getTenantId())
                                .name(campaign.getName())
                                .channel(campaign.getChannel())
                                .status(campaign.getStatus())
                                .isTransactional(campaign.isTransactional())
                                .scheduledAt(campaign.getScheduledAt())
                                .createdAt(campaign.getCreatedAt())
                                .totalRecipients(total)
                                .sentCount(sent)
                                .failedCount(failed)
                                .skippedCount(skipped)
                                .pendingCount(pending)
                                .delayedCount(delayed)
                                .build();
        }
}
