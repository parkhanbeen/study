# Follow/Following & Multi-Profile Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task.

**Goal:** 프로필 기반 팔로우/팔로잉 + 멀티프로필(페르소나) 시스템을 구현한다. Member(인증)와 MemberProfile(공개 페르소나)를 분리하고, 프로필 간 단방향 팔로우, 팔로잉 피드(Fan-out on Read), 멀티계정 전환을 지원한다.

**Architecture:** Member = 인증 엔티티 (로그인/비밀번호). MemberProfile = 공개 페르소나 (닉네임, 게시글, 팔로워). Post/Comment/Like 전부 authorProfileId 기반. JWT에 activeProfileId 추가. Gateway가 X-Profile-Id 헤더 전파. followerCount/followingCount는 CDC(Debezium → Kafka → FollowEventConsumer)로 비정규화.

**Tech Stack:** Kotlin 1.9.25, Spring Boot 3.4, Kotest 5.9.1 (DescribeSpec), MockK, JPA, OpenSearch, Redis, Kafka, Next.js 16 App Router, Tailwind CSS 4, shadcn/ui, React Query v5, Zustand

---

## 사전 지식

### 테스트 패턴 (반드시 준수)

```kotlin
// Kotest DescribeSpec 패턴 — 모든 테스트는 이 구조를 따름
class FooSpec : DescribeSpec({
    val port = mockk<SomePort>()
    val useCase = FooUseCase(port)

    describe("FooUseCase") {
        context("정상 케이스") {
            it("should do something") {
                every { port.find(any()) } returns someValue
                val result = useCase.invoke(command)
                result shouldBe expected
                verify(exactly = 1) { port.find(any()) }
            }
        }
        context("예외 케이스") {
            it("should throw when not found") {
                every { port.find(any()) } returns null
                shouldThrow<NotFoundException> { useCase.invoke(command) }
            }
        }
    }
})
```

### 도메인 패턴

```kotlin
// Value Object 패턴
@JvmInline
value class ProfileId(val value: Long) {
    init { require(value > 0) { "ProfileId must be positive" } }
}

// Aggregate 패턴 (PostLike와 동일 구조)
class MemberProfile private constructor(...) : AggregateRoot() {
    companion object {
        fun create(...): MemberProfile { ... }
    }
}

// Port 패턴
interface ProfileCommandPort {
    fun save(profile: MemberProfile): MemberProfile
}
```

### 빌드 명령어

```bash
# 전체 테스트
/Users/parkhanbin/git/study/anon-community/gradlew test

# 특정 모듈 테스트
/Users/parkhanbin/git/study/anon-community/gradlew :domain:member:test
/Users/parkhanbin/git/study/anon-community/gradlew :app:usecase:test
/Users/parkhanbin/git/study/anon-community/gradlew :app:api:test
/Users/parkhanbin/git/study/anon-community/gradlew :app:gateway:test

# 프론트엔드 빌드
export PATH="$HOME/.nvm/versions/node/v20.20.0/bin:$PATH"
cd /Users/parkhanbin/git/study/anon-community/frontend && npm run build
```

---

## Phase 1: 도메인 리팩토링

---

### Task 1: ProfileId 값 객체 + MemberProfile 도메인

**Files:**
- Create: `domain/member/src/main/kotlin/com/anoncommunity/domain/member/ProfileId.kt`
- Create: `domain/member/src/main/kotlin/com/anoncommunity/domain/member/MemberProfile.kt`
- Create: `domain/member/src/main/kotlin/com/anoncommunity/domain/member/port/ProfileCommandPort.kt`
- Create: `domain/member/src/main/kotlin/com/anoncommunity/domain/member/port/ProfileQueryPort.kt`
- Create: `domain/member/src/test/kotlin/com/anoncommunity/domain/member/MemberProfileSpec.kt`

**Step 1: 테스트 작성**

```kotlin
// MemberProfileSpec.kt
package com.anoncommunity.domain.member

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class MemberProfileSpec : DescribeSpec({

    describe("MemberProfile") {

        context("create") {
            it("should create default profile") {
                val profile = MemberProfile.create(
                    id = ProfileId(1L),
                    memberId = MemberId(10L),
                    nickname = MemberNickname("testuser"),
                    bio = Bio("hello"),
                    isDefault = true,
                )
                profile.id shouldBe ProfileId(1L)
                profile.memberId shouldBe MemberId(10L)
                profile.nickname.value shouldBe "testuser"
                profile.isDefault shouldBe true
                profile.postCount shouldBe 0
                profile.followerCount shouldBe 0
                profile.followingCount shouldBe 0
            }

            it("should reject blank nickname") {
                shouldThrow<IllegalArgumentException> {
                    MemberProfile.create(
                        id = ProfileId(1L),
                        memberId = MemberId(10L),
                        nickname = MemberNickname(""),
                        bio = Bio(""),
                        isDefault = false,
                    )
                }
            }
        }

        context("updateProfile") {
            it("should update nickname and bio") {
                val profile = MemberProfile.create(
                    ProfileId(1L), MemberId(10L),
                    MemberNickname("old"), Bio("old bio"), isDefault = true,
                )
                val updated = profile.updateProfile(MemberNickname("new"), Bio("new bio"))
                updated.nickname.value shouldBe "new"
                updated.bio.value shouldBe "new bio"
            }
        }

        context("postCount") {
            it("should increment postCount") {
                val profile = MemberProfile.create(ProfileId(1L), MemberId(10L), MemberNickname("u"), Bio(""), isDefault = true)
                profile.incrementPostCount()
                profile.postCount shouldBe 1
            }

            it("should not decrement below 0") {
                val profile = MemberProfile.create(ProfileId(1L), MemberId(10L), MemberNickname("u"), Bio(""), isDefault = true)
                profile.decrementPostCount()
                profile.postCount shouldBe 0
            }
        }

        context("followerCount") {
            it("should increment followerCount") {
                val profile = MemberProfile.create(ProfileId(1L), MemberId(10L), MemberNickname("u"), Bio(""), isDefault = true)
                profile.incrementFollowerCount()
                profile.followerCount shouldBe 1
            }

            it("should not decrement followerCount below 0") {
                val profile = MemberProfile.create(ProfileId(1L), MemberId(10L), MemberNickname("u"), Bio(""), isDefault = true)
                profile.decrementFollowerCount()
                profile.followerCount shouldBe 0
            }
        }

        context("followingCount") {
            it("should increment followingCount") {
                val profile = MemberProfile.create(ProfileId(1L), MemberId(10L), MemberNickname("u"), Bio(""), isDefault = true)
                profile.incrementFollowingCount()
                profile.followingCount shouldBe 1
            }
        }
    }
})
```

**Step 2: 테스트 실패 확인**

```bash
/Users/parkhanbin/git/study/anon-community/gradlew :domain:member:test
# Expected: FAIL — ProfileId, MemberProfile not found
```

**Step 3: ProfileId 구현**

```kotlin
// ProfileId.kt
package com.anoncommunity.domain.member

@JvmInline
value class ProfileId(val value: Long) {
    init { require(value > 0) { "ProfileId must be positive" } }
}
```

**Step 4: MemberProfile 구현**

```kotlin
// MemberProfile.kt
package com.anoncommunity.domain.member

import java.time.Instant

class MemberProfile(
    val id: ProfileId,
    val memberId: MemberId,
    val nickname: MemberNickname,
    val bio: Bio,
    var postCount: Int = 0,
    var followerCount: Int = 0,
    var followingCount: Int = 0,
    val isDefault: Boolean = false,
    val createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now(),
) {
    fun updateProfile(nickname: MemberNickname, bio: Bio): MemberProfile =
        MemberProfile(
            id = this.id, memberId = this.memberId,
            nickname = nickname, bio = bio,
            postCount = this.postCount, followerCount = this.followerCount,
            followingCount = this.followingCount, isDefault = this.isDefault,
            createdAt = this.createdAt, updatedAt = Instant.now(),
        )

    fun incrementPostCount() { postCount = maxOf(0, postCount + 1) }
    fun decrementPostCount() { postCount = maxOf(0, postCount - 1) }
    fun incrementFollowerCount() { followerCount = maxOf(0, followerCount + 1) }
    fun decrementFollowerCount() { followerCount = maxOf(0, followerCount - 1) }
    fun incrementFollowingCount() { followingCount = maxOf(0, followingCount + 1) }
    fun decrementFollowingCount() { followingCount = maxOf(0, followingCount - 1) }

    companion object {
        fun create(
            id: ProfileId,
            memberId: MemberId,
            nickname: MemberNickname,
            bio: Bio,
            isDefault: Boolean = false,
        ): MemberProfile = MemberProfile(
            id = id, memberId = memberId, nickname = nickname, bio = bio,
            postCount = 0, followerCount = 0, followingCount = 0,
            isDefault = isDefault,
        )
    }
}
```

**Step 5: Port 인터페이스 구현**

```kotlin
// ProfileCommandPort.kt
package com.anoncommunity.domain.member.port

import com.anoncommunity.domain.member.MemberProfile
import com.anoncommunity.domain.member.ProfileId

interface ProfileCommandPort {
    fun save(profile: MemberProfile): MemberProfile
    fun delete(id: ProfileId)
}
```

```kotlin
// ProfileQueryPort.kt
package com.anoncommunity.domain.member.port

import com.anoncommunity.domain.member.MemberProfile
import com.anoncommunity.domain.member.MemberId
import com.anoncommunity.domain.member.ProfileId

interface ProfileQueryPort {
    fun findById(id: ProfileId): MemberProfile?
    fun findByMemberId(memberId: MemberId): List<MemberProfile>
    fun findDefaultByMemberId(memberId: MemberId): MemberProfile?
    fun countByMemberId(memberId: MemberId): Int
    fun existsByNickname(nickname: String): Boolean
}
```

**Step 6: 테스트 통과 확인**

```bash
/Users/parkhanbin/git/study/anon-community/gradlew :domain:member:test
# Expected: BUILD SUCCESSFUL
```

**Step 7: 커밋**

```bash
git add domain/member/src/
git commit -m "feat(domain): add ProfileId value object and MemberProfile aggregate"
```

---

### Task 2: Member 도메인 리팩토링 (nickname/bio/postCount 제거)

**Files:**
- Modify: `domain/member/src/main/kotlin/com/anoncommunity/domain/member/Member.kt`
- Modify: `domain/member/src/test/kotlin/com/anoncommunity/domain/member/MemberSpec.kt`

