# Anonymous Community Project

익명 커뮤니티 플랫폼 — Spring Boot 3.4 (Kotlin) 백엔드 + Next.js 16 (App Router) 프론트엔드

## Project Overview

**도메인**: 익명 게시판 커뮤니티
**아키텍처**: Clean Architecture (Hexagonal) + Event-Driven (Kafka CDC)
**테스트**: Kotest 5.9.1 (DescribeSpec) — 모든 테스트는 `*Spec.kt` 네이밍 규칙

### Tech Stack

**Backend**:
- Language: Kotlin 1.9.25
- Framework: Spring Boot 3.4.0
- Database: MySQL 8.0 (Aurora)
- Search: OpenSearch 2.18.0
- Message Broker: Kafka 7.8.0 (KRaft mode)
- Cache: Redis 7.4.1
- Test: Kotest 5.9.1, MockK 1.13.13, Spring Boot Test 3.4

**Frontend**:
- Framework: Next.js 16.1.6 (App Router)
- Language: TypeScript 5.9.3
- UI: React 19.2.3, Tailwind CSS 4, shadcn/ui
- State: Zustand, React Query (TanStack Query v5)
- Build: Turbopack

**Infrastructure**:
- Containerization: Docker Compose
- Local Development: `local-dev.sh` (backend), `local-run.sh` (all services)

## Architecture

### Layered Clean Architecture

```
┌─────────────────────────────────────────┐
│  app/api (Controllers + Exception)     │ ← REST API endpoints
├─────────────────────────────────────────┤
│  app/gateway (Security + Filters)       │ ← JWT auth, CORS, rate limit
├─────────────────────────────────────────┤
│  app/usecase (Application Services)     │ ← Business workflows
├─────────────────────────────────────────┤
│  domain/* (Aggregates + Ports)          │ ← Core business logic
├─────────────────────────────────────────┤
│  infra/* (Adapters)                     │ ← RDB, Redis, OpenSearch, Kafka
└─────────────────────────────────────────┘
```

**Key Modules**:
- `domain/member` — Member aggregate (회원)
- `domain/post` — Post aggregate (게시글)
- `domain/comment` — Comment aggregate (댓글)
- `domain/like` — PostLike aggregate (좋아요)
- `contract` — Shared value objects (DomainPageable, SortDirection, PageResult)

**Ports & Adapters**:
- **Command Ports**: `MemberCommandPort`, `PostCommandPort`, etc. (write operations)
- **Query Ports**: `MemberQueryPort`, `PostStatsQueryPort`, etc. (read operations)
- **Feed Port**: `PostFeedPort` (OpenSearch 기반 feed 조회)
- **Adapters**: `*PortAdapter` classes in `infra/rdb`, `infra/opensearch`

### Event-Driven CDC

**Debezium → Kafka → Consumer**:
- MySQL binlog → Debezium → Kafka topics (`mysql-server.anon_community.posts`, etc.)
- CDC 이벤트 → Consumer (`PostCdcConsumer`, etc.) → 도메인 이벤트 발행
- 도메인 이벤트 → Event handler (`PostEventConsumer`, etc.) → 통계 업데이트

**Key Events**:
- `PostCreated`, `PostDeleted` → `member.postCount` 증감
- `PostLiked`, `PostUnliked` → `post_stats.likeCount` 증감
- `CommentCreated`, `CommentDeleted` → `post_stats.commentCount` 증감

## Project Structure

```
anon-community/
├── app/
│   ├── api/          # REST controllers, DTOs, exception handlers
│   ├── gateway/      # Security filters, JWT provider, CORS config
│   ├── usecase/      # Application services (UseCases)
│   └── consumer/     # Kafka CDC consumers, event handlers
├── domain/
│   ├── member/       # Member aggregate
│   ├── post/         # Post aggregate + PostStats
│   ├── comment/      # Comment aggregate
│   └── like/         # PostLike aggregate
├── contract/         # Shared models (DomainPageable, PageResult)
├── infra/
│   ├── rdb/          # JPA entities, repositories, adapters
│   ├── opensearch/   # OpenSearch index writer, feed port adapter
│   ├── kafka/        # Kafka producer config
│   └── redis/        # Redis config (future use)
├── frontend/
│   ├── src/
│   │   ├── app/            # Next.js App Router pages
│   │   │   ├── (auth)/     # Login, Signup
│   │   │   └── (main)/     # Feed, Post detail, Search, Settings
│   │   ├── components/     # React components
│   │   ├── lib/            # API client, hooks, utils
│   │   ├── stores/         # Zustand auth store
│   │   └── types/          # TypeScript types
│   └── public/
├── docs/plans/       # Design documents, implementation plans
├── docker/           # Dockerfiles
├── init-db/          # MySQL init scripts
├── init-opensearch/  # OpenSearch init scripts
└── debezium/         # Debezium connector configs
```

