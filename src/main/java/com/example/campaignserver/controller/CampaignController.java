package com.example.campaignserver.controller;

import com.example.campaignserver.dto.CampaignCreateRequest;
import com.example.campaignserver.dto.CampaignResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("campaign")
public class CampaignController {
    private static final Logger log = LoggerFactory.getLogger(CampaignController.class);


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CampaignResponse> createCampaign(
        @RequestParam("tenantId") UUID tenantId,
        @RequestParam("name")            String         name,
        @RequestParam("channel")         String         channel,       // EMAIL | SMS | PUSH
        @RequestParam("messageTemplate") String         messageTemplate,
        @RequestParam(value = "isTransactional", defaultValue = "false") boolean isTransactional,
        @RequestParam(value = "scheduledAt", required = false) Instant scheduledAt,
        @RequestParam(value = "recipients", required = false) MultipartFile recipientsCsv
    ) {
        log.info("[POST /campaigns] tenantId={} name={} channel={}", tenantId, name, channel);

        CampaignCreateRequest request = new CampaignCreateRequest(tenantId, name, channel, messageTemplate, isTransactional, scheduledAt, recipientsCsv);

        CampaignResponse response = new CampaignResponse();
        return ResponseEntity.accepted().body(response);
    }
}