**현재 Member.kt 확인**: `domain/member/src/main/kotlin/com/anoncommunity/domain/member/Member.kt` 를 읽어서 현재 필드 파악 후 제거.

**Step 1: Member 도메인에서 nickname, bio, postCount 제거**

```kotlin
// Member.kt — nickname, bio, postCount 필드 및 관련 메서드 제거
// updateProfile(nickname, bio) 메서드 제거
// incrementPostCount() / decrementPostCount() 메서드 제거
// 남기는 필드: id, passwordHash, status, preferredLanguage, createdAt, updatedAt

class Member(
    val id: MemberId,
    val passwordHash: String?,
    val status: MemberStatus,
    val preferredLanguage: PreferredLanguage?,
    val createdAt: Instant,
    var updatedAt: Instant,
) {
    fun updatePreferredLanguage(language: PreferredLanguage): Member = ...
    fun suspend(): Member = ...
    fun ban(): Member = ...
    fun activate(): Member = ...
    // nickname/bio/postCount 관련 메서드 전부 제거
}
```

**Step 2: MemberSpec.kt 업데이트**

MemberSpec.kt에서 nickname, bio, postCount 관련 테스트 케이스 제거. 기존 updateProfile 테스트 → MemberProfileSpec으로 이동.

**Step 3: 컴파일 오류 확인 및 수정**

```bash
/Users/parkhanbin/git/study/anon-community/gradlew :domain:member:compileKotlin
# 오류 있으면 수정 후 재시도
```

**Step 4: 테스트 통과 확인**

```bash
/Users/parkhanbin/git/study/anon-community/gradlew :domain:member:test
# Expected: BUILD SUCCESSFUL
```

**Step 5: 커밋**

```bash
git add domain/member/
git commit -m "refactor(domain): remove nickname/bio/postCount from Member — moved to MemberProfile"
```

---

### Task 3: Post 도메인 리팩토링 (authorProfileId)

**Files:**
- Modify: `domain/post/src/main/kotlin/com/anoncommunity/domain/post/Post.kt`
- Modify: 관련 테스트 파일 (있다면)

**Step 1: Post.kt 읽기**

`domain/post/src/main/kotlin/com/anoncommunity/domain/post/Post.kt` 파일을 읽어서 authorId 필드 위치 확인.

**Step 2: authorId → authorProfileId 변경**

```kotlin
// Post.kt 변경
class Post(
    val id: PostId,
    val authorProfileId: ProfileId,  // 변경: authorId: MemberId → authorProfileId: ProfileId
    val title: PostTitle,
    val content: PostContent,
    val category: PostCategory,
    val tags: PostTags,
    val status: PostStatus,
    val createdAt: Instant,
    var updatedAt: Instant,
) {
    // create() factory도 authorProfileId로 변경
    companion object {
        fun create(
            id: PostId,
            authorProfileId: ProfileId,  // 변경
            title: PostTitle,
            content: PostContent,
            category: PostCategory,
            tags: PostTags,
        ): Post = Post(...)
    }
}
```

**Step 3: 컴파일 오류 수정**

```bash
/Users/parkhanbin/git/study/anon-community/gradlew :domain:post:compileKotlin
# 오류 나는 파일들 확인 후 authorId → authorProfileId 일괄 수정
```

**Step 4: 테스트 수정 + 통과 확인**

관련 테스트에서 `authorId = MemberId(1L)` → `authorProfileId = ProfileId(1L)` 로 수정.

```bash
/Users/parkhanbin/git/study/anon-community/gradlew :domain:post:test
# Expected: BUILD SUCCESSFUL
```

**Step 5: 커밋**

```bash
git add domain/post/
git commit -m "refactor(domain): replace Post.authorId(MemberId) with authorProfileId(ProfileId)"
```

---

### Task 4: Comment 도메인 리팩토링 (authorProfileId)

**Files:**
- Modify: `domain/comment/src/main/kotlin/com/anoncommunity/domain/comment/Comment.kt`
- Modify: 관련 테스트 파일

Task 3와 동일한 패턴. `authorId: MemberId` → `authorProfileId: ProfileId` 변경.

**Step 1: Comment.kt 읽기 후 변경**

```kotlin
class Comment(
    val id: CommentId,
    val postId: PostId,
    val authorProfileId: ProfileId,  // 변경
    ...
) { ... }
```

**Step 2: 컴파일 + 테스트**

```bash
/Users/parkhanbin/git/study/anon-community/gradlew :domain:comment:test
# Expected: BUILD SUCCESSFUL
```

**Step 3: 커밋**

```bash
git add domain/comment/
git commit -m "refactor(domain): replace Comment.authorId(MemberId) with authorProfileId(ProfileId)"
```

---

### Task 5: PostLike 도메인 리팩토링 (profileId)

**Files:**
- Modify: `domain/like/src/main/kotlin/com/anoncommunity/domain/like/PostLike.kt`
- Modify: `domain/like/src/main/kotlin/com/anoncommunity/domain/like/port/LikeQueryPort.kt`
- Modify: 관련 테스트

**Step 1: PostLike.kt 읽기 후 변경**

```kotlin
class PostLike(
    val id: PostLikeId,
    val postId: PostId,
    val profileId: ProfileId,  // 변경: memberId: MemberId → profileId: ProfileId
    val createdAt: Instant,
) {
    companion object {
        fun like(id: PostLikeId, postId: PostId, profileId: ProfileId, postAuthorProfileId: ProfileId): PostLike {
            require(profileId != postAuthorProfileId) { "Cannot like your own post" }
            return PostLike(id, postId, profileId, Instant.now())
        }
    }
}
```

**Step 2: LikeQueryPort 변경**

```kotlin
interface LikeQueryPort {
    fun findByPostIdAndProfileId(postId: PostId, profileId: ProfileId): PostLike?
    fun existsByPostIdAndProfileId(postId: PostId, profileId: ProfileId): Boolean
    fun findPostIdsByProfileId(profileId: ProfileId, pageable: DomainPageable): PageResult<PostId>
}
```

**Step 3: 테스트 수정 + 통과**

```bash
/Users/parkhanbin/git/study/anon-community/gradlew :domain:like:test
```

**Step 4: 커밋**

```bash
git add domain/like/
git commit -m "refactor(domain): replace PostLike.memberId with profileId(ProfileId)"
```

---

## Phase 2: Follow 도메인

---

### Task 6: ProfileFollow 도메인

**Files:**
- Create: `domain/follow/` (새 Gradle 모듈 또는 domain/member 내부)
- Create: `domain/member/src/main/kotlin/com/anoncommunity/domain/member/ProfileFollow.kt`
- Create: `domain/member/src/main/kotlin/com/anoncommunity/domain/member/ProfileFollowId.kt`
- Create: `domain/member/src/main/kotlin/com/anoncommunity/domain/member/port/FollowCommandPort.kt`
- Create: `domain/member/src/main/kotlin/com/anoncommunity/domain/member/port/FollowQueryPort.kt`
- Create: `domain/member/src/test/kotlin/com/anoncommunity/domain/member/ProfileFollowSpec.kt`

> **주의**: 별도 domain/follow 모듈을 만들지 않고 domain/member 안에 배치하는 이유 — ProfileFollow는 MemberProfile 간의 관계이므로 member 도메인에 속함. 모듈 추가는 build.gradle.kts 수정이 필요하므로 YAGNI 원칙에 따라 기존 모듈 활용.

**Step 1: 테스트 작성**

```kotlin
// ProfileFollowSpec.kt
package com.anoncommunity.domain.member

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class ProfileFollowSpec : DescribeSpec({

    describe("ProfileFollow") {

        context("follow") {
            it("should create follow relationship") {
                val follow = ProfileFollow.follow(
                    id = ProfileFollowId(1L),
                    followerProfileId = ProfileId(1L),
                    followingProfileId = ProfileId(2L),
                )
                follow.followerProfileId shouldBe ProfileId(1L)
                follow.followingProfileId shouldBe ProfileId(2L)
                follow.createdAt shouldNotBe null
            }

            it("should reject self-follow") {
                shouldThrow<IllegalArgumentException> {
                    ProfileFollow.follow(
                        id = ProfileFollowId(1L),
                        followerProfileId = ProfileId(1L),
                        followingProfileId = ProfileId(1L),
                    )
                }
            }
        }
    }
})
```

**Step 2: 테스트 실패 확인**

```bash
/Users/parkhanbin/git/study/anon-community/gradlew :domain:member:test
# Expected: FAIL — ProfileFollow, ProfileFollowId not found
```

**Step 3: 구현**

```kotlin
// ProfileFollowId.kt
@JvmInline
value class ProfileFollowId(val value: Long) {
    init { require(value > 0) { "ProfileFollowId must be positive" } }
}

// ProfileFollow.kt
import java.time.Instant

class ProfileFollow(
    val id: ProfileFollowId,
    val followerProfileId: ProfileId,
    val followingProfileId: ProfileId,
    val createdAt: Instant = Instant.now(),
) {
    companion object {
        fun follow(
            id: ProfileFollowId,
            followerProfileId: ProfileId,
            followingProfileId: ProfileId,
        ): ProfileFollow {
            require(followerProfileId != followingProfileId) { "Cannot follow yourself" }
            return ProfileFollow(id, followerProfileId, followingProfileId)
        }
    }
}
```

```kotlin
// FollowCommandPort.kt
interface FollowCommandPort {
    fun save(follow: ProfileFollow): ProfileFollow
    fun delete(followerProfileId: ProfileId, followingProfileId: ProfileId)
}

// FollowQueryPort.kt
interface FollowQueryPort {
    fun findByFollowerAndFollowing(followerProfileId: ProfileId, followingProfileId: ProfileId): ProfileFollow?
    fun existsByFollowerAndFollowing(followerProfileId: ProfileId, followingProfileId: ProfileId): Boolean
    fun findFollowingIds(followerProfileId: ProfileId, limit: Int = 1000): List<ProfileId>
    fun findFollowers(followingProfileId: ProfileId, pageable: DomainPageable): PageResult<MemberProfile>
    fun findFollowing(followerProfileId: ProfileId, pageable: DomainPageable): PageResult<MemberProfile>
}
```

**Step 4: 테스트 통과 확인**

```bash
/Users/parkhanbin/git/study/anon-community/gradlew :domain:member:test
# Expected: BUILD SUCCESSFUL
```

**Step 5: 커밋**

```bash
git add domain/member/
git commit -m "feat(domain): add ProfileFollow aggregate with self-follow guard"
```

