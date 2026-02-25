package com.example.campaignserver.service;

import com.example.campaignserver.entity.Campaign;
import com.example.campaignserver.entity.Recipient;
import com.example.campaignserver.repository.NotificationJobRepository;
import com.example.campaignserver.rules.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignRuleTest {

    @Mock
    private NotificationJobRepository notificationJobRepository;

    @InjectMocks
    private DndWindowRule dndWindowRule;

    @InjectMocks
    private DeduplicationRule deduplicationRule;

    @Test
    @DisplayName("DND Rule: Should ALLOW transactional campaigns regardless of time")
    void dndRuleAllowsTransactional() {
        Campaign campaign = Campaign.builder()
                .channel("SMS")
                .isTransactional(true)
                .build();
        Recipient recipient = Recipient.builder().timezone("UTC").build();

        RuleResult result = dndWindowRule.evaluate(campaign, recipient);
        assertEquals(RuleResult.Action.ALLOW, result.getAction());
    }

    @Test
    @DisplayName("DND Rule: Should ALLOW EMAIL campaigns (only SMS/PUSH are restricted)")
    void dndRuleAllowsEmails() {
        Campaign campaign = Campaign.builder()
                .channel("EMAIL")
                .isTransactional(false)
                .build();
        Recipient recipient = Recipient.builder().timezone("UTC").build();

        RuleResult result = dndWindowRule.evaluate(campaign, recipient);
        assertEquals(RuleResult.Action.ALLOW, result.getAction());
    }

    @Test
    @DisplayName("Deduplication Rule: Should DISCARD if job was sent within 5 minutes")
    void deduplicationRuleDiscardsRecent() {
        UUID campaignId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        Campaign campaign = Campaign.builder().id(campaignId).build();
        Recipient recipient = Recipient.builder().id(recipientId).build();

        when(notificationJobRepository.countRecentlySentJobs(campaignId, recipientId)).thenReturn(1L);

        RuleResult result = deduplicationRule.evaluate(campaign, recipient);
        assertEquals(RuleResult.Action.DISCARD, result.getAction());
    }

    @Test
    @DisplayName("Deduplication Rule: Should ALLOW if no recent job found")
    void deduplicationRuleAllowsSuccessive() {
        UUID campaignId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        Campaign campaign = Campaign.builder().id(campaignId).build();
        Recipient recipient = Recipient.builder().id(recipientId).build();

        when(notificationJobRepository.countRecentlySentJobs(campaignId, recipientId)).thenReturn(0L);

        RuleResult result = deduplicationRule.evaluate(campaign, recipient);
        assertEquals(RuleResult.Action.ALLOW, result.getAction());
    }
}
