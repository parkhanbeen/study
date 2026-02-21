-- Add preferred language column for cross-device language sync
ALTER TABLE members
    ADD COLUMN preferred_language VARCHAR(5) DEFAULT NULL AFTER status;