## Key Design Decisions

### Backend

**1. Domain-Driven Design (DDD)**
- Aggregates: `Member`, `Post`, `Comment`, `PostLike`
- Value Objects: `MemberNickname`, `PostTitle`, `PostContent`, `Bio`, etc.
- Domain Services: None yet (logic in aggregates)
- Events: Domain events for cross-aggregate coordination

**2. Port & Adapter Pattern**
- Ports define contracts in `domain/*/port/`
- Adapters implement in `infra/*/adapter/`
- Dependency inversion: domain → contract ← infra

**3. Stats Denormalization**
- `post_stats` table (postId, likeCount, commentCount, viewCount)
- Updated via event handlers (`PostEventConsumer`, `LikeEventConsumer`, `CommentEventConsumer`)
- Feed API enrichment: OpenSearch feed + RDB stats batch join (`PostStatsQueryPort.findByPostIds`)

**4. Search Strategy**
- Feed: OpenSearch cursor-based pagination (`searchAfter`)
- Search: OpenSearch with keyword + category + tag filters
- Feed endpoint: returns `PostDetailResponse` (post + stats)

**5. i18n Strategy**
- Accept-Language header → `X-Language` / `X-Country` (gateway filter)
- Stored in `member.preferredLanguage` (ko, en, ja, zh)
- Translation: 별도 번역 API 호출 (future: Papago, DeepL)

### Frontend

**1. App Router (Next.js 16)**
- Route groups: `(auth)`, `(main)`
- Layouts: auth layout (centered), main layout (header + FAB)
- SSR: Feed 초기 페이지 (`serverFetch`)

**2. State Management**
- **Global auth**: Zustand (`useAuthStore`)
- **Server state**: React Query (`useInfiniteQuery` for feed/search)
- **Local UI state**: `useState`

**3. API Architecture**
- Proxy pattern: `/api/proxy/[...path]` → backend API
- Auth routes: `/api/auth/login`, `/api/auth/signup`, etc. (route handlers)
- Client: `fetchAPI()` with auto-retry and token refresh

**4. Component Strategy**
- shadcn/ui for base components (Button, Card, Input, etc.)
- Custom components in `components/` (PostCard, CommentList, etc.)
- Shared utilities in `lib/utils`

**5. UX Enhancements (최근 추가)**
- ✅ 한국어 로컬라이제이션 (19 files)
- ✅ FAB 글쓰기 버튼 (fixed bottom-right)
- ✅ Feed 카드에 stats 표시 (likeCount, commentCount, viewCount)
- ✅ Sticky 카테고리 탭 (`sticky top-14 z-40`)
- ✅ 정렬 옵션 (최신순/오래된순)

## Development Workflow

### Backend Development

**1. Running locally**
```bash
# Start all services (MySQL, OpenSearch, Kafka, Redis, Debezium)
./local-run.sh

# Start backend only (assumes services already running)
./local-dev.sh

# Run tests
./gradlew test

# Run specific module tests
./gradlew :app:usecase:test
./gradlew :domain:member:test
```

**2. Adding a new UseCase**
1. Define command/query in `app/usecase/src/main/kotlin/com/anoncommunity/app/usecase/{domain}/`
2. Implement UseCase class (implements `CommandUseCase` or `QueryUseCase`)
3. Inject required ports (e.g., `MemberCommandPort`, `PostQueryPort`)
4. Write Kotest spec in `app/usecase/src/test/kotlin/.../` (name: `*UseCaseSpec.kt`)
5. Mock all dependencies with MockK
6. Verify tests pass: `./gradlew :app:usecase:test`

**3. Adding a new domain aggregate**
1. Create module: `domain/{aggregate-name}/`
2. Define aggregate class (e.g., `Member`, `Post`)
3. Define value objects (e.g., `MemberNickname`, `PostTitle`)
4. Define ports in `port/` (e.g., `MemberCommandPort`, `MemberQueryPort`)
5. Write Kotest specs for aggregate + value objects
6. Implement adapters in `infra/rdb/adapter/`

**4. Test conventions**
- Framework: Kotest 5.9.1 (DescribeSpec)
- Naming: `*Spec.kt` (NOT `*Test.kt`)
- Mocking: MockK 1.13.13
- Structure: `describe("ClassName") { it("should ...") { ... } }`
- Assertions: Kotest matchers (`shouldBe`, `shouldThrow`, etc.)

