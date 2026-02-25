package com.example.campaignserver.repository;

import com.example.campaignserver.entity.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, UUID> {

    Page<Campaign> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<Campaign> findByIdAndTenantId(UUID id, UUID tenantId);

    @Query("SELECT c FROM Campaign c WHERE c.status = 'RUNNING'")
    List<Campaign> findRunningCampaigns();

    /**
     * Finds SCHEDULED campaigns whose scheduledAt has passed — time to activate
     * them.
     */
    @Query("SELECT c FROM Campaign c WHERE c.status = 'SCHEDULED' AND c.scheduledAt <= :now")
    List<Campaign> findScheduledCampaignsDue(@Param("now") Instant now);

    @Modifying
    @Query("UPDATE Campaign c SET c.status = :status WHERE c.id = :id")
    void updateStatus(@Param("id") UUID id, @Param("status") String status);

    /**
     * Count campaigns created in the current month for credit-check rule.
     * Uses nativeQuery = true because JPQL FUNCTION() for date_trunc is
     * non-standard.
     */
    @Query(value = "SELECT COUNT(*) FROM campaign " +
            "WHERE tenant_id = :tenantId " +
            "AND date_trunc('month', created_at) = date_trunc('month', NOW())", nativeQuery = true)
    long countCampaignsThisMonth(@Param("tenantId") UUID tenantId);
}
