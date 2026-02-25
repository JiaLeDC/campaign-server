package com.example.campaignserver.repository;

import com.example.campaignserver.entity.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecipientRepository extends JpaRepository<Recipient, UUID> {

    List<Recipient> findByCampaignId(UUID campaignId);

    long countByCampaignId(UUID campaignId);

    /**
     * Count messages sent this month for the given tenant (for rate limiting /
     * credit check).
     * Uses nativeQuery for PostgreSQL date_trunc.
     */
    @Query(value = "SELECT COUNT(*) FROM recipient " +
            "WHERE tenant_id = :tenantId " +
            "AND date_trunc('month', created_at) = date_trunc('month', NOW())", nativeQuery = true)
    long countRecipientsThisMonth(@Param("tenantId") UUID tenantId);
}
