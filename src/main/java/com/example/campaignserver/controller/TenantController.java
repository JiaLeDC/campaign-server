package com.example.campaignserver.controller;

import com.example.campaignserver.dto.request.TenantCreateRequest;
import com.example.campaignserver.dto.response.ApiResponse;
import com.example.campaignserver.dto.response.TenantResponse;
import com.example.campaignserver.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    public ResponseEntity<ApiResponse<TenantResponse>> createTenant(
            @Valid @RequestBody TenantCreateRequest request) {
        log.info("[POST /tenants] name={}", request.getName());
        TenantResponse resp = tenantService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Tenant created", resp));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TenantResponse>>> getAllTenants() {
        return ResponseEntity.ok(ApiResponse.ok(tenantService.getAllTenants()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantResponse>> getTenantById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(tenantService.getTenantById(id)));
    }
}
