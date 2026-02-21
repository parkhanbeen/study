# anon-community Frontend Design

## Goal

React/Next.js 기반 익명 커뮤니티 프론트엔드. 스터디/포트폴리오 목적으로 모든 백엔드 API를 활용하는 풀스택 데모 구현.

## Architecture

Next.js 15 App Router + React Server Components 기반. 피드/게시글 상세/검색은 SSR로 SEO와 초기 로딩을 최적화하고, 글쓰기/댓글/좋아요 등 인터랙션은 Client Component + TanStack Query로 처리.

## Tech Stack

| Category | Package | Version |
|---|---|---|
| Framework | next | 15.x |
| UI | tailwindcss + shadcn/ui | 4.x + latest |
| Icons | lucide-react | latest |
| Server state | @tanstack/react-query | 5.x |
| Client state | zustand | 5.x |
| Form | react-hook-form + zod | 7.x + 3.x |
| Date | date-fns | 4.x |
| Toast | sonner | latest |
| Test | vitest + testing-library + msw | latest |

---

## Project Structure

```
frontend/
├── src/
│   ├── app/
│   │   ├── (auth)/
│   │   │   ├── login/page.tsx
│   │   │   └── signup/page.tsx
│   │   ├── (main)/
│   │   │   ├── layout.tsx
│   │   │   ├── page.tsx                  # Feed (SSR)
│   │   │   ├── posts/
│   │   │   │   ├── [postId]/page.tsx     # Post detail (SSR)
│   │   │   │   ├── [postId]/edit/page.tsx
│   │   │   │   └── new/page.tsx
│   │   │   ├── search/page.tsx           # Search (SSR + Client)
│   │   │   └── settings/page.tsx
│   │   ├── layout.tsx
│   │   ├── error.tsx
│   │   ├── not-found.tsx
│   │   └── globals.css
│   ├── components/
│   │   ├── ui/              # shadcn/ui
│   │   ├── post/            # PostCard, PostDetail, PostForm
│   │   ├── comment/         # CommentList, CommentItem, CommentForm
│   │   ├── auth/            # LoginForm, SignupForm
│   │   └── layout/          # Header, Navigation
│   ├── lib/
│   │   ├── api/             # API client + endpoint modules
│   │   │   ├── client.ts
│   │   │   ├── auth.ts
│   │   │   ├── posts.ts
│   │   │   ├── comments.ts
│   │   │   ├── likes.ts
│   │   │   ├── feed.ts
│   │   │   └── search.ts
│   │   ├── auth/
│   │   │   └── token.ts
│   │   └── hooks/           # TanStack Query hooks
│   │       ├── use-auth.ts
│   │       ├── use-posts.ts
│   │       ├── use-comments.ts
│   │       └── use-search.ts
│   ├── stores/
│   │   └── auth-store.ts
│   └── types/
│       └── api.ts
├── middleware.ts
├── next.config.ts
├── tailwind.config.ts
└── package.json
```

### Route Groups

- `(auth)`: Login/Signup pages. No header/navigation.
- `(main)`: All authenticated and public content pages. Shared header + navigation layout.

---

## Authentication Architecture

### Token Storage: httpOnly Cookies

Tokens are stored in httpOnly cookies, not accessible from JavaScript. This prevents XSS attacks from stealing tokens.

```
Browser → Next.js API Route Handler → Backend API
          (extracts token from cookie,
           adds Authorization header)
```

### Flow

1. Login/Signup → Backend returns `{ accessToken, refreshToken, memberId }`
2. Next.js API route sets httpOnly cookies for both tokens
3. Client Components call Next.js API route handlers (proxy) for authenticated requests
4. Server Components read cookies directly for SSR fetch
5. Next.js Middleware handles route protection + token refresh

### Auth State (Client)

Zustand `authStore` holds only non-sensitive info for UI rendering:
- `isLoggedIn: boolean`
- `memberId: number | null`
- `nickname: string | null`

Tokens themselves are never exposed to client JavaScript.

### Token Refresh

Next.js middleware intercepts all requests. If the access token is expiring soon (< 5 min), it triggers a refresh before forwarding the request.