---

## Phase 3: Infra 레이어

---

### Task 7: DB Schema 변경 (init-db SQL)

**Files:**
- Modify: `init-db/01-schema.sql`

**Step 1: 현재 SQL 읽기**

`init-db/01-schema.sql` 파일 읽어서 현재 테이블 구조 파악.

**Step 2: SQL 업데이트**

기존 `members` 테이블에서 `nickname`, `bio`, `post_count` 컬럼 제거. 신규 테이블 추가:

```sql
-- members 테이블 (nickname, bio, post_count 제거)
CREATE TABLE members (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    password_hash      VARCHAR(72),
    status             VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    preferred_language VARCHAR(5),
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

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
);

-- profile_follows (신규)
CREATE TABLE profile_follows (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    follower_profile_id  BIGINT    NOT NULL,
    following_profile_id BIGINT    NOT NULL,
    created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_follow    (follower_profile_id, following_profile_id),
    INDEX      idx_follower  (follower_profile_id),
    INDEX      idx_following (following_profile_id),
    CONSTRAINT fk_pf_follower  FOREIGN KEY (follower_profile_id)  REFERENCES member_profiles(id),
    CONSTRAINT fk_pf_following FOREIGN KEY (following_profile_id) REFERENCES member_profiles(id)
);

-- posts: author_id → author_profile_id
CREATE TABLE posts (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_profile_id BIGINT       NOT NULL,  -- 변경: author_id
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
);

-- comments: author_id → author_profile_id
CREATE TABLE comments (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id             BIGINT       NOT NULL,
    author_profile_id   BIGINT       NOT NULL,  -- 변경
    content             VARCHAR(1000) NOT NULL,
    parent_comment_id   BIGINT,
    depth               INT          NOT NULL DEFAULT 0,
    status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_post_id (post_id),
    CONSTRAINT fk_comments_post    FOREIGN KEY (post_id)           REFERENCES posts(id),
    CONSTRAINT fk_comments_profile FOREIGN KEY (author_profile_id) REFERENCES member_profiles(id),
    CONSTRAINT fk_comments_parent  FOREIGN KEY (parent_comment_id) REFERENCES comments(id)
);

-- post_likes: member_id → profile_id
CREATE TABLE post_likes (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id    BIGINT    NOT NULL,
    profile_id BIGINT    NOT NULL,  -- 변경: member_id
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_post_profile (post_id, profile_id),
    INDEX      idx_profile_id  (profile_id),
    CONSTRAINT fk_likes_post    FOREIGN KEY (post_id)    REFERENCES posts(id),
    CONSTRAINT fk_likes_profile FOREIGN KEY (profile_id) REFERENCES member_profiles(id)
);

-- post_stats (기존 유지)
CREATE TABLE post_stats (
    post_id      BIGINT NOT NULL PRIMARY KEY,
    like_count   INT    NOT NULL DEFAULT 0,
    comment_count INT   NOT NULL DEFAULT 0,
    view_count   INT    NOT NULL DEFAULT 0,
    CONSTRAINT fk_stats_post FOREIGN KEY (post_id) REFERENCES posts(id)
);
```

**Step 3: 커밋**

```bash
git add init-db/
git commit -m "feat(infra): update DB schema — MemberProfile table, profile_follows, author_profile_id"
```

---

### Task 8: MemberProfileEntity + JPA 어댑터

**Files:**
- Create: `infra/rdb/src/main/kotlin/com/anoncommunity/infra/rdb/entity/MemberProfileEntity.kt`
- Create: `infra/rdb/src/main/kotlin/com/anoncommunity/infra/rdb/repository/MemberProfileRepository.kt`
- Create: `infra/rdb/src/main/kotlin/com/anoncommunity/infra/rdb/adapter/ProfileCommandPortAdapter.kt`
- Create: `infra/rdb/src/main/kotlin/com/anoncommunity/infra/rdb/adapter/ProfileQueryPortAdapter.kt`
- Modify: `infra/rdb/src/main/kotlin/com/anoncommunity/infra/rdb/entity/MemberEntity.kt` (nickname, bio, postCount 컬럼 제거)
- Modify: `infra/rdb/src/main/kotlin/com/anoncommunity/infra/rdb/adapter/MemberCommandPortAdapter.kt`
- Modify: `infra/rdb/src/main/kotlin/com/anoncommunity/infra/rdb/adapter/MemberQueryPortAdapter.kt`

**Step 1: 기존 파일들 읽기**

`infra/rdb/src/main/kotlin/com/anoncommunity/infra/rdb/entity/MemberEntity.kt` 읽어서 패턴 파악.

**Step 2: MemberEntity 업데이트**

```kotlin
@Entity
@Table(name = "members")
class MemberEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column(name = "password_hash", length = 72)
    var passwordHash: String? = null,
    @Column(nullable = false, length = 20)
    var status: String = "ACTIVE",
    @Column(name = "preferred_language", length = 5)
    var preferredLanguage: String? = null,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: java.time.Instant = java.time.Instant.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: java.time.Instant = java.time.Instant.now(),
) {
    fun toModel(): Member = Member(
        id = MemberId(id),
        passwordHash = passwordHash,
        status = MemberStatus.valueOf(status),
        preferredLanguage = preferredLanguage?.let { PreferredLanguage(it) },
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
    companion object {
        fun fromModel(member: Member): MemberEntity = MemberEntity(
            id = member.id.value,
            passwordHash = member.passwordHash,
            status = member.status.name,
            preferredLanguage = member.preferredLanguage?.value,
            createdAt = member.createdAt,
            updatedAt = member.updatedAt,
        )
    }
}
```

**Step 3: MemberProfileEntity 구현**

```kotlin
// MemberProfileEntity.kt
@Entity
@Table(name = "member_profiles")
class MemberProfileEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column(name = "member_id", nullable = false)
    val memberId: Long = 0L,
    @Column(nullable = false, unique = true, length = 20)
    var nickname: String = "",
    @Column(length = 200)
    var bio: String? = null,
    @Column(name = "post_count", nullable = false)
    var postCount: Int = 0,
    @Column(name = "follower_count", nullable = false)
    var followerCount: Int = 0,
    @Column(name = "following_count", nullable = false)
    var followingCount: Int = 0,
    @Column(name = "is_default", nullable = false)
    var isDefault: Boolean = false,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: java.time.Instant = java.time.Instant.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: java.time.Instant = java.time.Instant.now(),
) {
    fun toModel(): MemberProfile = MemberProfile(
        id = ProfileId(id),
        memberId = MemberId(memberId),
        nickname = MemberNickname(nickname),
        bio = Bio(bio ?: ""),
        postCount = postCount,
        followerCount = followerCount,
        followingCount = followingCount,
        isDefault = isDefault,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
    companion object {
        fun fromModel(profile: MemberProfile): MemberProfileEntity = MemberProfileEntity(
            id = profile.id.value,
            memberId = profile.memberId.value,
            nickname = profile.nickname.value,
            bio = profile.bio.value.ifBlank { null },
            postCount = profile.postCount,
            followerCount = profile.followerCount,
            followingCount = profile.followingCount,
            isDefault = profile.isDefault,
            createdAt = profile.createdAt,
            updatedAt = profile.updatedAt,
        )
    }
}
```

**Step 4: Repository + 어댑터 구현**

```kotlin
// MemberProfileRepository.kt
interface MemberProfileRepository : JpaRepository<MemberProfileEntity, Long> {
    fun findByMemberId(memberId: Long): List<MemberProfileEntity>
    fun findByMemberIdAndIsDefaultTrue(memberId: Long): MemberProfileEntity?
    fun countByMemberId(memberId: Long): Int
    fun existsByNickname(nickname: String): Boolean
}

// ProfileCommandPortAdapter.kt
@Component
class ProfileCommandPortAdapter(
    private val repository: MemberProfileRepository,
) : ProfileCommandPort {
    @Transactional
    override fun save(profile: MemberProfile): MemberProfile =
        repository.save(MemberProfileEntity.fromModel(profile)).toModel()

    @Transactional
    override fun delete(id: ProfileId) = repository.deleteById(id.value)
}

// ProfileQueryPortAdapter.kt
@Component
class ProfileQueryPortAdapter(
    private val repository: MemberProfileRepository,
) : ProfileQueryPort {
    @Transactional(readOnly = true)
    override fun findById(id: ProfileId): MemberProfile? =
        repository.findByIdOrNull(id.value)?.toModel()

    @Transactional(readOnly = true)
    override fun findByMemberId(memberId: MemberId): List<MemberProfile> =
        repository.findByMemberId(memberId.value).map { it.toModel() }

    @Transactional(readOnly = true)
    override fun findDefaultByMemberId(memberId: MemberId): MemberProfile? =
        repository.findByMemberIdAndIsDefaultTrue(memberId.value)?.toModel()

    @Transactional(readOnly = true)
    override fun countByMemberId(memberId: MemberId): Int =
        repository.countByMemberId(memberId.value)

    @Transactional(readOnly = true)
    override fun existsByNickname(nickname: String): Boolean =
        repository.existsByNickname(nickname)
}
```

**Step 5: 컴파일 확인**

```bash
/Users/parkhanbin/git/study/anon-community/gradlew :infra:rdb:compileKotlin
```

**Step 6: 커밋**

```bash
git add infra/rdb/
git commit -m "feat(infra): add MemberProfileEntity and JPA adapters for ProfileCommandPort/QueryPort"
```

---

### Task 9: ProfileFollowEntity + JPA 어댑터

**Files:**
- Create: `infra/rdb/src/main/kotlin/com/anoncommunity/infra/rdb/entity/ProfileFollowEntity.kt`
- Create: `infra/rdb/src/main/kotlin/com/anoncommunity/infra/rdb/repository/ProfileFollowRepository.kt`
- Create: `infra/rdb/src/main/kotlin/com/anoncommunity/infra/rdb/adapter/FollowCommandPortAdapter.kt`
- Create: `infra/rdb/src/main/kotlin/com/anoncommunity/infra/rdb/adapter/FollowQueryPortAdapter.kt`

**Step 1: 구현**

