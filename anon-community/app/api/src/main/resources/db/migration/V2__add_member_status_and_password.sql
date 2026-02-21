-- Add password column and member status for auth enhancement
ALTER TABLE members
    ADD COLUMN password_hash VARCHAR(72) DEFAULT NULL AFTER nickname,
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' AFTER post_count;

CREATE INDEX idx_members_status ON members(status);
