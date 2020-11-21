CREATE TABLE IF NOT EXISTS user_records(
    id BIGSERIAL PRIMARY KEY,
    last_check_in TIMESTAMP WITH TIME ZONE NOT NULL,
    number VARCHAR NOT NULL UNIQUE,
    password VARCHAR NOT NULL,
    bssid VARCHAR NOT NULL,
    vd_mac VARCHAR NOT NULL,
    device_mac VARCHAR,
    pending_violation BOOLEAN DEFAULT FALSE NOT NULL,
    url VARCHAR NOT NULL
);