```kotlin
// ProfileFollowEntity.kt
@Entity
@Table(name = "profile_follows")
class ProfileFollowEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column(name = "follower_profile_id", nullable = false)
    val followerProfileId: Long = 0L,
    @Column(name = "following_profile_id", nullable = false)
    val followingProfileId: Long = 0L,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: java.time.Instant = java.time.Instant.now(),
) {
    fun toModel(): ProfileFollow = ProfileFollow(
        id = ProfileFollowId(id),
        followerProfileId = ProfileId(followerProfileId),
        followingProfileId = ProfileId(followingProfileId),
        createdAt = createdAt,
    )
    companion object {
        fun fromModel(follow: ProfileFollow): ProfileFollowEntity = ProfileFollowEntity(
            id = follow.id.value,
            followerProfileId = follow.followerProfileId.value,
            followingProfileId = follow.followingProfileId.value,
            createdAt = follow.createdAt,
        )
    }
}

// ProfileFollowRepository.kt
interface ProfileFollowRepository : JpaRepository<ProfileFollowEntity, Long> {
    fun findByFollowerProfileIdAndFollowingProfileId(
        followerProfileId: Long,
        followingProfileId: Long,
    ): ProfileFollowEntity?

    fun existsByFollowerProfileIdAndFollowingProfileId(
        followerProfileId: Long,
        followingProfileId: Long,
    ): Boolean

    @Query("SELECT e.followingProfileId FROM ProfileFollowEntity e WHERE e.followerProfileId = :followerId ORDER BY e.createdAt DESC LIMIT :limit")
    fun findFollowingIds(followerProfileId: Long, limit: Int): List<Long>

    fun deleteByFollowerProfileIdAndFollowingProfileId(
        followerProfileId: Long,
        followingProfileId: Long,
    )

    @Query("SELECT p FROM MemberProfileEntity p JOIN ProfileFollowEntity f ON f.followingProfileId = p.id WHERE f.followerProfileId = :followingProfileId")
    fun findFollowerProfiles(followingProfileId: Long, pageable: org.springframework.data.domain.Pageable): org.springframework.data.domain.Page<MemberProfileEntity>

    @Query("SELECT p FROM MemberProfileEntity p JOIN ProfileFollowEntity f ON f.followerProfileId = p.id WHERE f.followerProfileId = :followerProfileId")
    fun findFollowingProfiles(followerProfileId: Long, pageable: org.springframework.data.domain.Pageable): org.springframework.data.domain.Page<MemberProfileEntity>
}
```

**Step 2: 어댑터 구현**

```kotlin
// FollowCommandPortAdapter.kt
@Component
class FollowCommandPortAdapter(
    private val repository: ProfileFollowRepository,
) : FollowCommandPort {
    @Transactional
    override fun save(follow: ProfileFollow): ProfileFollow =
        repository.save(ProfileFollowEntity.fromModel(follow)).toModel()

    @Transactional
    override fun delete(followerProfileId: ProfileId, followingProfileId: ProfileId) =
        repository.deleteByFollowerProfileIdAndFollowingProfileId(
            followerProfileId.value, followingProfileId.value,
        )
}

// FollowQueryPortAdapter.kt
@Component
class FollowQueryPortAdapter(
    private val repository: ProfileFollowRepository,
    private val profileRepository: MemberProfileRepository,
) : FollowQueryPort {
    @Transactional(readOnly = true)
    override fun findByFollowerAndFollowing(followerProfileId: ProfileId, followingProfileId: ProfileId): ProfileFollow? =
        repository.findByFollowerProfileIdAndFollowingProfileId(
            followerProfileId.value, followingProfileId.value,
        )?.toModel()

    @Transactional(readOnly = true)
    override fun existsByFollowerAndFollowing(followerProfileId: ProfileId, followingProfileId: ProfileId): Boolean =
        repository.existsByFollowerProfileIdAndFollowingProfileId(
            followerProfileId.value, followingProfileId.value,
        )

    @Transactional(readOnly = true)
    override fun findFollowingIds(followerProfileId: ProfileId, limit: Int): List<ProfileId> =
        repository.findFollowingIds(followerProfileId.value, limit).map { ProfileId(it) }

    @Transactional(readOnly = true)
    override fun findFollowers(followingProfileId: ProfileId, pageable: DomainPageable): PageResult<MemberProfile> {
        val page = repository.findFollowerProfiles(
            followingProfileId.value,
            org.springframework.data.domain.PageRequest.of(pageable.page, pageable.size),
        )
        return PageResult(
            content = page.content.map { it.toModel() },
            page = pageable.page,
            size = pageable.size,
            totalElements = page.totalElements,
        )
    }

    @Transactional(readOnly = true)
    override fun findFollowing(followerProfileId: ProfileId, pageable: DomainPageable): PageResult<MemberProfile> {
        val page = repository.findFollowingProfiles(
            followerProfileId.value,
            org.springframework.data.domain.PageRequest.of(pageable.page, pageable.size),
        )
        return PageResult(
            content = page.content.map { it.toModel() },
            page = pageable.page,
            size = pageable.size,
            totalElements = page.totalElements,
        )
    }
}
```

**Step 3: 컴파일 + 커밋**

```bash
/Users/parkhanbin/git/study/anon-community/gradlew :infra:rdb:compileKotlin
git add infra/rdb/
git commit -m "feat(infra): add ProfileFollowEntity and FollowCommandPort/QueryPort adapters"
```

---

### Task 10: PostEntity/CommentEntity/PostLikeEntity 업데이트

**Files:**
- Modify: `infra/rdb/src/main/kotlin/com/anoncommunity/infra/rdb/entity/PostEntity.kt`
- Modify: `infra/rdb/src/main/kotlin/com/anoncommunity/infra/rdb/entity/CommentEntity.kt`
- Modify: `infra/rdb/src/main/kotlin/com/anoncommunity/infra/rdb/entity/PostLikeEntity.kt`
- Modify: 관련 어댑터들

**Step 1: 각 파일 읽기 후 authorId → authorProfileId, memberId → profileId 변경**

PostEntity:
```kotlin
// authorId: Long → authorProfileId: Long
@Column(name = "author_profile_id", nullable = false)
val authorProfileId: Long = 0L,

// toModel()에서: authorId = MemberId(authorId) → authorProfileId = ProfileId(authorProfileId)
// fromModel()에서: authorId = post.authorId.value → authorProfileId = post.authorProfileId.value
```

CommentEntity, PostLikeEntity도 동일한 방식.

**Step 2: 어댑터에서 컴파일 오류 수정**

PostCommandPortAdapter, PostQueryPortAdapter 등에서 authorId 참조 → authorProfileId로 변경.

**Step 3: 컴파일 + 커밋**

```bash
/Users/parkhanbin/git/study/anon-community/gradlew :infra:rdb:compileKotlin
git add infra/rdb/
git commit -m "refactor(infra): update PostEntity/CommentEntity/PostLikeEntity to use profile-based author IDs"
```

---

### Task 11: PostDocument (OpenSearch) authorProfileId 업데이트

**Files:**
- Modify: `infra/opensearch/src/main/kotlin/com/anoncommunity/infra/opensearch/document/PostDocument.kt`
- Modify: `infra/opensearch/src/main/kotlin/com/anoncommunity/infra/opensearch/adapter/PostFeedPortAdapter.kt`
- Modify: `infra/opensearch/src/main/kotlin/com/anoncommunity/infra/opensearch/index/PostIndexMapping.kt` (매핑 있다면)
- Modify: `app/consumer/src/main/kotlin/com/anoncommunity/app/consumer/domain/PostCdcConsumer.kt`

**Step 1: PostDocument.kt 읽기 후 변경**

```kotlin
data class PostDocument(
    val id: Long,
    val authorProfileId: Long,     // 변경: authorId → authorProfileId
    val authorNickname: String,    // profile.nickname
    val title: String,
    val content: String,
    val category: String,
    val tags: List<String>,
    val status: String,
    val likeCount: Int,
    val commentCount: Int,
    val viewCount: Int,
    val createdAt: String,
    val updatedAt: String,
)
```

**Step 2: PostFeedPortAdapter에서 getFollowingFeed 메서드 추가**

```kotlin
// PostFeedPortAdapter.kt에 추가
fun getFollowingFeed(followingProfileIds: List<ProfileId>, pageable: DomainPageable): PageResult<Post> {
    if (followingProfileIds.isEmpty()) return PageResult.empty(pageable)
    val ids = followingProfileIds.map { it.value }
    val query = BoolQueryBuilder()
        .filter(TermsQueryBuilder("authorProfileId", ids))
        .filter(TermQueryBuilder("status", "PUBLISHED"))
    // searchAfter pagination (기존 getFeed와 동일 패턴)
    ...
}
```

**Step 3: PostCdcConsumer에서 authorId → authorProfileId 변경**

**Step 4: OpenSearch 매핑 파일에서 authorProfileId 반영**

```bash
/Users/parkhanbin/git/study/anon-community/gradlew :infra:opensearch:compileKotlin
git add infra/opensearch/ app/consumer/
git commit -m "refactor(infra): update PostDocument and OpenSearch mapping to use authorProfileId"
```

---

## Phase 4: Gateway

---

### Task 12: AuthFilter X-Profile-Id 헤더 + JWT Claims 변경

**Files:**
- Modify: `app/gateway/src/main/kotlin/com/anoncommunity/app/gateway/filter/AuthFilter.kt` (또는 관련 필터)
- Modify: `app/gateway/src/main/kotlin/com/anoncommunity/app/gateway/security/JwtProvider.kt`
- Modify: `app/gateway/src/test/kotlin/.../GatewayAuthFilterSpec.kt`

**Step 1: JwtProvider 읽기 후 변경**

```kotlin
// JwtProvider.kt
// generateToken에 activeProfileId 파라미터 추가
fun generateToken(memberId: Long, activeProfileId: Long): String =
    Jwts.builder()
        .claim("memberId", memberId)
        .claim("activeProfileId", activeProfileId)  // 추가
        .expiration(Date(System.currentTimeMillis() + tokenExpiry))
        .signWith(secretKey)
        .compact()

// extractActiveProfileId 메서드 추가
fun extractActiveProfileId(token: String): Long =
    parseClaims(token).get("activeProfileId", Long::class.java)
        ?: throw IllegalArgumentException("Missing activeProfileId in token")
```

**Step 2: AuthFilter에서 X-Profile-Id 헤더 전파**

```kotlin
// AuthFilter.kt — validateAndExtract 후
val memberId = jwtProvider.extractMemberId(token)
val activeProfileId = jwtProvider.extractActiveProfileId(token)

exchange.request.mutate()
    .header("X-Member-Id", memberId.toString())
    .header("X-Profile-Id", activeProfileId.toString())  // 추가
    .build()
```

