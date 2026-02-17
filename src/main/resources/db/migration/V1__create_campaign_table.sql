-- 1. Tenant
CREATE TABLE tenant (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        name VARCHAR(255) NOT NULL,
                        monthly_campaign_limit INT DEFAULT 100,
                        monthly_message_limit INT DEFAULT 1000000,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Campaign (tenant-scoped)
CREATE TABLE campaign (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          tenant_id UUID NOT NULL REFERENCES tenant(id),
                          name VARCHAR(255) NOT NULL,
                          channel VARCHAR(10) NOT NULL CHECK (channel IN ('EMAIL', 'SMS', 'PUSH')),
                          message_template TEXT NOT NULL,
                          is_transactional BOOLEAN DEFAULT FALSE,
                          status VARCHAR(20) DEFAULT 'SCHEDULED'
                              CHECK (status IN ('SCHEDULED', 'RUNNING', 'COMPLETED', 'FAILED')),
                          scheduled_at TIMESTAMP,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_campaign_tenant_id ON campaign(tenant_id);
CREATE INDEX idx_campaign_status ON campaign(status);

-- 3. Recipient (tenant-scoped, from CSV upload)
CREATE TABLE recipient (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           tenant_id UUID NOT NULL REFERENCES tenant(id),
                           campaign_id UUID NOT NULL REFERENCES campaign(id),
                           recipient_ref VARCHAR(255) NOT NULL,
                           email VARCHAR(255),
                           phone VARCHAR(50),
                           timezone VARCHAR(100) DEFAULT 'UTC',
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_recipient_campaign_id ON recipient(campaign_id);
CREATE INDEX idx_recipient_tenant_id ON recipient(tenant_id);

-- 4. Global Suppression List
CREATE TABLE suppression_list (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  tenant_id UUID NOT NULL REFERENCES tenant(id),
                                  recipient_ref VARCHAR(255) NOT NULL,
                                  channel VARCHAR(10) NOT NULL CHECK (channel IN ('EMAIL', 'SMS', 'PUSH')),
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                  UNIQUE (tenant_id, recipient_ref, channel)
);

-- 5. NotificationJob
CREATE TABLE notification_job (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  tenant_id UUID NOT NULL REFERENCES tenant(id),
                                  campaign_id UUID NOT NULL REFERENCES campaign(id),
                                  recipient_id UUID NOT NULL REFERENCES recipient(id),
                                  idempotency_key VARCHAR(512) NOT NULL,
                                  status VARCHAR(20) DEFAULT 'PENDING'
                                      CHECK (status IN ('PENDING', 'PROCESSING', 'SENT', 'FAILED', 'SKIPPED', 'DELAYED')),
                                  retry_count INT DEFAULT 0,
                                  next_retry_at TIMESTAMP,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                  UNIQUE (idempotency_key),
                                  UNIQUE (campaign_id, recipient_id)
);

CREATE INDEX idx_job_tenant_id ON notification_job(tenant_id);
CREATE INDEX idx_job_campaign_id ON notification_job(campaign_id);
CREATE INDEX idx_job_status ON notification_job(status);

-- 6. DeliveryAttempt
CREATE TABLE delivery_attempt (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  tenant_id UUID NOT NULL REFERENCES tenant(id),
                                  notification_job_id UUID NOT NULL REFERENCES notification_job(id),
                                  attempt_number INT NOT NULL,
                                  status VARCHAR(10) NOT NULL CHECK (status IN ('SENT', 'FAILED')),
                                  provider_response TEXT,
                                  error_code VARCHAR(100),
                                  attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_attempt_job_id ON delivery_attempt(notification_job_id);

-- 7. Outbox (Transactional Outbox Pattern)
CREATE TABLE outbox_event (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              tenant_id UUID NOT NULL,
                              aggregate_type VARCHAR(100) NOT NULL,
                              aggregate_id UUID NOT NULL,
                              event_type VARCHAR(100) NOT NULL,
                              payload JSONB NOT NULL,
                              published BOOLEAN DEFAULT FALSE,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Partial index — only indexes unpublished rows (very efficient)
CREATE INDEX idx_outbox_unpublished ON outbox_event(published) WHERE published = FALSE;