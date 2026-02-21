-- Expand post status for content moderation
-- Existing values: PUBLISHED, DELETED
-- New values: HIDDEN, UNDER_REVIEW, REMOVED
ALTER TABLE posts MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED';