### Frontend Development

**1. Running locally**
```bash
cd frontend
npm install
npm run dev  # http://localhost:3000
```

**2. Building for production**
```bash
# Requires Node.js >= 20.9.0
cd frontend
npm run build
npm run start
```

**3. Adding a new page**
1. Create route in `src/app/(main)/{route}/page.tsx`
2. Add client component if needed (`"use client"`)
3. Use `serverFetch` for SSR data
4. Add to header navigation if needed

**4. Adding a new API endpoint**
- Proxy: Auto-routed via `/api/proxy/[...path]`
- Custom: Create route handler in `src/app/api/{route}/route.ts`

**5. Styling conventions**
- Tailwind utility classes (prefer over custom CSS)
- shadcn/ui components for consistency
- Korean text for all user-facing strings
- Responsive: mobile-first design

## Testing Strategy

**Coverage Target**: 50%+ overall, 100% for business logic layers

**Test Pyramid**:
```
           E2E (future)
         ─────────────
      Integration (@WebMvcTest)
    ──────────────────────────
  Unit (MockK + Kotest DescribeSpec)
```

**What to test**:
- ✅ Domain aggregates: All state transitions, validation rules
- ✅ UseCases: Happy path + edge cases (exceptions, validation)
- ✅ Value Objects: Validation rules
- ✅ Gateway Filters: Header processing, JWT validation
- ✅ Event Consumers: Idempotency, event handling
- ✅ Controllers (Integration): Request/response mapping, validation
- ✅ Exception Handlers: All exception types → correct HTTP status
- ⚠️ Adapters: Entity mapping (basic coverage)
- ❌ CDC Consumers: Testcontainers-based (deferred)

**What NOT to test**:
- Spring Boot auto-configuration
- Third-party libraries (Kafka, OpenSearch clients)
- Simple DTOs without logic

## API Conventions

### REST Endpoints

**Base URL**: `http://localhost:8080/api/v1`

**Auth**:
- `POST /auth/signup` — 회원가입
- `POST /auth/login` — 로그인
- `POST /auth/refresh` — 토큰 갱신
- `POST /auth/logout` — 로그아웃

**Member**:
- `GET /members/me` — 내 정보 조회
- `PUT /members/me/language` — 선호 언어 변경

**Feed**:
- `GET /feed?category={cat}&page=0&size=20&sortBy=createdAt&sortDirection=DESC` — 피드 조회

**Post**:
- `POST /posts` — 게시글 작성
- `GET /posts/{id}` — 게시글 조회
- `PUT /posts/{id}` — 게시글 수정
- `DELETE /posts/{id}` — 게시글 삭제
- `POST /posts/{id}/like` — 좋아요
- `DELETE /posts/{id}/like` — 좋아요 취소
- `GET /posts/{id}/comments` — 댓글 목록
- `POST /posts/{id}/comments` — 댓글 작성
- `DELETE /posts/{postId}/comments/{commentId}` — 댓글 삭제
- `POST /posts/{id}/translate?targetLanguage=ko` — 게시글 번역

**Search**:
- `GET /search/posts?keyword={kw}&tags={tag1,tag2}&category={cat}&cursor={cursor}` — 검색

### Response Format

**Success (200, 201)**:
```json
{
  "id": 1,
  "title": "Post Title",
  "content": "Post content",
  "category": "DAILY",
  "tags": ["tag1", "tag2"],
  "authorId": 100,
  "likeCount": 5,
  "commentCount": 3,
  "viewCount": 100,
  "createdAt": "2026-02-20T10:00:00",
  "updatedAt": "2026-02-20T10:00:00"
}
```

**Error (4xx, 5xx)**:
```json
{
  "code": "POST_NOT_FOUND",
  "message": "Post not found: 123"
}
```

**Pagination (Page-based)**:
```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

**Pagination (Cursor-based)**:
```json
{
  "content": [...],
  "size": 20,
  "searchAfter": "encoded-cursor-string",
  "hasNext": true
}
```

## Common Commands

```bash
# Backend
./gradlew test                    # Run all tests
./gradlew :app:api:compileKotlin  # Compile API module
./gradlew bootRun                 # Run Spring Boot app
./local-dev.sh                    # Start backend (assumes services running)
./local-run.sh                    # Start all services (docker-compose up)

# Frontend
cd frontend && npm run dev        # Dev server (http://localhost:3000)
cd frontend && npm run build      # Production build
cd frontend && npm run lint       # ESLint

# Docker
docker-compose up -d              # Start all services
docker-compose down               # Stop all services
docker-compose logs -f {service}  # View logs

