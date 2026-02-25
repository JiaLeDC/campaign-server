package com.example.campaignserver.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Very small in‑memory provider simulator used mainly for tests.
 *
 * Behaviour:
 * - Supports channels: EMAIL, SMS, PUSH
 * - Applies a simple per‑instance, per‑channel limit of 100 requests
 * (after that, returns RATE_LIMITED)
 * - For allowed requests, randomly fails a percentage of calls to simulate
 * provider instability.
 *
 * This is intentionally lightweight and is not wired into the main campaign
 * flow yet; it serves to demonstrate how provider behaviour and rate limiting
 * could be tested.
 */
@Slf4j
@Component
public class SimulatedNotificationProvider {

    private static final int MAX_REQUESTS_PER_CHANNEL = 100;

    private final Map<String, Integer> requestCounts = new HashMap<>();
    private final Random random = new Random();

    public ProviderResponse send(String channel, String destination, String message) {
        String normalizedChannel = channel == null ? "" : channel.toUpperCase();

        // Validate channel
        if (!normalizedChannel.equals("EMAIL")
                && !normalizedChannel.equals("SMS")
                && !normalizedChannel.equals("PUSH")) {
            return new ProviderResponse(false, "UNKNOWN_CHANNEL");
        }

        // Simple per‑channel rate limiting (per provider instance)
        int used = requestCounts.getOrDefault(normalizedChannel, 0);
        if (used >= MAX_REQUESTS_PER_CHANNEL) {
            return new ProviderResponse(false, "RATE_LIMITED");
        }
        requestCounts.put(normalizedChannel, used + 1);

        // Simulate provider failure with ~20% probability
        boolean fail = random.nextDouble() < 0.20;
        if (fail) {
            log.error("[SIMULATOR] FAILED to send {} to {}: PROVIDER_FAILURE", normalizedChannel, destination);
            return new ProviderResponse(false, "PROVIDER_FAILURE");
        }

        log.info("[SIMULATOR] SUCCESS sending {} to {}: \"{}\"", normalizedChannel, destination, message);
        return new ProviderResponse(true, null);
    }
}
