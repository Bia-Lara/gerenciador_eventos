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

INSERT INTO category (id, event_id, name, capacity, price) VALUES
    ('20000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000004', 'Participação geral', 40, 25.00)
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