# Database
docker exec -it anon-mysql mysql -uroot -proot anon_community
docker exec -it anon-opensearch curl -X GET "localhost:9200/_cat/indices?v"

# Kafka
docker exec -it anon-kafka kafka-topics --list --bootstrap-server localhost:9092
docker exec -it anon-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic mysql-server.anon_community.posts --from-beginning
```

## Git Workflow

**Branching Strategy**:
- `master` — Production-ready code
- `feat/*` — Feature branches (e.g., `feat/frontend`, `feat/ux-improvements`)
- `fix/*` — Bug fix branches

**Commit Message Convention**:
```
<type>(<scope>): <subject>

type: feat, fix, refactor, test, docs, chore
scope: be, fe, infra, domain, api, etc.
subject: Imperative mood, lowercase, no period

Examples:
feat(fe): add sticky category tabs with sort dropdown
fix(be): handle null stats in feed API enrichment
test(domain): add MemberSpec for state transitions
```

**PR Template**:
```markdown
## Summary
- Bullet 1
- Bullet 2

## Test Plan
- [ ] Backend tests pass (`./gradlew test`)
- [ ] Frontend build succeeds (`npm run build`)
- [ ] Manual smoke test: {scenario}
```

## Deployment (Future)

**Target**: AWS EKS (Kubernetes)
- Backend: Spring Boot Docker image (multi-arch: amd64 + arm64/Graviton)
- Frontend: Next.js standalone build in Docker
- Database: Aurora MySQL (RDS)
- Search: OpenSearch Service (managed)
- Message Broker: MSK (Kafka managed)
- Cache: ElastiCache (Redis)

**Build Pipeline** (Future):
1. Run tests (`./gradlew test`, `npm run build`)
2. Build Docker images (backend + frontend)
3. Push to ECR
4. Deploy to EKS via ArgoCD or Flux

## Key Metrics & Observability

**Planned** (not yet implemented):
- APM: Datadog
- Logs: Datadog (structured JSON logs)
- Metrics: Spring Boot Actuator → Prometheus → Datadog
- Alerts: High error rate, CDC lag, OpenSearch indexing delay

## Known Issues & TODOs

**Current Limitations**:
- [ ] No integration tests for CDC flow (Testcontainers-based deferred)
- [ ] No E2E tests (Playwright/Cypress future)
- [ ] Popularity sort (`likeCount DESC`) requires OpenSearch schema denormalization
- [ ] Translation API not implemented (번역 버튼은 UI만 있음)
- [ ] Dark mode toggle (UI 준비만)
- [ ] Redis cache layer not used yet (infra 준비만)

**Technical Debt**:
- [ ] Replace in-memory translation with external API
- [ ] Add circuit breaker for external API calls (Resilience4j)
- [ ] Add distributed tracing (Zipkin or Datadog APM)
- [ ] Refactor CDC consumers to handle schema evolution

## Troubleshooting

**Backend won't start**:
1. Check if MySQL/OpenSearch/Kafka are running: `docker ps`
2. Check logs: `./gradlew bootRun --info`
3. Verify DB schema: `docker exec -it anon-mysql mysql -uroot -proot -e "SHOW DATABASES;"`

**Frontend build fails with "Node.js version required"**:
```bash
# Use Node.js 20.20.0
export PATH="$HOME/.nvm/versions/node/v20.20.0/bin:$PATH"
npm run build
```

**Tests fail with "No such file or directory: gradlew"**:
```bash
# Use full path
/Users/parkhanbin/git/study/anon-community/gradlew test
```

**CDC events not flowing**:
1. Check Debezium connector status: `curl http://localhost:8083/connectors/mysql-connector/status`
2. Check Kafka topics: `docker exec -it anon-kafka kafka-topics --list --bootstrap-server localhost:9092`
3. Verify MySQL binlog enabled: `docker exec -it anon-mysql mysql -uroot -proot -e "SHOW VARIABLES LIKE 'log_bin';"`

## References

- **Design Docs**: `docs/plans/`
  - `2026-02-17-i18n-locale-propagation-design.md` — i18n 설계
  - `2026-02-19-fe-nextjs-implementation-plan.md` — Frontend 구현 계획
  - `2026-02-20-ux-improvements-p0-p1.md` — UX 개선 계획
- **API Documentation**: Swagger UI at `http://localhost:8080/swagger-ui/index.html` (when running)
- **Test Coverage Report**: Future (JaCoCo integration planned)

## Contact

- **Project Owner**: parkhanbin
- **Repository**: Private (study project)
- **Last Updated**: 2026-02-20