**Step 3: LoginUseCase에서 토큰 발행 시 defaultProfile ID 전달**

`app/usecase/src/main/kotlin/com/anoncommunity/app/usecase/member/LoginUseCase.kt` 수정:
- 로그인 후 `profileQueryPort.findDefaultByMemberId(member.id)` 조회
- `jwtProvider.generateToken(memberId, defaultProfile.id.value)` 호출

**Step 4: GatewayAuthFilterSpec 업데이트**

X-Profile-Id 헤더가 올바르게 설정되는지 테스트 추가.

**Step 5: 테스트 + 커밋**

```bash
/Users/parkhanbin/git/study/anon-community/gradlew :app:gateway:test
git add app/gateway/ app/usecase/
git commit -m "feat(gateway): add X-Profile-Id header propagation from JWT activeProfileId claim"
```

---

## Phase 5: UseCase 레이어

---

### Task 13: CreateMemberUseCase — default Profile 자동 생성

**Files:**
- Modify: `app/usecase/src/main/kotlin/com/anoncommunity/app/usecase/member/CreateMemberUseCase.kt`
- Modify: `app/usecase/src/test/kotlin/com/anoncommunity/app/usecase/member/CreateMemberUseCaseSpec.kt`

**Step 1: 현재 CreateMemberUseCase 읽기**

**Step 2: 테스트 업데이트**

```kotlin
// CreateMemberUseCaseSpec.kt에 추가
it("should create default profile when member is created") {
    val savedMember = Member(MemberId(1L), passwordHash = "hash", ...)
    every { memberCommandPort.save(any()) } returns savedMember
    every { profileCommandPort.save(any()) } returns mockk<MemberProfile>()

    useCase.invoke(CreateMemberCommand("nick", "pass"))

    verify(exactly = 1) { profileCommandPort.save(match { it.isDefault && it.nickname.value == "nick" }) }
}
```

**Step 3: UseCase 구현 수정**

```kotlin
// CreateMemberUseCase.kt
@Transactional
override fun invoke(command: CreateMemberCommand): Member {
    val member = memberCommandPort.save(Member.create(...))
    // default profile 자동 생성
    profileCommandPort.save(
        MemberProfile.create(
            id = ProfileId(0L),  // auto-generated
            memberId = member.id,
            nickname = MemberNickname(command.nickname),
            bio = Bio(""),
            isDefault = true,
        )
    )
    return member
}
```

**Step 4: 테스트 + 커밋**

```bash
/Users/parkhanbin/git/study/anon-community/gradlew :app:usecase:test
git add app/usecase/
git commit -m "feat(usecase): auto-create default MemberProfile on member registration"
```

---

### Task 14: CreateProfileUseCase + UpdateProfileUseCase

**Files:**
- Create: `app/usecase/src/main/kotlin/com/anoncommunity/app/usecase/member/CreateProfileUseCase.kt`
- Create: `app/usecase/src/main/kotlin/com/anoncommunity/app/usecase/member/UpdateProfileUseCase.kt`
- Create: `app/usecase/src/test/kotlin/com/anoncommunity/app/usecase/member/CreateProfileUseCaseSpec.kt`
- Create: `app/usecase/src/test/kotlin/com/anoncommunity/app/usecase/member/UpdateProfileUseCaseSpec.kt`

**Step 1: 테스트 작성**

```kotlin
// CreateProfileUseCaseSpec.kt
class CreateProfileUseCaseSpec : DescribeSpec({
    val profileCommandPort = mockk<ProfileCommandPort>()
    val profileQueryPort = mockk<ProfileQueryPort>()
    val useCase = CreateProfileUseCase(profileCommandPort, profileQueryPort)

    describe("CreateProfileUseCase") {
        context("정상 생성") {
            it("should create profile for member") {
                every { profileQueryPort.countByMemberId(MemberId(1L)) } returns 1
                every { profileQueryPort.existsByNickname("new_nick") } returns false
                every { profileCommandPort.save(any()) } returnsArgument 0

                val result = useCase.invoke(CreateProfileCommand(memberId = 1L, nickname = "new_nick", bio = "bio"))
                result.nickname.value shouldBe "new_nick"
                result.isDefault shouldBe false
            }
        }
        context("최대 5개 초과") {
            it("should throw when profile count >= 5") {
                every { profileQueryPort.countByMemberId(MemberId(1L)) } returns 5
                shouldThrow<ProfileLimitExceededException> {
                    useCase.invoke(CreateProfileCommand(1L, "nick", "bio"))
                }
            }
        }
        context("닉네임 중복") {
            it("should throw when nickname already exists") {
                every { profileQueryPort.countByMemberId(any()) } returns 1
                every { profileQueryPort.existsByNickname("dup") } returns true
                shouldThrow<NicknameAlreadyExistsException> {
                    useCase.invoke(CreateProfileCommand(1L, "dup", "bio"))
                }
            }
        }
    }
})
```

**Step 2: 예외 클래스 추가**

```kotlin
// 기존 예외 패턴 확인 후 동일하게 추가
class ProfileLimitExceededException : RuntimeException("Profile limit exceeded: max 5")
class ProfileNotFoundException(id: ProfileId) : RuntimeException("Profile not found: ${id.value}")
class NicknameAlreadyExistsException(nickname: String) : RuntimeException("Nickname already exists: $nickname")
```

**Step 3: UseCase 구현**

```kotlin
data class CreateProfileCommand(val memberId: Long, val nickname: String, val bio: String)

@Service
class CreateProfileUseCase(
    private val profileCommandPort: ProfileCommandPort,
    private val profileQueryPort: ProfileQueryPort,
) : CommandUseCase<CreateProfileCommand, MemberProfile> {
    @Transactional
    override fun invoke(command: CreateProfileCommand): MemberProfile {
        val memberId = MemberId(command.memberId)
        if (profileQueryPort.countByMemberId(memberId) >= 5) throw ProfileLimitExceededException()
        if (profileQueryPort.existsByNickname(command.nickname)) throw NicknameAlreadyExistsException(command.nickname)
        return profileCommandPort.save(
            MemberProfile.create(
                id = ProfileId(0L),
                memberId = memberId,
                nickname = MemberNickname(command.nickname),
                bio = Bio(command.bio),
                isDefault = false,
            )
        )
    }
}
```

**Step 4: UpdateProfileUseCase 동일 패턴으로 구현**

```kotlin
data class UpdateProfileCommand(val memberId: Long, val profileId: Long, val nickname: String, val bio: String)

@Service
class UpdateProfileUseCase(...) : CommandUseCase<UpdateProfileCommand, MemberProfile> {
    @Transactional
    override fun invoke(command: UpdateProfileCommand): MemberProfile {
        val profile = profileQueryPort.findById(ProfileId(command.profileId))
            ?: throw ProfileNotFoundException(ProfileId(command.profileId))
        if (profile.memberId != MemberId(command.memberId)) throw ProfileNotFoundException(ProfileId(command.profileId))
        if (profileQueryPort.existsByNickname(command.nickname) && profile.nickname.value != command.nickname)
            throw NicknameAlreadyExistsException(command.nickname)
        return profileCommandPort.save(profile.updateProfile(MemberNickname(command.nickname), Bio(command.bio)))
    }
}
```

**Step 5: 테스트 + 커밋**

```bash
/Users/parkhanbin/git/study/anon-community/gradlew :app:usecase:test
git add app/usecase/
git commit -m "feat(usecase): add CreateProfileUseCase and UpdateProfileUseCase with TDD"
```

---

### Task 15: SwitchProfileUseCase + GetPublicProfileUseCase

**Files:**
- Create: `app/usecase/src/main/kotlin/com/anoncommunity/app/usecase/member/SwitchProfileUseCase.kt`
- Create: `app/usecase/src/main/kotlin/com/anoncommunity/app/usecase/member/GetPublicProfileUseCase.kt`
- Create: `app/usecase/src/test/kotlin/.../SwitchProfileUseCaseSpec.kt`
- Create: `app/usecase/src/test/kotlin/.../GetPublicProfileUseCaseSpec.kt`

**Step 1: SwitchProfileUseCaseSpec 작성**

```kotlin
class SwitchProfileUseCaseSpec : DescribeSpec({
    val profileQueryPort = mockk<ProfileQueryPort>()
    val jwtProvider = mockk<JwtProvider>()
    val useCase = SwitchProfileUseCase(profileQueryPort, jwtProvider)

    describe("SwitchProfileUseCase") {
        context("소유자가 전환") {
            it("should return new token with switched profileId") {
                val profile = mockk<MemberProfile> {
                    every { memberId } returns MemberId(1L)
                    every { id } returns ProfileId(42L)
                }
                every { profileQueryPort.findById(ProfileId(42L)) } returns profile
                every { jwtProvider.generateToken(1L, 42L) } returns "new-token"

                val result = useCase.invoke(SwitchProfileCommand(memberId = 1L, targetProfileId = 42L))
                result shouldBe "new-token"
            }
        }
        context("타인 프로필 전환 시도") {
            it("should throw ProfileNotFoundException (security: no leakage)") {
                val profile = mockk<MemberProfile> { every { memberId } returns MemberId(999L) }
                every { profileQueryPort.findById(ProfileId(42L)) } returns profile

                shouldThrow<ProfileNotFoundException> {
                    useCase.invoke(SwitchProfileCommand(memberId = 1L, targetProfileId = 42L))
                }
            }
        }
        context("존재하지 않는 프로필") {
            it("should throw ProfileNotFoundException") {
                every { profileQueryPort.findById(ProfileId(99L)) } returns null
                shouldThrow<ProfileNotFoundException> {
                    useCase.invoke(SwitchProfileCommand(memberId = 1L, targetProfileId = 99L))
                }
            }
        }
    }
})
```

**Step 2: 구현**

```kotlin
data class SwitchProfileCommand(val memberId: Long, val targetProfileId: Long)

@Service
class SwitchProfileUseCase(
    private val profileQueryPort: ProfileQueryPort,
    private val jwtProvider: JwtProvider,
) : CommandUseCase<SwitchProfileCommand, String> {
    override fun invoke(command: SwitchProfileCommand): String {
        val profile = profileQueryPort.findById(ProfileId(command.targetProfileId))
            ?: throw ProfileNotFoundException(ProfileId(command.targetProfileId))
        if (profile.memberId != MemberId(command.memberId))
            throw ProfileNotFoundException(ProfileId(command.targetProfileId))
        return jwtProvider.generateToken(command.memberId, command.targetProfileId)
    }
}
```

