package com.example.campaignserver.service;

import com.example.campaignserver.entity.Recipient;
import com.example.campaignserver.exception.CsvParseException;
import com.example.campaignserver.repository.RecipientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service to parse CSV files containing recipient information.
 * This service reads the file and saves recipients in batches to the database.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CsvParsingService {

    private final RecipientRepository recipientRepository;
    private static final int BATCH_SIZE = 500;

    /**
     * Parses the CSV file and saves recipients for a specific campaign.
     * 
     * @param file       The multipart file from the request
     * @param tenantId   The tenant owning the campaign
     * @param campaignId The campaign ID
     * @return The total number of recipients saved
     */
    public int parseAndSaveRecipients(
            MultipartFile file,
            UUID tenantId,
            UUID campaignId) {

        // Basic check if file is empty
        if (file == null || file.isEmpty()) {
            log.warn("CSV file is empty or null");
            return 0;
        }

        log.info("Starting CSV parsing for campaign: {}", campaignId);

        // Define the CSV format using the commons-csv library
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .build();

        int totalCount = 0;
        List<Recipient> currentBatch = new ArrayList<>();

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
                CSVParser parser = new CSVParser(reader, format)) {

            // Validate that the required headers are present
            validateCsvHeaders(parser);

            // Iterate through each record in the CSV
            for (CSVRecord record : parser) {
                // Skip lines that are completely empty
                if (checkIfLineIsBlank(record)) {
                    continue;
                }

                // Map the CSV row to our Recipient entity
                Recipient recipient = mapRecordToRecipient(record, tenantId, campaignId);
                currentBatch.add(recipient);
                totalCount++;

                // If batch size is reached, save to database and clear list
                if (currentBatch.size() >= BATCH_SIZE) {
                    recipientRepository.saveAll(currentBatch);
                    log.debug("Saved batch of {} recipients", currentBatch.size());
                    currentBatch.clear();
                }
            }

            // Save any remaining recipients in the last batch
            if (!currentBatch.isEmpty()) {
                recipientRepository.saveAll(currentBatch);
                log.debug("Saved final batch of {} recipients", currentBatch.size());
            }

        } catch (Exception e) {
            log.error("Error occurred while parsing CSV: {}", e.getMessage());
            throw new CsvParseException("Failed to parse CSV file: " + e.getMessage(), e);
        }

        log.info("Finished parsing CSV. Total recipients: {}", totalCount);
        return totalCount;
    }

    /**
     * Helper to validate CSV headers.
     */
    private void validateCsvHeaders(CSVParser parser) {
        List<String> headers = parser.getHeaderNames();
        if (!headers.contains("recipientId")) {
            log.error("CSV Missing 'recipientId' header. Found: {}", headers);
            throw new CsvParseException("Invalid CSV header. 'recipientId' column is required.");
        }
    }

    /**
     * Helper to check if a CSV record is blank.
     */
    private boolean checkIfLineIsBlank(CSVRecord record) {
        for (int i = 0; i < record.size(); i++) {
            if (record.get(i) != null && !record.get(i).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Maps a single CSV record to a Recipient entity.
     */
    private Recipient mapRecordToRecipient(CSVRecord record, UUID tenantId, UUID campaignId) {
        String recipientRef = record.get("recipientId");

        // Validation for required field
        if (recipientRef == null || recipientRef.trim().isEmpty()) {
            throw new CsvParseException("Found empty recipientId at record number " + record.getRecordNumber());
        }

        // Get optional fields
        String email = getCellValue(record, "email");
        String phone = getCellValue(record, "phone");
        String timezone = getCellValue(record, "timezone");

        // Use UTC as default timezone if not provided
        if (timezone == null || timezone.trim().isEmpty()) {
            timezone = "UTC";
        }

        return Recipient.builder()
                .tenantId(tenantId)
                .campaignId(campaignId)
                .recipientRef(recipientRef.trim())
                .email(email)
                .phone(phone)
                .timezone(timezone.trim())
                .build();
    }

    /**
     * Helper to safely get a cell value from a CSV record.
     */
    private String getCellValue(CSVRecord record, String columnName) {
        try {
            String value = record.get(columnName);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
            return null;
        } catch (IllegalArgumentException e) {
            // Column might not exist in the CSV
            return null;
        }
    }

    /**
     * Legacy method for backward compatibility if needed, but we now use
     * parseAndSaveRecipients.
     */
    public int streamRecipients(MultipartFile file, UUID tenantId, UUID campaignId,
            java.util.function.Consumer<List<Recipient>> batchConsumer) {
        // Redirection to the new method or implementing old one for compatibility
        return parseAndSaveRecipients(file, tenantId, campaignId);
    }
}
