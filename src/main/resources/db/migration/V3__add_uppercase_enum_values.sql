-- =============================================================================
-- V3: Add Uppercase ENUM Values for Hibernate Compatibility
-- PostgreSQL ENUMs are case-sensitive. Hibernate sends uppercase strings
-- by default. This syncs the database types with the Kotlin enum names.
-- =============================================================================
-- user_role
ALTER TYPE user_role ADD VALUE 'PASSENGER';
ALTER TYPE user_role ADD VALUE 'DRIVER';
ALTER TYPE user_role ADD VALUE 'NONE';

-- ride_status
ALTER TYPE ride_status ADD VALUE 'SEARCHING';
ALTER TYPE ride_status ADD VALUE 'DRIVER_ASSIGNED';
ALTER TYPE ride_status ADD VALUE 'DRIVER_EN_ROUTE';
ALTER TYPE ride_status ADD VALUE 'DRIVER_ARRIVED';
ALTER TYPE ride_status ADD VALUE 'IN_PROGRESS';
ALTER TYPE ride_status ADD VALUE 'COMPLETED';
ALTER TYPE ride_status ADD VALUE 'CANCELLED';

-- payment_status
ALTER TYPE payment_status ADD VALUE 'PENDING';
ALTER TYPE payment_status ADD VALUE 'COLLECTED';