**Step 3: GetPublicProfileUseCase**

```kotlin
data class GetPublicProfileQuery(val profileId: Long, val requesterProfileId: Long?)

data class PublicProfile(val profile: MemberProfile, val isFollowing: Boolean)

@Service
class GetPublicProfileUseCase(
    private val profileQueryPort: ProfileQueryPort,
    private val followQueryPort: FollowQueryPort,
) : QueryUseCase<GetPublicProfileQuery, PublicProfile> {
    override fun invoke(query: GetPublicProfileQuery): PublicProfile {
        val profile = profileQueryPort.findById(ProfileId(query.profileId))
            ?: throw ProfileNotFoundException(ProfileId(query.profileId))
        val isFollowing = query.requesterProfileId?.let {
            followQueryPort.existsByFollowerAndFollowing(ProfileId(it), ProfileId(query.profileId))
        } ?: false
        return PublicProfile(profile, isFollowing)
    }
}
```

**Step 4: 테스트 + 커밋**

```bash
/Users/parkhanbin/git/study/anon-community/gradlew :app:usecase:test
git add app/usecase/
git commit -m "feat(usecase): add SwitchProfileUseCase and GetPublicProfileUseCase with TDD"
```

---

### Task 16: FollowUseCase + UnfollowUseCase

**Files:**
- Create: `app/usecase/src/main/kotlin/com/anoncommunity/app/usecase/follow/FollowUseCase.kt`
- Create: `app/usecase/src/main/kotlin/com/anoncommunity/app/usecase/follow/UnfollowUseCase.kt`
- Create: `app/usecase/src/test/kotlin/com/anoncommunity/app/usecase/follow/FollowUseCaseSpec.kt`

**Step 1: 테스트 작성**

```kotlin
class FollowUseCaseSpec : DescribeSpec({
    val followCommandPort = mockk<FollowCommandPort>()
    val followQueryPort = mockk<FollowQueryPort>()
    val profileQueryPort = mockk<ProfileQueryPort>()
    val useCase = FollowUseCase(followCommandPort, followQueryPort, profileQueryPort)

    describe("FollowUseCase") {
        context("정상 팔로우") {
            it("should follow a profile") {
                every { profileQueryPort.findById(ProfileId(2L)) } returns mockk()
                every { followQueryPort.existsByFollowerAndFollowing(ProfileId(1L), ProfileId(2L)) } returns false
                every { followCommandPort.save(any()) } returnsArgument 0

                useCase.invoke(FollowCommand(followerProfileId = 1L, followingProfileId = 2L))

                verify(exactly = 1) { followCommandPort.save(any()) }
            }
        }
        context("자기 자신 팔로우") {
            it("should throw IllegalArgumentException") {
                shouldThrow<IllegalArgumentException> {
                    useCase.invoke(FollowCommand(followerProfileId = 1L, followingProfileId = 1L))
                }
            }
        }
        context("이미 팔로우 중") {
            it("should be idempotent — no duplicate save") {
                every { profileQueryPort.findById(ProfileId(2L)) } returns mockk()
                every { followQueryPort.existsByFollowerAndFollowing(ProfileId(1L), ProfileId(2L)) } returns true

                useCase.invoke(FollowCommand(followerProfileId = 1L, followingProfileId = 2L))

                verify(exactly = 0) { followCommandPort.save(any()) }
            }
        }
        context("존재하지 않는 프로필 팔로우") {
            it("should throw ProfileNotFoundException") {
                every { profileQueryPort.findById(ProfileId(99L)) } returns null
                shouldThrow<ProfileNotFoundException> {
                    useCase.invoke(FollowCommand(followerProfileId = 1L, followingProfileId = 99L))
                }
            }
        }
    }
})
```

**Step 2: FollowUseCase 구현**

```kotlin
data class FollowCommand(val followerProfileId: Long, val followingProfileId: Long)

@Service
class FollowUseCase(
    private val followCommandPort: FollowCommandPort,
    private val followQueryPort: FollowQueryPort,
    private val profileQueryPort: ProfileQueryPort,
) : CommandUseCase<FollowCommand, Unit> {
    @Transactional
    override fun invoke(command: FollowCommand) {
        require(command.followerProfileId != command.followingProfileId) { "Cannot follow yourself" }
        profileQueryPort.findById(ProfileId(command.followingProfileId))
            ?: throw ProfileNotFoundException(ProfileId(command.followingProfileId))
        val followerProfileId = ProfileId(command.followerProfileId)
        val followingProfileId = ProfileId(command.followingProfileId)
        if (followQueryPort.existsByFollowerAndFollowing(followerProfileId, followingProfileId)) return
        followCommandPort.save(
            ProfileFollow.follow(
                id = ProfileFollowId(0L),
                followerProfileId = followerProfileId,
                followingProfileId = followingProfileId,
            )
        )
    }
}
```

**Step 3: UnfollowUseCase 동일 패턴으로 구현**

```kotlin
data class UnfollowCommand(val followerProfileId: Long, val followingProfileId: Long)

@Service
class UnfollowUseCase(...) : CommandUseCase<UnfollowCommand, Unit> {
    @Transactional
    override fun invoke(command: UnfollowCommand) {
        val followerProfileId = ProfileId(command.followerProfileId)
        val followingProfileId = ProfileId(command.followingProfileId)
        if (!followQueryPort.existsByFollowerAndFollowing(followerProfileId, followingProfileId)) return
        followCommandPort.delete(followerProfileId, followingProfileId)
    }
}
```

**Step 4: 테스트 + 커밋**

```bash
/Users/parkhanbin/git/study/anon-community/gradlew :app:usecase:test
git add app/usecase/
git commit -m "feat(usecase): add FollowUseCase and UnfollowUseCase with idempotency"
```

---

### Task 17: GetFollowersUseCase + GetFollowingUseCase + GetFollowingFeedUseCase

**Files:**
- Create: `app/usecase/src/main/kotlin/com/anoncommunity/app/usecase/follow/GetFollowersUseCase.kt`
- Create: `app/usecase/src/main/kotlin/com/anoncommunity/app/usecase/follow/GetFollowingUseCase.kt`
- Create: `app/usecase/src/main/kotlin/com/anoncommunity/app/usecase/feed/GetFollowingFeedUseCase.kt`
- Create: `app/usecase/src/test/kotlin/.../GetFollowingFeedUseCaseSpec.kt`

**Step 1: GetFollowingFeedUseCaseSpec**

```kotlin
class GetFollowingFeedUseCaseSpec : DescribeSpec({
    val followQueryPort = mockk<FollowQueryPort>()
    val postFeedPort = mockk<PostFeedPort>()
    val postStatsQueryPort = mockk<PostStatsQueryPort>()
    val useCase = GetFollowingFeedUseCase(followQueryPort, postFeedPort, postStatsQueryPort)

    describe("GetFollowingFeedUseCase") {
        context("팔로잉이 없는 경우") {
            it("should return empty result") {
                every { followQueryPort.findFollowingIds(ProfileId(1L), 1000) } returns emptyList()
                val result = useCase.invoke(GetFollowingFeedQuery(activeProfileId = 1L, pageable = DomainPageable()))
                result.content shouldBe emptyList()
            }
        }
        context("팔로잉이 있는 경우") {
            it("should return posts from following profiles") {
                every { followQueryPort.findFollowingIds(ProfileId(1L), 1000) } returns listOf(ProfileId(2L), ProfileId(3L))
                val mockPost = mockk<Post> { every { id } returns PostId(10L) }
                every { postFeedPort.getFollowingFeed(any(), any()) } returns PageResult(listOf(mockPost), 0, 20, 1L)
                every { postStatsQueryPort.findByPostIds(any()) } returns emptyMap()
                val result = useCase.invoke(GetFollowingFeedQuery(activeProfileId = 1L, pageable = DomainPageable()))
                result.content.size shouldBe 1
            }
        }
    }
})
```

**Step 2: 구현**

```kotlin
data class GetFollowingFeedQuery(val activeProfileId: Long, val pageable: DomainPageable)

@Service
class GetFollowingFeedUseCase(
    private val followQueryPort: FollowQueryPort,
    private val postFeedPort: PostFeedPort,
    private val postStatsQueryPort: PostStatsQueryPort,
) : QueryUseCase<GetFollowingFeedQuery, PageResult<PostWithStats>> {
    override fun invoke(query: GetFollowingFeedQuery): PageResult<PostWithStats> {
        val followingIds = followQueryPort.findFollowingIds(ProfileId(query.activeProfileId), limit = 1000)
        if (followingIds.isEmpty()) return PageResult(emptyList(), query.pageable.page, query.pageable.size, 0L)
        val feedResult = postFeedPort.getFollowingFeed(followingIds, query.pageable)
        val postIds = feedResult.content.map { it.id }
        val statsMap = postStatsQueryPort.findByPostIds(postIds)
        return feedResult.copy(content = feedResult.content.map { post ->
            PostWithStats(post = post, stats = statsMap[post.id])
        })
    }
}
```

**Step 3: PostFeedPort에 getFollowingFeed 메서드 추가**

```kotlin
// PostFeedPort.kt (domain/post/port)
interface PostFeedPort {
    fun getFeed(category: String?, pageable: DomainPageable): PageResult<Post>
    fun getFollowingFeed(followingProfileIds: List<ProfileId>, pageable: DomainPageable): PageResult<Post>  // 추가
}
```

**Step 4: GetFollowersUseCase + GetFollowingUseCase 동일 패턴으로 구현**

**Step 5: 테스트 + 커밋**

```bash
/Users/parkhanbin/git/study/anon-community/gradlew :app:usecase:test
git add app/usecase/ domain/post/
git commit -m "feat(usecase): add GetFollowingFeedUseCase, GetFollowersUseCase, GetFollowingUseCase"
```

---

## Phase 6: API 레이어

---

### Task 18: MemberController 프로필 엔드포인트 + FollowController + FeedController + AuthController

**Files:**
- Modify: `app/api/src/main/kotlin/com/anoncommunity/app/api/controller/MemberController.kt`
- Create: `app/api/src/main/kotlin/com/anoncommunity/app/api/controller/FollowController.kt`
- Create: `app/api/src/main/kotlin/com/anoncommunity/app/api/controller/ProfileController.kt`
- Modify: `app/api/src/main/kotlin/com/anoncommunity/app/api/controller/FeedController.kt`
- Modify: `app/api/src/main/kotlin/com/anoncommunity/app/api/controller/AuthController.kt`
- Modify: `app/api/src/main/kotlin/com/anoncommunity/app/api/dto/Requests.kt`
- Modify: `app/api/src/main/kotlin/com/anoncommunity/app/api/dto/Responses.kt`

