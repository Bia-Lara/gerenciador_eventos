CREATE TABLE IF NOT EXISTS event (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    start_date_time TEXT NOT NULL,
    end_date_time TEXT NOT NULL,
    organizer_id TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS category (
    id TEXT PRIMARY KEY,
    event_id TEXT NOT NULL,
    name TEXT NOT NULL,
    capacity INTEGER NOT NULL,
    price REAL NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_category_event_name
ON category(event_id, name);

CREATE TABLE IF NOT EXISTS registration (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    category_id TEXT NOT NULL,
    status TEXT NOT NULL
);
