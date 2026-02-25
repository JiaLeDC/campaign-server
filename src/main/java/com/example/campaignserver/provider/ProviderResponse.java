package com.example.campaignserver.provider;

/**
 * Simple value object representing the result of a simulated provider call.
 *
 * This is intentionally minimal for the take‑home:
 * - success: whether the provider accepted the request
 * - errorCode: optional error identifier (e.g. RATE_LIMITED, UNKNOWN_CHANNEL)
 */
public class ProviderResponse {

    private final boolean success;
    private final String errorCode;

    public ProviderResponse(boolean success, String errorCode) {
        this.success = success;
        this.errorCode = errorCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