**Step 1: Responses.kt에 신규 DTO 추가**

```kotlin
// ProfileResponse.kt (또는 Responses.kt에 추가)
data class ProfileResponse(
    val id: Long,
    val memberId: Long,
    val nickname: String,
    val bio: String?,
    val postCount: Int,
    val followerCount: Int,
    val followingCount: Int,
    val isDefault: Boolean,
    val isFollowing: Boolean = false,
    val isMe: Boolean = false,
    val createdAt: String,
) {
    companion object {
        fun from(profile: MemberProfile, isFollowing: Boolean = false, isMe: Boolean = false): ProfileResponse =
            ProfileResponse(
                id = profile.id.value,
                memberId = profile.memberId.value,
                nickname = profile.nickname.value,
                bio = profile.bio.value.ifBlank { null },
                postCount = profile.postCount,
                followerCount = profile.followerCount,
                followingCount = profile.followingCount,
                isDefault = profile.isDefault,
                isFollowing = isFollowing,
                isMe = isMe,
                createdAt = profile.createdAt.toString(),
            )
    }
}
```

**Step 2: MemberController 업데이트**

```kotlin
// 기존 X-Member-Id 기반 → X-Profile-Id도 추가
@PostMapping("/me/profiles")
fun createProfile(
    @RequestHeader("X-Member-Id") memberId: Long,
    @Valid @RequestBody request: CreateProfileRequest,
): ResponseEntity<ProfileResponse> { ... }

@GetMapping("/me/profiles")
fun getMyProfiles(@RequestHeader("X-Member-Id") memberId: Long): ResponseEntity<List<ProfileResponse>> { ... }

@PutMapping("/me/profiles/{profileId}")
fun updateProfile(
    @RequestHeader("X-Member-Id") memberId: Long,
    @PathVariable profileId: Long,
    @Valid @RequestBody request: UpdateProfileRequest,
): ResponseEntity<ProfileResponse> { ... }

@DeleteMapping("/me/profiles/{profileId}")
fun deleteProfile(
    @RequestHeader("X-Member-Id") memberId: Long,
    @PathVariable profileId: Long,
): ResponseEntity<Void> { ... }
```

**Step 3: ProfileController (공개 프로필)**

```kotlin
@RestController
@RequestMapping("/api/v1/profiles")
class ProfileController(
    private val getPublicProfileUseCase: GetPublicProfileUseCase,
    private val getFollowersUseCase: GetFollowersUseCase,
    private val getFollowingUseCase: GetFollowingUseCase,
) {
    @GetMapping("/{profileId}")
    fun getProfile(
        @PathVariable profileId: Long,
        @RequestHeader("X-Profile-Id", required = false) requesterProfileId: Long?,
    ): ResponseEntity<ProfileResponse> { ... }

    @GetMapping("/{profileId}/followers")
    fun getFollowers(@PathVariable profileId: Long, @RequestParam page: Int = 0, @RequestParam size: Int = 20): ResponseEntity<PageResponse<ProfileResponse>> { ... }

    @GetMapping("/{profileId}/following")
    fun getFollowing(@PathVariable profileId: Long, @RequestParam page: Int = 0, @RequestParam size: Int = 20): ResponseEntity<PageResponse<ProfileResponse>> { ... }
}
```

**Step 4: FollowController**

```kotlin
@RestController
@RequestMapping("/api/v1/profiles/{profileId}")
class FollowController(
    private val followUseCase: FollowUseCase,
    private val unfollowUseCase: UnfollowUseCase,
) {
    @PostMapping("/follow")
    fun follow(
        @PathVariable profileId: Long,
        @RequestHeader("X-Profile-Id") followerProfileId: Long,
    ): ResponseEntity<Void> {
        followUseCase.invoke(FollowCommand(followerProfileId, profileId))
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/follow")
    fun unfollow(
        @PathVariable profileId: Long,
        @RequestHeader("X-Profile-Id") followerProfileId: Long,
    ): ResponseEntity<Void> {
        unfollowUseCase.invoke(UnfollowCommand(followerProfileId, profileId))
        return ResponseEntity.noContent().build()
    }
}
```

**Step 5: FeedController — following feed 추가**

```kotlin
@GetMapping("/following")
fun getFollowingFeed(
    @RequestHeader("X-Profile-Id") activeProfileId: Long,
    @RequestParam cursor: String? = null,
    @RequestParam size: Int = 20,
): ResponseEntity<CursorPageResponse<PostDetailResponse>> { ... }
```

**Step 6: AuthController — switch-profile 추가**

```kotlin
@PostMapping("/switch-profile")
fun switchProfile(
    @RequestHeader("X-Member-Id") memberId: Long,
    @Valid @RequestBody request: SwitchProfileRequest,
): ResponseEntity<TokenResponse> {
    val newToken = switchProfileUseCase.invoke(SwitchProfileCommand(memberId, request.profileId))
    return ResponseEntity.ok(TokenResponse(accessToken = newToken))
}
```

**Step 7: 기존 Controller에서 X-Member-Id → X-Profile-Id 전환**

PostController, CommentController에서 `@RequestHeader("X-Member-Id") memberId: Long` → `@RequestHeader("X-Profile-Id") profileId: Long` 변경. UseCase command도 profileId 사용.

**Step 8: 컴파일 + API 테스트 수정**

```bash
/Users/parkhanbin/git/study/anon-community/gradlew :app:api:compileKotlin
/Users/parkhanbin/git/study/anon-community/gradlew :app:api:test
git add app/api/
git commit -m "feat(api): add profile CRUD, follow/unfollow, following feed, and switch-profile endpoints"
```

---

## Phase 7: CDC Consumer

---

### Task 19: FollowEventConsumer (followerCount/followingCount)

**Files:**
- Create: `app/consumer/src/main/kotlin/com/anoncommunity/app/consumer/domain/FollowEventConsumer.kt`
- Create: `app/consumer/src/test/kotlin/com/anoncommunity/app/consumer/domain/FollowEventConsumerSpec.kt`

**Step 1: 테스트 작성 (CommentEventConsumerSpec 패턴 참고)**

```kotlin
class FollowEventConsumerSpec : DescribeSpec({
    val profileCommandPort = mockk<ProfileCommandPort>()
    val processedEventPort = mockk<ProcessedEventPort>()
    val objectMapper: ObjectMapper = jacksonObjectMapper()
    val consumer = FollowEventConsumer(profileCommandPort, processedEventPort, objectMapper)

    beforeEach { clearMocks(profileCommandPort, processedEventPort) }

    describe("FollowEventConsumer") {
        context("ProfileFollowed event") {
            it("should increment follower and following counts") {
                every { processedEventPort.markProcessed("evt-1", "ProfileFollowed") } returns true
                every { profileCommandPort.incrementFollowerCount(ProfileId(2L)) } returns 1
                every { profileCommandPort.incrementFollowingCount(ProfileId(1L)) } returns 1

                val message = """{"eventId":"evt-1","eventType":"ProfileFollowed","followerProfileId":{"value":1},"followingProfileId":{"value":2}}"""
                consumer.consume(message)

                verify(exactly = 1) { profileCommandPort.incrementFollowerCount(ProfileId(2L)) }
                verify(exactly = 1) { profileCommandPort.incrementFollowingCount(ProfileId(1L)) }
            }

            it("should skip when already processed (idempotency)") {
                every { processedEventPort.markProcessed("evt-1", "ProfileFollowed") } returns false
                consumer.consume("""{"eventId":"evt-1","eventType":"ProfileFollowed","followerProfileId":{"value":1},"followingProfileId":{"value":2}}""")
                verify(exactly = 0) { profileCommandPort.incrementFollowerCount(any()) }
            }
        }
        context("ProfileUnfollowed event") {
            it("should decrement follower and following counts") {
                every { processedEventPort.markProcessed("evt-2", "ProfileUnfollowed") } returns true
                every { profileCommandPort.decrementFollowerCount(ProfileId(2L)) } returns 1
                every { profileCommandPort.decrementFollowingCount(ProfileId(1L)) } returns 1

                val message = """{"eventId":"evt-2","eventType":"ProfileUnfollowed","followerProfileId":{"value":1},"followingProfileId":{"value":2}}"""
                consumer.consume(message)

                verify(exactly = 1) { profileCommandPort.decrementFollowerCount(ProfileId(2L)) }
            }
        }
    }
})
```

**Step 2: ProfileCommandPort에 count 메서드 추가**

```kotlin
interface ProfileCommandPort {
    fun save(profile: MemberProfile): MemberProfile
    fun delete(id: ProfileId)
    fun incrementFollowerCount(profileId: ProfileId): Int   // UPDATE 쿼리 직접 (SELECT FOR UPDATE 회피)
    fun decrementFollowerCount(profileId: ProfileId): Int
    fun incrementFollowingCount(profileId: ProfileId): Int
    fun decrementFollowingCount(profileId: ProfileId): Int
    fun incrementPostCount(profileId: ProfileId): Int
    fun decrementPostCount(profileId: ProfileId): Int
}
```

**Step 3: ProfileCommandPortAdapter에 JPA UPDATE 쿼리 구현**

```kotlin
// MemberProfileRepository에 추가
@Modifying
@Query("UPDATE MemberProfileEntity p SET p.followerCount = GREATEST(0, p.followerCount + 1) WHERE p.id = :id")
fun incrementFollowerCount(@Param("id") id: Long): Int

@Modifying
@Query("UPDATE MemberProfileEntity p SET p.followerCount = GREATEST(0, p.followerCount - 1) WHERE p.id = :id")
fun decrementFollowerCount(@Param("id") id: Long): Int
// followingCount, postCount도 동일하게
```

**Step 4: FollowEventConsumer 구현**

```kotlin
@Component
class FollowEventConsumer(
    private val profileCommandPort: ProfileCommandPort,
    private val processedEventPort: ProcessedEventPort,
    private val objectMapper: ObjectMapper,
) {
    @KafkaListener(topics = ["profile-follow-events"])
    fun consume(message: String) {
        val event = objectMapper.readTree(message)
        val eventId = event["eventId"].asText()
        val eventType = event["eventType"].asText()
        if (!processedEventPort.markProcessed(eventId, eventType)) return

        val followerProfileId = ProfileId(event["followerProfileId"]["value"].asLong())
        val followingProfileId = ProfileId(event["followingProfileId"]["value"].asLong())

        when (eventType) {
            "ProfileFollowed" -> {
                profileCommandPort.incrementFollowerCount(followingProfileId)
                profileCommandPort.incrementFollowingCount(followerProfileId)
            }
            "ProfileUnfollowed" -> {
                profileCommandPort.decrementFollowerCount(followingProfileId)
                profileCommandPort.decrementFollowingCount(followerProfileId)
            }
        }
    }
}
```

