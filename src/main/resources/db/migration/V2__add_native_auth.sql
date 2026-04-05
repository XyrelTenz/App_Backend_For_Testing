-- =============================================================================
-- V2: Add Native Authentication Support
-- =============================================================================

-- Drop the NOT NULL constraint on firebase_uid so users can register natively
ALTER TABLE users ALTER COLUMN firebase_uid DROP NOT NULL;

-- Drop the UNIQUE constraint index on firebase_uid if the framework created one,
-- Wait, actually we can keep it UNIQUE. PostgreSQL ignores nulls in UNIQUE indexes.
-- So multiple users can have a NULL firebase_uid.

-- Add password_hash for native email/password authentication
ALTER TABLE users ADD COLUMN password_hash TEXT;
