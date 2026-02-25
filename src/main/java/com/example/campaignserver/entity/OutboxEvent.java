package com.example.campaignserver.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String aggregateType; // e.g., "CAMPAIGN"

    @Column(nullable = false)
    private UUID aggregateId; // The ID of the campaign

    @Column(nullable = false)
    private String eventType; // e.g., "CAMPAIGN_CREATED"

    @Column(nullable = false)
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED

    @Column(columnDefinition = "TEXT")
    private String payload; // JSON payload for the event

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    private Instant processedAt;
}
