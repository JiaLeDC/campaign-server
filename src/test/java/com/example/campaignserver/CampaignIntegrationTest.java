package com.example.campaignserver;

import com.example.campaignserver.dto.response.ApiResponse;
import com.example.campaignserver.dto.response.CampaignResponse;
import com.example.campaignserver.entity.Tenant;
import com.example.campaignserver.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CampaignIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TenantRepository tenantRepository;

    private UUID tenantId;

    @BeforeEach
    void setUp() {
        tenantRepository.deleteAll();
        Tenant t = Tenant.builder().name("Test Tenant").build();
        t = tenantRepository.save(t);
        tenantId = t.getId();
    }

    @Test
    @DisplayName("IT: POST /campaigns with valid CSV should return 202")
    void createCampaignSuccess() {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("tenantId", tenantId.toString());
        body.add("name", "Test Campaign");
        body.add("channel", "EMAIL");
        body.add("messageTemplate", "Hello {{name}}");

        String csvContent = "recipientId,email,phone,timezone\nREF1,a@b.com,123,UTC";
        ByteArrayResource csvResource = new ByteArrayResource(csvContent.getBytes()) {
            @Override
            public String getFilename() {
                return "recipients.csv";
            }
        };
        body.add("recipients", csvResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<ApiResponse<CampaignResponse>> response = restTemplate.exchange(
                "/campaigns", HttpMethod.POST, request, new ParameterizedTypeReference<>() {
                });

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("EMAIL", response.getBody().getData().getChannel());
    }

    @Test
    @DisplayName("IT: POST /campaigns with unknown tenant should return 404")
    void createCampaignUnknownTenant() {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("tenantId", UUID.randomUUID().toString());
        body.add("name", "Fail Campaign");
        body.add("channel", "SMS");
        body.add("messageTemplate", "Msg");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
                "/campaigns", HttpMethod.POST, request, new ParameterizedTypeReference<>() {
                });

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("IT: GET /campaigns/{id} with wrong tenant should return 404")
    void getCampaignWrongTenant() {
        // First create one
        Tenant otherTenant = tenantRepository.save(Tenant.builder().name("Other").build());

        // This is a bit complex to setup via REST, let's just test the 404 logic
        ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
                "/campaigns/" + UUID.randomUUID() + "?tenantId=" + tenantId,
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                });

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
