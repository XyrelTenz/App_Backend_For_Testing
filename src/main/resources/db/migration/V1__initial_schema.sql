-- =============================================================================
-- V1: Initial Schema — Driving App (EasyBao / BaoBao)
-- Based on secondesign.sql — PostgreSQL + PostGIS
-- Managed by Flyway
-- =============================================================================

-- Extensions
CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS postgis;

-- =============================================================================
-- ENUMS
-- =============================================================================
CREATE TYPE user_role AS ENUM ('passenger', 'driver', 'none');

CREATE TYPE ride_status AS ENUM (
    'searching',
    'driver_assigned',
    'driver_en_route',
    'driver_arrived',
    'in_progress',
    'completed',
    'cancelled'
);

CREATE TYPE payment_status AS ENUM ('pending', 'collected');

-- =============================================================================
-- SHARED TRIGGER FUNCTION
-- =============================================================================
CREATE OR REPLACE FUNCTION update_timestamp_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

-- =============================================================================
-- USERS TABLE
-- =============================================================================
CREATE TABLE users (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    firebase_uid TEXT    UNIQUE NOT NULL,
    email        CITEXT  UNIQUE NOT NULL,
    phone        CITEXT  UNIQUE,
    full_name    TEXT    NOT NULL,
    role         user_role DEFAULT 'none',
    is_active    BOOLEAN DEFAULT true,
    deleted_at   TIMESTAMPTZ,
    last_seen_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ DEFAULT now(),
    updated_at   TIMESTAMPTZ DEFAULT now()
);

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users FOR EACH ROW EXECUTE PROCEDURE update_timestamp_column();

-- =============================================================================
-- USER FCM TOKENS
-- =============================================================================
CREATE TABLE user_fcm_tokens (
    id          UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    fcm_token   TEXT    UNIQUE NOT NULL,
    device_type TEXT,
    created_at  TIMESTAMPTZ DEFAULT now(),
    updated_at  TIMESTAMPTZ DEFAULT now()
);

CREATE TRIGGER trg_user_fcm_tokens_updated_at
    BEFORE UPDATE ON user_fcm_tokens FOR EACH ROW EXECUTE PROCEDURE update_timestamp_column();

-- =============================================================================
-- USER PROFILES
-- =============================================================================
CREATE TABLE user_profiles (
    user_id              UUID    PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    profile_image_url    TEXT,
    country              TEXT    DEFAULT 'Philippines',
    address              TEXT,
    dob                  DATE,
    average_rating       NUMERIC(3,2) DEFAULT 5.0 CHECK (average_rating BETWEEN 1 AND 5),
    total_trips_completed INT    DEFAULT 0,
    updated_at           TIMESTAMPTZ DEFAULT now()
);

CREATE TRIGGER trg_user_profiles_updated_at
    BEFORE UPDATE ON user_profiles FOR EACH ROW EXECUTE PROCEDURE update_timestamp_column();

-- =============================================================================
-- DRIVERS TABLE
-- =============================================================================
CREATE TABLE drivers (
    user_id                    UUID    PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    plate_number               TEXT    UNIQUE NOT NULL CHECK (length(plate_number) >= 3),
    vehicle_type               TEXT    NOT NULL DEFAULT 'baobao_etrike',
    vehicle_color              TEXT,
    license_number             TEXT    NOT NULL,
    is_verified                BOOLEAN DEFAULT false,
    is_online                  BOOLEAN DEFAULT false,
    total_cash_collected       NUMERIC(15,2) DEFAULT 0.0,
    commission_owed_to_platform NUMERIC(15,2) DEFAULT 0.0,
    last_known_location        GEOGRAPHY(Point, 4326),
    updated_at                 TIMESTAMPTZ DEFAULT now()
);

CREATE TRIGGER trg_drivers_updated_at
    BEFORE UPDATE ON drivers FOR EACH ROW EXECUTE PROCEDURE update_timestamp_column();

