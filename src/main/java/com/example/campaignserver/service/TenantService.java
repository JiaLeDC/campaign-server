package com.example.campaignserver.service;

import com.example.campaignserver.dto.request.TenantCreateRequest;
import com.example.campaignserver.dto.response.TenantResponse;
import com.example.campaignserver.entity.Tenant;
import com.example.campaignserver.exception.TenantNotFoundException;
import com.example.campaignserver.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    @Transactional
    public TenantResponse createTenant(TenantCreateRequest request) {
        Tenant tenant = Tenant.builder()
                .name(request.getName())
                .monthlyCampaignLimit(
                        request.getMonthlyCampaignLimit() != null ? request.getMonthlyCampaignLimit() : 100)
                .monthlyMessageLimit(
                        request.getMonthlyMessageLimit() != null ? request.getMonthlyMessageLimit() : 1_000_000)
                .build();
        tenant = tenantRepository.save(tenant);
        log.info("[TenantService] Created tenant id={} name={}", tenant.getId(), tenant.getName());
        return toResponse(tenant);
    }

    @Transactional(readOnly = true)
    public List<TenantResponse> getAllTenants() {
        return tenantRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TenantResponse getTenantById(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + id));
        return toResponse(tenant);
    }

    private TenantResponse toResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .monthlyCampaignLimit(tenant.getMonthlyCampaignLimit())
                .monthlyMessageLimit(tenant.getMonthlyMessageLimit())
                .createdAt(tenant.getCreatedAt())
                .build();
    }
}
