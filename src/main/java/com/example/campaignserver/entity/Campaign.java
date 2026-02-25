package com.example.campaignserver.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "campaign")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "channel", nullable = false, length = 10)
    private String channel;

    @Column(name = "message_template", nullable = false, columnDefinition = "TEXT")
    private String messageTemplate;

    @Column(name = "is_transactional")
    @Builder.Default
    private boolean isTransactional = false;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "SCHEDULED";

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