-- =============================================================================
-- RIDES TABLE (PARTITIONED by created_at)
-- =============================================================================
CREATE TABLE rides (
    id                     UUID          DEFAULT gen_random_uuid(),
    created_at             TIMESTAMPTZ   DEFAULT now(),
    passenger_id           UUID          NOT NULL,
    driver_id              UUID,
    pickup_address         TEXT          NOT NULL,
    pickup_location        GEOGRAPHY(Point, 4326) NOT NULL,
    dropoff_address        TEXT          NOT NULL,
    dropoff_location       GEOGRAPHY(Point, 4326) NOT NULL,
    distance_km            NUMERIC(6,2),
    estimated_duration_mins INT,
    estimated_fare_amount  NUMERIC(8,2),
    final_fare_amount      NUMERIC(8,2),
    payment_method         TEXT          NOT NULL DEFAULT 'cash',
    payment_status         payment_status DEFAULT 'pending',
    status                 ride_status   DEFAULT 'searching',
    scheduled_at           TIMESTAMPTZ,
    started_at             TIMESTAMPTZ,
    completed_at           TIMESTAMPTZ,
    cancelled_at           TIMESTAMPTZ,
    cancel_reason          TEXT,
    updated_at             TIMESTAMPTZ   DEFAULT now(),
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

-- Initial partitions
CREATE TABLE rides_y2026m04 PARTITION OF rides FOR VALUES FROM ('2026-04-01') TO ('2026-05-01');
CREATE TABLE rides_y2026m05 PARTITION OF rides FOR VALUES FROM ('2026-05-01') TO ('2026-06-01');
CREATE TABLE rides_y2026m06 PARTITION OF rides FOR VALUES FROM ('2026-06-01') TO ('2026-07-01');
CREATE TABLE rides_y2026m07 PARTITION OF rides FOR VALUES FROM ('2026-07-01') TO ('2026-08-01');
CREATE TABLE rides_y2026m08 PARTITION OF rides FOR VALUES FROM ('2026-08-01') TO ('2026-09-01');
CREATE TABLE rides_y2026m09 PARTITION OF rides FOR VALUES FROM ('2026-09-01') TO ('2026-10-01');
CREATE TABLE rides_y2026m10 PARTITION OF rides FOR VALUES FROM ('2026-10-01') TO ('2026-11-01');
CREATE TABLE rides_y2026m11 PARTITION OF rides FOR VALUES FROM ('2026-11-01') TO ('2026-12-01');
CREATE TABLE rides_y2026m12 PARTITION OF rides FOR VALUES FROM ('2026-12-01') TO ('2027-01-01');

CREATE TRIGGER trg_rides_updated_at
    BEFORE UPDATE ON rides FOR EACH ROW EXECUTE PROCEDURE update_timestamp_column();

-- =============================================================================
-- RATINGS TABLE
-- =============================================================================
CREATE TABLE ratings (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ride_id    UUID NOT NULL,
    rater_id   UUID NOT NULL REFERENCES users(id),
    rated_id   UUID NOT NULL REFERENCES users(id),
    score      INT  NOT NULL CHECK (score BETWEEN 1 AND 5),
    comment    TEXT,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TRIGGER trg_ratings_updated_at
    BEFORE UPDATE ON ratings FOR EACH ROW EXECUTE PROCEDURE update_timestamp_column();

-- =============================================================================
-- SAVED PLACES TABLE
-- =============================================================================
CREATE TABLE saved_places (
    id         UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    label      TEXT    NOT NULL CHECK (length(label) > 0),
    address    TEXT    NOT NULL,
    location   GEOGRAPHY(Point, 4326) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TRIGGER trg_saved_places_updated_at
    BEFORE UPDATE ON saved_places FOR EACH ROW EXECUTE PROCEDURE update_timestamp_column();

-- =============================================================================
-- CHAT MESSAGES TABLE
-- =============================================================================
CREATE TABLE chat_messages (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ride_id      UUID NOT NULL,
    sender_id    UUID NOT NULL REFERENCES users(id),
    message_text TEXT NOT NULL,
    created_at   TIMESTAMPTZ DEFAULT now()
);

-- =============================================================================
-- NOTIFICATIONS TABLE
-- =============================================================================
CREATE TABLE notifications (
    id         UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title      TEXT    NOT NULL,
    message    TEXT    NOT NULL,
    type       TEXT    NOT NULL,
    is_read    BOOLEAN DEFAULT false,
    payload    JSONB,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- =============================================================================
-- PERFORMANCE INDEXES
-- =============================================================================
CREATE INDEX idx_drivers_last_location    ON drivers    USING GIST (last_known_location);
CREATE INDEX idx_rides_pickup_location    ON rides      USING GIST (pickup_location);
CREATE INDEX idx_saved_places_location    ON saved_places USING GIST (location);
CREATE INDEX idx_rides_passenger_id       ON rides(passenger_id);
CREATE INDEX idx_rides_driver_id          ON rides(driver_id);
CREATE INDEX idx_ratings_ride_id          ON ratings(ride_id);
CREATE INDEX idx_chat_messages_ride_id    ON chat_messages(ride_id);
CREATE INDEX idx_notifications_user_id    ON notifications(user_id);
CREATE INDEX idx_fcm_tokens_user_id       ON user_fcm_tokens(user_id);
CREATE INDEX idx_drivers_online_verified  ON drivers(is_online) WHERE is_online = true AND is_verified = true;
CREATE INDEX idx_rides_active_status      ON rides(status) WHERE status NOT IN ('completed', 'cancelled');
