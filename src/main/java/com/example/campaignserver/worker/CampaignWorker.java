package com.example.campaignserver.worker;

import com.example.campaignserver.entity.Campaign;
import com.example.campaignserver.entity.OutboxEvent;
import com.example.campaignserver.entity.Recipient;
import com.example.campaignserver.provider.SimulatedNotificationProvider;
import com.example.campaignserver.repository.CampaignRepository;
import com.example.campaignserver.repository.OutboxRepository;
import com.example.campaignserver.repository.RecipientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CampaignWorker {

    private final OutboxRepository outboxRepository;
    private final CampaignRepository campaignRepository;
    private final RecipientRepository recipientRepository;
    private final SimulatedNotificationProvider notificationProvider;

    @Scheduled(fixedDelay = 5000) // Poll every 5 seconds
    public void processOutboxEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository.findByStatus("PENDING");
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Found {} pending outbox events to process", pendingEvents.size());
        for (OutboxEvent event : pendingEvents) {
            try {
                processEvent(event);
            } catch (Exception e) {
                log.error("Failed to process event {}: {}", event.getId(), e.getMessage());
                event.setStatus("FAILED");
                outboxRepository.save(event);
            }
        }
    }

    @Transactional
    public void processEvent(OutboxEvent event) {
        log.info("Processing event: {} for campaign: {}", event.getId(), event.getAggregateId());

        event.setStatus("PROCESSING");
        outboxRepository.saveAndFlush(event);

        Campaign campaign = campaignRepository.findById(event.getAggregateId()).orElse(null);
        if (campaign == null) {
            log.warn("Campaign {} not found for event {}", event.getAggregateId(), event.getId());
            event.setStatus("COMPLETED");
            event.setProcessedAt(Instant.now());
            outboxRepository.save(event);
            return;
        }

        // Fetch recipients for this campaign
        List<Recipient> recipients = recipientRepository.findByCampaignId(campaign.getId());
        log.info("Sending notifications to {} recipients for campaign {}", recipients.size(), campaign.getId());

        for (Recipient recipient : recipients) {
            // Simulated send
            notificationProvider.send(campaign.getChannel(),
                    recipient.getEmail() != null ? recipient.getEmail() : recipient.getPhone(),
                    campaign.getMessageTemplate());
        }

        // Mark campaign as completed
        campaign.setStatus("COMPLETED");
        campaignRepository.save(campaign);

        // Mark event as completed
        event.setStatus("COMPLETED");
        event.setProcessedAt(Instant.now());
        outboxRepository.save(event);

        log.info("Successfully processed campaign: {}", campaign.getId());
    }
}
