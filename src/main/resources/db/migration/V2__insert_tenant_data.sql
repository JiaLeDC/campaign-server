-- Bulk insert fake tenants
INSERT INTO tenant (id, name, monthly_campaign_limit, monthly_message_limit, created_at) VALUES
(gen_random_uuid(), 'Acme Corp', 100, 1000000, NOW()),
(gen_random_uuid(), 'BrightFuture Inc', 150, 500000, NOW() - INTERVAL '1 day'),
(gen_random_uuid(), 'Skyline Solutions', 200, 2000000, NOW() - INTERVAL '2 days'),
(gen_random_uuid(), 'NextGen Tech', 120, 750000, NOW() - INTERVAL '3 days'),
(gen_random_uuid(), 'BlueOcean LLC', 80, 300000, NOW() - INTERVAL '4 days'),
(gen_random_uuid(), 'GreenLeaf Ltd', 100, 1000000, NOW() - INTERVAL '5 days'),
(gen_random_uuid(), 'Sunrise Media', 90, 600000, NOW() - INTERVAL '6 days'),
(gen_random_uuid(), 'RapidFlow Systems', 110, 900000, NOW() - INTERVAL '7 days'),
(gen_random_uuid(), 'Quantum Innovations', 130, 1200000, NOW() - INTERVAL '8 days'),
(gen_random_uuid(), 'Pioneer Labs', 100, 1000000, NOW() - INTERVAL '9 days');
