package com.example.campaignserver.config;

import com.example.campaignserver.entity.Tenant;
import com.example.campaignserver.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds data on startup if not already present.
 * Creates a single demo tenant that integration tests and manual testing can
 * use.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final TenantRepository tenantRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (tenantRepository.count() == 0) {
            Tenant demo = Tenant.builder()
                    .name("Demo Tenant")
                    .monthlyCampaignLimit(100)
                    .monthlyMessageLimit(1_000_000)
                    .build();
            tenantRepository.save(demo);
            log.info("[DataSeeder] Created demo tenant id={}", demo.getId());
        } else {
            log.info("[DataSeeder] Demo tenant already exists, skipping seed");
        }
    }
}
