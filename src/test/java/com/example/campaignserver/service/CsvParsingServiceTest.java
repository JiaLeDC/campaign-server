package com.example.campaignserver.service;

import com.example.campaignserver.entity.Recipient;
import com.example.campaignserver.exception.CsvParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CsvParsingServiceTest {

    private final CsvParsingService csvParsingService = new CsvParsingService();

    @Test
    @DisplayName("CSV Test: Valid file with all fields should parse correctly")
    void parseValidCsv() {
        String content = "recipientId,email,phone,timezone\n" +
                "REF1,test@example.com,12345,America/New_York";
        MockMultipartFile file = new MockMultipartFile("recipients", "test.csv", "text/csv", content.getBytes());

        List<Recipient> allRecipients = new ArrayList<>();
        int count = csvParsingService.streamRecipients(file, UUID.randomUUID(), UUID.randomUUID(),
                allRecipients::addAll);

        assertEquals(1, count);
        assertEquals("REF1", allRecipients.get(0).getRecipientRef());
        assertEquals("test@example.com", allRecipients.get(0).getEmail());
        assertEquals("America/New_York", allRecipients.get(0).getTimezone());
    }

    @Test
    @DisplayName("CSV Test: Missing timezone should default to UTC")
    void parseCsvMissingTimezone() {
        String content = "recipientId,email,phone,timezone\n" +
                "REF2,test2@example.com,67890,";
        MockMultipartFile file = new MockMultipartFile("recipients", "test.csv", "text/csv", content.getBytes());

        List<Recipient> allRecipients = new ArrayList<>();
        csvParsingService.streamRecipients(file, UUID.randomUUID(), UUID.randomUUID(), allRecipients::addAll);

        assertEquals(1, allRecipients.size());
        assertEquals("UTC", allRecipients.get(0).getTimezone());
    }

    @Test
    @DisplayName("CSV Test: Bad header should throw exception")
    void parseCsvBadHeader() {
        String content = "wrong,header,columns\n" +
                "val1,val2,val3";
        MockMultipartFile file = new MockMultipartFile("recipients", "test.csv", "text/csv", content.getBytes());

        assertThrows(CsvParseException.class,
                () -> csvParsingService.streamRecipients(file, UUID.randomUUID(), UUID.randomUUID(), batch -> {
                }));
    }

    @Test
    @DisplayName("CSV Test: Empty file should return 0")
    void parseEmptyCsv() {
        MockMultipartFile file = new MockMultipartFile("recipients", "test.csv", "text/csv", new byte[0]);
        int count = csvParsingService.streamRecipients(file, UUID.randomUUID(), UUID.randomUUID(), batch -> {
        });
        assertEquals(0, count);
    }
}
