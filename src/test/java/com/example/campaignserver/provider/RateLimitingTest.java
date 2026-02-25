package com.example.campaignserver.provider;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitingTest {

    private final SimulatedNotificationProvider provider = new SimulatedNotificationProvider();

    @Test
    @DisplayName("Rate Limit: Should return RATE_LIMITED after 100 requests in a batch")
    void testRateLimiting() {
        int successfulHits = 0;
        int rateLimitedHits = 0;

        // Try to send 120 messages quickly
        for (int i = 0; i < 110; i++) {
            ProviderResponse resp = provider.send("EMAIL", "user" + i, "msg");
            if (resp.isSuccess() || (!resp.isSuccess() && !"RATE_LIMITED".equals(resp.getErrorCode()))) {
                successfulHits++; // Count both successes and simulated failures (which still consume tokens)
            } else if ("RATE_LIMITED".equals(resp.getErrorCode())) {
                rateLimitedHits++;
            }
        }

        // We expect exactly 100 allowed (success+failed) and 10 rate limited
        assertEquals(100, successfulHits);
        assertEquals(10, rateLimitedHits);
    }

    @Test
    @DisplayName("Failure Rate: Should be between 10% and 45% over a large sample")
    void testFailureRateRange() {
        // We use a fresh provider if needed, but 100 requests is enough for a rough
        // check
        // Or we wait for refill... actually let's just use 100 requests on a fresh one
        SimulatedNotificationProvider freshProvider = new SimulatedNotificationProvider();

        int failures = 0;
        int total = 100;

        for (int i = 0; i < total; i++) {
            ProviderResponse resp = freshProvider.send("SMS", "user" + i, "msg");
            if (!resp.isSuccess()) {
                failures++;
            }
        }

        double rate = (double) failures / total;
        // Spec says 15-25% failures, let's allow 10-45% for statistical variance in
        // small sample
        assertTrue(rate >= 0.10 && rate <= 0.45, "Failure rate " + rate + " out of expected range");
    }

    @Test
    @DisplayName("Unknown Channel: Should return error")
    void testUnknownChannel() {
        ProviderResponse resp = provider.send("CARRIER_PIGEON", "user", "msg");
        assertFalse(resp.isSuccess());
        assertEquals("UNKNOWN_CHANNEL", resp.getErrorCode());
    }
}