**Step 5: 테스트 + 커밋**

```bash
/Users/parkhanbin/git/study/anon-community/gradlew :app:consumer:test
git add app/consumer/ infra/rdb/ domain/member/
git commit -m "feat(consumer): add FollowEventConsumer for follower/following count CDC"
```

---

## Phase 8: 프론트엔드

---

### Task 20: Auth Store 업데이트 (activeProfile, profiles, accounts)

**Files:**
- Modify: `frontend/src/stores/auth-store.ts`
- Modify: `frontend/src/lib/api/client.ts` (types)
- Create: `frontend/src/types/profile.ts`

**Step 1: 타입 정의**

```ts
// frontend/src/types/profile.ts
export interface ProfileResponse {
  id: number;
  memberId: number;
  nickname: string;
  bio: string | null;
  postCount: number;
  followerCount: number;
  followingCount: number;
  isDefault: boolean;
  isFollowing: boolean;
  isMe: boolean;
  createdAt: string;
}
```

**Step 2: Auth Store 변경**

```ts
// auth-store.ts
interface ActiveProfile {
  id: number;
  nickname: string;
  bio: string | null;
  postCount: number;
  followerCount: number;
  followingCount: number;
  isDefault: boolean;
}

interface LinkedAccount {
  memberId: number;
  activeProfileId: number;
  nickname: string;
  accessToken: string;
}

interface AuthState {
  memberId: number | null;
  activeProfile: ActiveProfile | null;
  profiles: ActiveProfile[];         // 내 모든 프로필
  accounts: LinkedAccount[];         // 기기에 연결된 계정들 (localStorage)
  isInitialized: boolean;

  // actions
  setAuth: (memberId: number, profile: ActiveProfile, profiles: ActiveProfile[]) => void;
  switchProfile: (profileId: number) => Promise<void>;
  addAccount: (account: LinkedAccount) => void;
  removeAccount: (memberId: number) => void;
  logout: () => void;
}
```

**Step 3: localStorage 기반 멀티계정 구현**

```ts
// accounts는 localStorage['linked_accounts']에 저장
// switchProfile → POST /auth/switch-profile → 새 JWT → store 업데이트
```

**Step 4: 커밋**

```bash
git add frontend/src/stores/ frontend/src/types/
git commit -m "feat(fe): update auth store with activeProfile, profiles, and multi-account support"
```

---

### Task 21: 공개 프로필 페이지 /profiles/[profileId]

**Files:**
- Create: `frontend/src/app/(main)/profiles/[profileId]/page.tsx`
- Create: `frontend/src/components/profile/profile-header.tsx`
- Create: `frontend/src/components/profile/follow-button.tsx`
- Create: `frontend/src/lib/api/profiles.ts`
- Create: `frontend/src/lib/hooks/use-follow.ts`

**Step 1: profiles API 클라이언트**

```ts
// frontend/src/lib/api/profiles.ts
export const profilesApi = {
  getProfile: (profileId: number) =>
    fetchAPI<ProfileResponse>(`/profiles/${profileId}`),
  getProfilePosts: (profileId: number, page = 0) =>
    fetchAPI<PageResponse<PostResponse>>(`/profiles/${profileId}/posts?page=${page}`),
  getFollowers: (profileId: number, page = 0) =>
    fetchAPI<PageResponse<ProfileResponse>>(`/profiles/${profileId}/followers?page=${page}`),
  getFollowing: (profileId: number, page = 0) =>
    fetchAPI<PageResponse<ProfileResponse>>(`/profiles/${profileId}/following?page=${page}`),
  follow: (profileId: number) =>
    fetchAPI<void>(`/profiles/${profileId}/follow`, { method: "POST" }),
  unfollow: (profileId: number) =>
    fetchAPI<void>(`/profiles/${profileId}/follow`, { method: "DELETE" }),
};
```

**Step 2: use-follow hook**

```ts
// use-follow.ts
export function useFollow(profileId: number) {
  const queryClient = useQueryClient();
  const followMutation = useMutation({
    mutationFn: () => profilesApi.follow(profileId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["profile", profileId] }),
  });
  const unfollowMutation = useMutation({
    mutationFn: () => profilesApi.unfollow(profileId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["profile", profileId] }),
  });
  return { follow: followMutation.mutate, unfollow: unfollowMutation.mutate,
           isFollowPending: followMutation.isPending, isUnfollowPending: unfollowMutation.isPending };
}
```

**Step 3: 프로필 페이지**

```tsx
// /profiles/[profileId]/page.tsx
"use client";

export default function ProfilePage({ params }: { params: { profileId: string } }) {
  const profileId = Number(params.profileId);
  const { data: profile, isLoading } = useQuery({
    queryKey: ["profile", profileId],
    queryFn: () => profilesApi.getProfile(profileId),
  });
  const [activeTab, setActiveTab] = useState<"posts" | "followers" | "following">("posts");

  // ProfileHeader (닉네임, bio, 팔로워/팔로잉 수, 팔로우 버튼)
  // 탭: 게시글 | 팔로워 | 팔로잉
  // 각 탭 콘텐츠: PostCard 목록 or ProfileCard 목록
}
```

**Step 4: 커밋**

```bash
git add frontend/src/app/(main)/profiles/ frontend/src/lib/api/profiles.ts frontend/src/lib/hooks/use-follow.ts
git commit -m "feat(fe): add public profile page with follow/unfollow and tabs"
```

---

### Task 22: 피드 팔로잉 탭 + PostCard 프로필 링크

**Files:**
- Modify: `frontend/src/app/(main)/page.tsx`
- Modify: `frontend/src/components/post/post-card.tsx`
- Modify: `frontend/src/lib/api/feed.ts`

**Step 1: 피드 API에 following feed 추가**

```ts
// feed.ts
export const feedApi = {
  ...
  getFollowing: (cursor?: string, size = 20) =>
    fetchAPI<CursorPageResponse<PostDetailResponse>>(`/feed/following?cursor=${cursor ?? ""}&size=${size}`),
};
```

**Step 2: 메인 피드 페이지에 탭 추가**

```tsx
// page.tsx
const [feedTab, setFeedTab] = useState<"all" | "following">("all");

// 탭 UI
<div className="flex gap-2 mb-4 border-b">
  <button onClick={() => setFeedTab("all")} className={...}>전체</button>
  <button onClick={() => setFeedTab("following")} className={...}>팔로잉</button>
</div>

{feedTab === "all" ? <FeedList /> : <FollowingFeedList />}
```

**Step 3: PostCard 닉네임 → 프로필 링크**

```tsx
// post-card.tsx
// 기존: <span>{post.authorNickname}</span>
// 변경:
<Link href={`/profiles/${post.authorProfileId}`} className="hover:underline">
  {post.authorNickname}
</Link>
```

**Step 4: 커밋**

```bash
git add frontend/src/
git commit -m "feat(fe): add following feed tab and profile links on PostCard"
```

---

### Task 23: Settings 프로필 관리 + My Page 업데이트

**Files:**
- Modify: `frontend/src/app/(main)/settings/page.tsx`
- Modify: `frontend/src/app/(main)/my/page.tsx`
- Create: `frontend/src/components/profile/profile-switcher.tsx`
- Modify: `frontend/src/components/layout/left-sidebar.tsx`

**Step 1: Settings — 프로필 관리 섹션 추가**

```tsx
// settings/page.tsx에 추가
<Card>
  <CardHeader><CardTitle>프로필 관리</CardTitle></CardHeader>
  <CardContent>
    {profiles.map(profile => (
      <div key={profile.id} className="flex items-center justify-between py-2">
        <span>{profile.nickname} {profile.isDefault && <Badge>기본</Badge>}</span>
        <div className="flex gap-2">
          <Button size="sm" onClick={() => handleSwitchProfile(profile.id)}>전환</Button>
          <Button size="sm" variant="outline">수정</Button>
          {!profile.isDefault && <Button size="sm" variant="destructive">삭제</Button>}
        </div>
      </div>
    ))}
    {profiles.length < 5 && (
      <Button onClick={() => setShowCreateProfile(true)}>+ 프로필 추가</Button>
    )}
  </CardContent>
</Card>

<Card>
  <CardHeader><CardTitle>계정 관리</CardTitle></CardHeader>
  <CardContent>
    {accounts.map(account => (
      <div key={account.memberId}>닉네임: {account.nickname}</div>
    ))}
    <Button onClick={() => setShowAddAccount(true)}>계정 추가</Button>
  </CardContent>
</Card>
```

**Step 2: LeftSidebar — 활성 프로필 카드 업데이트**

```tsx
// left-sidebar.tsx — 기존 사용자 카드를 activeProfile 기반으로 변경
{isInitialized && isLoggedIn && activeProfile && (
  <Link href="/profiles/me">
    <div className="p-3 rounded-lg border hover:bg-muted/50">
      <p className="font-medium text-sm">{activeProfile.nickname}</p>
      <p className="text-xs text-muted-foreground">게시글 {activeProfile.postCount}개</p>
    </div>
  </Link>
)}
```

**Step 3: 커밋**

```bash
git add frontend/src/
git commit -m "feat(fe): add profile management in settings and update sidebar with activeProfile"
```

---

## Phase 9: 빌드 검증

---

### Task 24: 전체 빌드 검증

**Step 1: 백엔드 전체 테스트**

```bash
/Users/parkhanbin/git/study/anon-community/gradlew test
# Expected: BUILD SUCCESSFUL
# 실패 시 오류 메시지 확인 후 수정
```

**Step 2: 프론트엔드 빌드**

```bash
export PATH="$HOME/.nvm/versions/node/v20.20.0/bin:$PATH"
cd /Users/parkhanbin/git/study/anon-community/frontend && npm run build
# Expected: ✓ Compiled successfully
# 타입 오류 있으면 수정 후 재시도
```

**Step 3: 최종 커밋**

```bash
git add .
git commit -m "chore: verify full build — backend tests pass, frontend compiled successfully"
```
