# Follow/Following & Multi-Profile 설계서

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:writing-plans to turn this design into an implementation plan.

**Goal:** 프로필 기반 팔로우/팔로잉 시스템과 멀티프로필(페르소나) 기능을 추가하여 MAU 5천만 규모의 소셜 커뮤니티 플랫폼으로 발전시킨다.

**Architecture:** Phased approach — Phase 1 (도메인 리팩토링: Member→Profile 분리), Phase 2 (Follow 도메인), Phase 3 (UseCase + API), Phase 4 (팔로잉 피드 + CDC), Phase 5 (프론트엔드)

**Tech Stack:** Kotlin/Spring Boot (backend), Next.js 16 App Router + Tailwind CSS 4 + shadcn/ui (frontend), OpenSearch, Redis, Kafka CDC

---

## 확정된 요구사항

| 항목 | 결정 |
|------|------|
| 팔로우 방식 | 단방향 (Twitter/X 스타일) — 승인 불필요 |
| 팔로우 타깃 | 프로필 기반 (MemberProfile) — 계정이 아닌 페르소나 단위 |
| 공개 프로필 | `/profiles/{profileId}` 신규 페이지 |
| 멀티프로필 | 하나의 계정(Member)에 복수 MemberProfile (최대 5개) |
| 멀티계정 | 클라이언트 localStorage에 복수 JWT 저장 (기기 수준) |
| 팔로잉 피드 | Fan-out on Read + Redis followingIds 캐시 |
| 카운트 관리 | CDC (Debezium → Kafka → FollowEventConsumer) 비정규화 |

---

## 이미 구현됨 (재구현 금지)

- Member aggregate (nickname, bio, postCount, status 포함) ✅
- Post aggregate (authorId: MemberId) ✅
- Comment aggregate (authorId: MemberId) ✅
- PostLike aggregate (memberId: MemberId) ✅
- 전체 피드 (GetFeedUseCase + OpenSearch) ✅
- 검색 (SearchPostUseCase) ✅
- JWT 인증 (Gateway AuthFilter) ✅

---

## 아키텍처 설계

### 핵심 개념 분리

```
Member (인증 엔티티)            MemberProfile (공개 페르소나)
├── id: MemberId               ├── id: ProfileId
├── passwordHash: String?      ├── memberId: MemberId   ← 소유자
├── status: MemberStatus       ├── nickname: MemberNickname
├── preferredLanguage          ├── bio: Bio
└── createdAt / updatedAt      ├── postCount: Int
                               ├── followerCount: Int
                               ├── followingCount: Int
                               ├── isDefault: Boolean
                               └── createdAt / updatedAt
```

**Before**: Post.authorId = MemberId (계정 단위)
**After**: Post.authorProfileId = ProfileId (페르소나 단위)

회원가입 시 default MemberProfile 자동 생성 → 기존 사용자 경험 유지.

---

## DB Schema 변경

```sql
-- 1. members 테이블: 인증 정보만 남김
ALTER TABLE members
  DROP COLUMN nickname,
  DROP COLUMN bio,
  DROP COLUMN post_count;

-- 2. member_profiles: 신규
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

-- 3. profile_follows: 신규
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

-- 4. posts: author_id → author_profile_id
ALTER TABLE posts
  DROP FOREIGN KEY fk_posts_member,
  CHANGE COLUMN author_id author_profile_id BIGINT NOT NULL,
  ADD CONSTRAINT fk_posts_profile FOREIGN KEY (author_profile_id) REFERENCES member_profiles(id);

-- 5. comments: 동일
ALTER TABLE comments
  DROP FOREIGN KEY fk_comments_member,
  CHANGE COLUMN author_id author_profile_id BIGINT NOT NULL,
  ADD CONSTRAINT fk_comments_profile FOREIGN KEY (author_profile_id) REFERENCES member_profiles(id);

-- 6. post_likes: member_id → profile_id
ALTER TABLE post_likes
  DROP FOREIGN KEY fk_likes_member,
  CHANGE COLUMN member_id profile_id BIGINT NOT NULL,
  ADD CONSTRAINT fk_likes_profile FOREIGN KEY (profile_id) REFERENCES member_profiles(id);
```

---

## JWT & Auth Flow

### JWT Claims 변경

```
Before: { memberId, nickname, role, exp }
After:  { memberId, activeProfileId, exp }
```

### Gateway 헤더 전파

```
X-Member-Id:  {memberId}        ← 기존 유지
X-Profile-Id: {activeProfileId} ← 신규 추가
```

AuthFilter가 JWT 파싱 시 두 헤더 모두 설정. UseCase는 `X-Profile-Id`를 사용하여 작성자/권한 확인.

### 프로필 전환 API

```
POST /api/v1/auth/switch-profile
Body: { "profileId": 42 }

Logic:
1. profileQueryPort.findById(profileId) — 없으면 ProfileNotFoundException
2. profile.memberId != memberId → throw ProfileNotFoundException (정보 노출 방지)
3. JWT 재발행: { memberId, activeProfileId: 42, exp: now+1h }
4. return new AccessToken
```

---

## API 설계

### 프로필 API

```
POST   /api/v1/members/me/profiles              — 프로필 생성 (max 5)
GET    /api/v1/members/me/profiles              — 내 프로필 목록
DELETE /api/v1/members/me/profiles/{profileId} — 프로필 삭제 (default 불가)
PUT    /api/v1/members/me/profiles/{profileId} — 닉네임/소개글 수정
GET    /api/v1/profiles/{profileId}            — 공개 프로필 조회
GET    /api/v1/profiles/{profileId}/posts      — 프로필의 게시글 목록
```

### 팔로우 API

