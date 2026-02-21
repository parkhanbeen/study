CREATE DATABASE IF NOT EXISTS anon_community CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE anon_community;

-- Debezium user
CREATE USER IF NOT EXISTS 'debezium'@'%' IDENTIFIED BY 'debezium_password';
GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'debezium'@'%';
FLUSH PRIVILEGES;

-- members 테이블 (nickname, bio, post_count 제거)
CREATE TABLE members (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    password_hash      VARCHAR(72),
    status             VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    preferred_language VARCHAR(5),
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- member_profiles (신규)
CREATE TABLE member_profiles (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id       BIGINT       NOT NULL,
    nickname        VARCHAR(20)  NOT NULL,
    bio             VARCHAR(200),
    post_count      INT          NOT NULL DEFAULT 0,
    follower_count  INT          NOT NULL DEFAULT 0,
    following_count INT          NOT NULL DEFAULT 0,
    is_default      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE  KEY uk_nickname   (nickname),
    INDEX       idx_member_id (member_id),
    CONSTRAINT fk_mp_member FOREIGN KEY (member_id) REFERENCES members(id)
) ENGINE=InnoDB;

-- profile_follows (신규)
CREATE TABLE profile_follows (
    id                   BIGINT    AUTO_INCREMENT PRIMARY KEY,
    follower_profile_id  BIGINT    NOT NULL,
    following_profile_id BIGINT    NOT NULL,
    created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_follow    (follower_profile_id, following_profile_id),
    INDEX      idx_follower  (follower_profile_id),
    INDEX      idx_following (following_profile_id),
    CONSTRAINT fk_pf_follower  FOREIGN KEY (follower_profile_id)  REFERENCES member_profiles(id),
    CONSTRAINT fk_pf_following FOREIGN KEY (following_profile_id) REFERENCES member_profiles(id)
) ENGINE=InnoDB;

-- posts: author_id → author_profile_id
CREATE TABLE posts (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_profile_id BIGINT       NOT NULL,
    title             VARCHAR(100) NOT NULL,
    content           TEXT         NOT NULL,
    category          VARCHAR(30)  NOT NULL,
    tags              JSON,
    status            VARCHAR(20)  NOT NULL DEFAULT 'PUBLISHED',
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_author_profile (author_profile_id),
    INDEX idx_category (category),
    INDEX idx_status_created (status, created_at),
    CONSTRAINT fk_posts_profile FOREIGN KEY (author_profile_id) REFERENCES member_profiles(id)
) ENGINE=InnoDB;

-- comments: author_id → author_profile_id
CREATE TABLE comments (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id             BIGINT        NOT NULL,
    author_profile_id   BIGINT        NOT NULL,
    content             VARCHAR(1000) NOT NULL,
    parent_comment_id   BIGINT,
    depth               INT           NOT NULL DEFAULT 0,
    status              VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_post_id (post_id),
    CONSTRAINT fk_comments_post    FOREIGN KEY (post_id)           REFERENCES posts(id),
    CONSTRAINT fk_comments_profile FOREIGN KEY (author_profile_id) REFERENCES member_profiles(id),
    CONSTRAINT fk_comments_parent  FOREIGN KEY (parent_comment_id) REFERENCES comments(id)
) ENGINE=InnoDB;

-- post_likes: member_id → profile_id
CREATE TABLE post_likes (
    id         BIGINT    NOT NULL AUTO_INCREMENT PRIMARY KEY,
    post_id    BIGINT    NOT NULL,
    profile_id BIGINT    NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_post_profile (post_id, profile_id),
    INDEX      idx_profile_id  (profile_id),
    CONSTRAINT fk_likes_post    FOREIGN KEY (post_id)    REFERENCES posts(id),
    CONSTRAINT fk_likes_profile FOREIGN KEY (profile_id) REFERENCES member_profiles(id)
) ENGINE=InnoDB;

-- post_stats (기존 유지)
CREATE TABLE post_stats (
    post_id       BIGINT NOT NULL PRIMARY KEY,
    like_count    INT    NOT NULL DEFAULT 0,
    comment_count INT    NOT NULL DEFAULT 0,
    view_count    INT    NOT NULL DEFAULT 0,
    CONSTRAINT fk_stats_post FOREIGN KEY (post_id) REFERENCES posts(id)
) ENGINE=InnoDB;

-- processed_events (기존 유지 — idempotency for CDC consumers)
CREATE TABLE IF NOT EXISTS processed_events (
    event_id     VARCHAR(100) NOT NULL,
    event_type   VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (event_id, event_type)
) ENGINE=InnoDB;
