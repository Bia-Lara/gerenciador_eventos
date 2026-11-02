INSERT INTO event (id, name, start_date_time, end_date_time, organizer_id, status) VALUES
    ('10000000-0000-0000-0000-000000000004', 'Encontro concluído de demonstração',
     strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-2 days', 'start of day', '+10 hours'),
     strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-2 days', 'start of day', '+12 hours'),
     '00000000-0000-0000-0000-000000000001', 'COMPLETED')
ON CONFLICT(id) DO UPDATE SET
    name = excluded.name,
    start_date_time = excluded.start_date_time,
    end_date_time = excluded.end_date_time,
    organizer_id = excluded.organizer_id,
    status = excluded.status;

INSERT INTO event (id, name, start_date_time, end_date_time, organizer_id, status) VALUES
    ('10000000-0000-0000-0000-000000000005', 'Encontro de demonstração de outro organizador',
     strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '+7 days', 'start of day', '+10 hours'),
     strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '+7 days', 'start of day', '+12 hours'),
     '00000000-0000-0000-0000-000000000003', 'ACTIVE')
ON CONFLICT(id) DO UPDATE SET
    name = excluded.name,
    start_date_time = excluded.start_date_time,
    end_date_time = excluded.end_date_time,
    organizer_id = excluded.organizer_id,
    status = excluded.status;

INSERT INTO category (id, event_id, name, capacity, price) VALUES
    ('20000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000004', 'Participação geral', 40, 25.00),
    ('20000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000005', 'Inscrição de demonstração', 40, 25.00)
ON CONFLICT(id) DO UPDATE SET
    event_id = excluded.event_id,
    name = excluded.name,
    capacity = excluded.capacity,
    price = excluded.price;

INSERT INTO registration (id, user_id, category_id, status) VALUES
    ('30000000-0000-0000-0000-000000000002',
     '00000000-0000-0000-0000-000000000002',
     '20000000-0000-0000-0000-000000000007',
     'ATIVA')
ON CONFLICT(id) DO UPDATE SET
    user_id = excluded.user_id,
    category_id = excluded.category_id,
    status = excluded.status;

WITH RECURSIVE registration_slots(slot) AS (
    SELECT 1
    UNION ALL
    SELECT slot + 1 FROM registration_slots WHERE slot < 39
),
active_count(count) AS (
    SELECT COUNT(*)
    FROM registration
    WHERE category_id = '20000000-0000-0000-0000-000000000008'
      AND status = 'ATIVA'
),
missing_slots(slot) AS (
    SELECT registration_slots.slot
    FROM registration_slots
    CROSS JOIN active_count
    WHERE NOT EXISTS (
        SELECT 1
        FROM registration
        WHERE id = printf('30000000-0000-0000-0000-%012d', registration_slots.slot)
          AND category_id = '20000000-0000-0000-0000-000000000008'
          AND status = 'ATIVA'
    )
    ORDER BY registration_slots.slot
    LIMIT (SELECT MAX(0, 39 - count) FROM active_count)
)
INSERT INTO registration (id, user_id, category_id, status)
SELECT printf('30000000-0000-0000-0000-%012d', slot),
       printf('90000000-0000-0000-0000-%012d', slot),
       '20000000-0000-0000-0000-000000000008',
       'ATIVA'
FROM missing_slots
WHERE true
ON CONFLICT(id) DO UPDATE SET
    user_id = excluded.user_id,
    category_id = excluded.category_id,
    status = excluded.status;