```
POST   /api/v1/profiles/{profileId}/follow     — 팔로우
DELETE /api/v1/profiles/{profileId}/follow     — 언팔로우
GET    /api/v1/profiles/{profileId}/followers  — 팔로워 목록 (cursor)
GET    /api/v1/profiles/{profileId}/following  — 팔로잉 목록 (cursor)
```

### 피드 API (추가)

```
GET    /api/v1/feed/following?cursor=          — 팔로잉 피드 (신규)
POST   /api/v1/auth/switch-profile             — 프로필 전환
```

---

## 팔로잉 피드 아키텍처 (Fan-out on Read)

```
GET /api/v1/feed/following
  1. Redis GET "following:{activeProfileId}" → Set<ProfileId> (TTL 10분)
     Cache miss → DB followingIds 조회 → Redis SET
  2. followingIds 빈 경우 → 빈 결과 반환
  3. followingIds > 1000 → 최근 1000개만 (Twitter 방식)
  4. OpenSearch: bool filter { terms: { authorProfileId: followingIds } }
               + searchAfter cursor pagination
  5. RDB batch join: postStatsQueryPort.findByPostIds(postIds)
  6. 결과 반환
```

---

## 프론트엔드 변경

### 신규

- `/profiles/[profileId]/page.tsx` — 공개 프로필 (닉네임, 소개, 팔로워/팔로잉, 팔로우 버튼, 게시글)
- 피드 탭: "전체" | "팔로잉"

### 변경

- Auth Store: `activeProfile`, `profiles[]`, `accounts[]` (멀티계정) 추가
- PostCard: 닉네임 → `/profiles/{authorProfileId}` 링크
- My Page: 프로필 관리 탭 추가
- Settings: 프로필 생성/수정/삭제, 계정 추가 (localStorage)
- LeftSidebar: 활성 프로필 카드 + 전환 드롭다운

---

## 테스트 전략

### 기존 Spec 수정

- `MemberSpec.kt` — nickname/bio/postCount 필드 제거 반영
- `PostSpec.kt` (있다면) — authorProfileId 반영
- `CreatePostUseCaseSpec.kt` — authorProfileId 사용
- `CreateCommentUseCaseSpec.kt` — authorProfileId 사용
- `PostLikeSpec.kt` — profileId 사용
- `GatewayAuthFilterSpec.kt` — X-Profile-Id 헤더 추가

### 신규 Spec (TDD)

- `MemberProfileSpec.kt` — create, updateProfile, incrementFollowerCount, isDefault 보장
- `ProfileFollowSpec.kt` — follow (자기 팔로우 금지), unfollow
- `CreateProfileUseCaseSpec.kt` — max 5개 제한, default 보장
- `SwitchProfileUseCaseSpec.kt` — 소유자 검증, 타인 프로필 접근 시 ProfileNotFoundException
- `FollowUseCaseSpec.kt` — 중복 팔로우, 자기 팔로우 금지
- `UnfollowUseCaseSpec.kt` — 존재하지 않는 팔로우 처리
- `GetFollowingFeedUseCaseSpec.kt` — 빈 팔로잉 → 빈 피드
- `FollowEventConsumerSpec.kt` — followerCount/followingCount 증감, idempotency

---

## 구현 순서

```
Phase 1 (도메인 리팩토링):
  Task 1: ProfileId 값 객체 + MemberProfile 도메인
  Task 2: Member 도메인 리팩토링 (nickname/bio/postCount 제거)
  Task 3: Post 도메인 리팩토링 (authorProfileId)
  Task 4: Comment 도메인 리팩토링 (authorProfileId)
  Task 5: PostLike 도메인 리팩토링 (profileId)

Phase 2 (Follow 도메인):
  Task 6: ProfileFollow 도메인

Phase 3 (Infra):
  Task 7: DB Schema 변경 (init-db SQL)
  Task 8: MemberProfileEntity + JPA 어댑터
  Task 9: ProfileFollowEntity + JPA 어댑터
  Task 10: PostEntity/CommentEntity/PostLikeEntity 업데이트
  Task 11: PostDocument (OpenSearch) authorProfileId 업데이트

Phase 4 (Gateway):
  Task 12: AuthFilter X-Profile-Id 헤더 추가 + JWT claims 변경

Phase 5 (UseCase):
  Task 13: CreateMemberUseCase → default Profile 자동 생성
  Task 14: CreateProfileUseCase
  Task 15: SwitchProfileUseCase
  Task 16: UpdateProfileUseCase
  Task 17: GetPublicProfileUseCase
  Task 18: FollowUseCase + UnfollowUseCase
  Task 19: GetFollowersUseCase + GetFollowingUseCase
  Task 20: GetFollowingFeedUseCase (Redis 캐시 포함)

Phase 6 (API):
  Task 21: MemberController 프로필 엔드포인트 추가
  Task 22: FollowController 신규
  Task 23: FeedController following feed 추가
  Task 24: AuthController switch-profile 추가
  Task 25: 기존 PostController/CommentController X-Profile-Id 전환

Phase 7 (CDC Consumer):
  Task 26: FollowEventConsumer (followerCount/followingCount)

Phase 8 (Frontend):
  Task 27: Auth Store 업데이트 (activeProfile, profiles, accounts)
  Task 28: 공개 프로필 페이지 /profiles/[profileId]
  Task 29: 피드 팔로잉 탭
  Task 30: PostCard 프로필 링크
  Task 31: My Page 프로필 관리
  Task 32: Settings 계정 관리
  Task 33: LeftSidebar 프로필 전환 UI

Phase 9 (Build Verification):
  Task 34: 전체 빌드 검증 (backend + frontend)
```
