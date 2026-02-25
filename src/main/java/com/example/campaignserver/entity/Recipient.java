package com.example.campaignserver.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "recipient")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recipient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "campaign_id", nullable = false)
    private UUID campaignId;

    @Column(name = "recipient_ref", nullable = false, length = 255)
    private String recipientRef;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "timezone", length = 100)
    @Builder.Default
    private String timezone = "UTC";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