---

## Data Fetching Patterns

| Page/Feature | Rendering | Method |
|---|---|---|
| Feed (initial) | SSR | Server Component fetch |
| Feed (infinite scroll) | CSR | `useInfiniteQuery` + cursor API |
| Post detail | SSR | Server Component fetch |
| Comments | CSR | `useInfiniteQuery` (pagination) |
| Like toggle | CSR | `useMutation` + optimistic update |
| Post create/edit | CSR | `useMutation` + react-hook-form |
| Search (initial) | SSR | URL searchParams → server fetch |
| Search (typing) | CSR | debounced `useQuery` |

---

## Pages (7 total)

### 1. Feed — `/` (SSR + CSR)
- SSR: Initial 20 posts
- CSR: Infinite scroll via IntersectionObserver + `useInfiniteQuery`
- Components: `PostCard`, `PostCardSkeleton`, `FeedList`

### 2. Post Detail — `/posts/[postId]` (SSR + CSR)
- SSR: Post content + metadata
- CSR: Like button (optimistic), comments (useInfiniteQuery), comment form
- Components: `PostDetail`, `LikeButton`, `CommentList`, `CommentItem`, `CommentForm`

### 3. Post Create — `/posts/new` (CSR)
- Auth required (middleware redirect)
- react-hook-form + zod validation
- Component: `PostForm` (shared for create/edit)

### 4. Post Edit — `/posts/[postId]/edit` (CSR)
- Auth required, author-only (check on load)
- Reuses `PostForm` with `mode="edit"`

### 5. Search — `/search` (SSR + CSR)
- SSR: Initial search from URL params
- CSR: Debounced keyword input, tag filters, cursor-based infinite scroll
- Components: `SearchInput`, `TagFilter`, `PostCard`

### 6. Login — `/login` (CSR)
- Component: `LoginForm`
- On success: set cookies + redirect to callbackUrl or `/`

### 7. Signup — `/signup` (CSR)
- Component: `SignupForm`
- On success: auto-login (set cookies) + redirect to `/`

### 8. Settings — `/settings` (CSR)
- Language preference selector (ko/en/ja/zh)
- Logout button
- Auth required

---

## Error Handling (3 layers)

### Layer 1: API Client
- Custom `ApiError` class with `status`, `code`, `message`
- 401 → automatic token refresh → retry → fail → logout + redirect
- All other errors thrown as `ApiError`

### Layer 2: TanStack Query (per mutation/query)

| Backend Code | FE Action |
|---|---|
| `NICKNAME_DUPLICATED` | Inline form field error |
| `ALREADY_LIKED` | Rollback optimistic update + toast |
| `POST_NOT_FOUND` | Next.js `notFound()` |
| `FORBIDDEN` | Toast + navigate back |
| `VALIDATION_ERROR` | Map to form field errors |
| `INTERNAL_ERROR` | Toast "try again later" |

### Layer 3: Next.js Error Boundaries
- `app/error.tsx` — Global fallback
- `app/(main)/posts/[postId]/error.tsx` — Post-specific errors
- `app/not-found.tsx` — Global 404

### Edge Cases
- Draft preservation in localStorage during token refresh
- Multi-tab logout via BroadcastChannel API
- Offline detection via TanStack Query `networkMode`

---

## Testing Strategy

| Layer | Target | Tool | Priority |
|---|---|---|---|
| API Client | fetchAPI, token refresh, error mapping | vitest + msw | High |
| Hooks | useAuth, usePosts, useComments | vitest + msw | High |
| Components | PostForm validation, LikeButton optimistic | testing-library | Medium |
| E2E | Login → write → comment flow | playwright | Low (later) |

---

## Dev Environment

| Item | Value |
|---|---|
| Node.js | 20 LTS |
| Package manager | pnpm |
| Project location | `anon-community/frontend/` |
| Dev port | 3000 |
| API proxy | `next.config.ts` rewrites `/api/v1/**` → `localhost:9000` |
| Lint | ESLint (next/core-web-vitals) |
| Format | Prettier |
