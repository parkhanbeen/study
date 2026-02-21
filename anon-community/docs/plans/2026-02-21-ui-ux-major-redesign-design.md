# UI/UX 대규모 리디자인 설계서

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:writing-plans to turn this design into an implementation plan.

**Goal:** 3-컬럼 레이아웃 도입, 댓글 수정 기능 추가, 번역 자동 실행, 모바일 하단 탭 바, 시각적 polish을 통해 서비스 완성도를 높인다.

**Architecture:** Phased incremental approach — Phase 1 (백엔드 댓글 수정), Phase 2 (프론트엔드 기능 완성), Phase 3 (3-컬럼 레이아웃 리디자인), Phase 4 (시각적 polish + 빌드 검증)

**Tech Stack:** Kotlin/Spring Boot (backend), Next.js 16 App Router + Tailwind CSS 4 + shadcn/ui (frontend)

---

## 현재 상태 분석

### 이미 구현됨 (재구현 금지)
- 댓글 답글 UI (depth, parentCommentId, reply form in CommentList) ✅
- 댓글 삭제 ✅
- Hot posts 정렬 likeCount DESC ✅ (feedApi.getHot 이미 올바르게 구현됨)
- Post 수정/삭제 UI (PostActions 컴포넌트) ✅
- TranslateButton with Google Translate API ✅ (but explicit 번역 button still present)
- 다크 모드 ✅
- 검색 하이라이팅 ✅
- My Page ✅
- Settings 계정 정보 ✅

### 신규 구현 필요
1. **백엔드**: 댓글 수정 (`Comment.update()` 도메인 메서드 + UseCase + 엔드포인트)
2. **프론트엔드**: 댓글 수정 UI (inline edit)
3. **프론트엔드**: 번역 자동 실행 (언어 선택 시 즉시 번역)
4. **프론트엔드**: 3-컬럼 레이아웃 (데스크탑)
5. **프론트엔드**: 모바일 하단 탭 바
6. **프론트엔드**: 좋아요 버튼 애니메이션 + 시각적 polish
7. **프론트엔드**: 카테고리 이모지 배지

---

## 설계

### 1. 백엔드: 댓글 수정

**도메인 변경** — `Comment` 클래스에 `update()` 메서드 추가:
```kotlin
fun update(newContent: CommentContent) {
    check(status == CommentStatus.ACTIVE) { "Cannot update deleted comment" }
    this.content = newContent
    this.updatedAt = Instant.now()
}
```
주의: `content`가 `val`로 선언되어 있어 `var`로 변경 필요.

**포트 변경** — `CommentCommandPort`에 update 추가:
```kotlin
fun update(comment: Comment): Comment
```
또는 기존 `save()`를 upsert 방식으로 활용 (구현 방식은 JPA 엔티티 확인 후 결정).

**UseCase** — `UpdateCommentUseCase`:
```kotlin
data class UpdateCommentCommand(
    val commentId: CommentId,
    val requesterId: MemberId,
    val content: String,
)
```
- `CommentQueryPort.findById(commentId)` 조회
- `comment.authorId != requesterId` → throw `CommentNotFoundException` (보안상 작성자만 수정)
- `comment.update(CommentContent(content))` 호출
- `commentCommandPort.save(comment)` 저장

**컨트롤러** — `PUT /api/v1/posts/{postId}/comments/{commentId}`:
```kotlin
@PutMapping("/{commentId}")
fun updateComment(
    @PathVariable postId: Long,
    @PathVariable commentId: Long,
    @RequestHeader("X-Member-Id") memberId: Long,
    @Valid @RequestBody request: UpdateCommentRequest,
): ResponseEntity<CommentResponse>
```

**DTO** — `UpdateCommentRequest`:
```kotlin
data class UpdateCommentRequest(
    @field:NotBlank @field:Size(max = 1000) val content: String
)
```

### 2. 프론트엔드: 댓글 수정 UI

**CommentItem** 변경:
- 작성자인 경우 연필 아이콘(Pencil) 버튼 추가 (Trash2 옆)
- `isEditing` 로컬 state로 inline edit 모드 관리
- 수정 모드: textarea + 저장/취소 버튼
- `useMutation` → `PUT /posts/{postId}/comments/{commentId}`

```tsx
// 수정 모드 UI
{isEditing ? (
  <form onSubmit={handleSave} className="mt-2 space-y-2">
    <Textarea value={editContent} onChange={...} className="min-h-[60px] text-sm" />
    <div className="flex gap-2">
      <Button size="sm" type="submit" disabled={isPending}>저장</Button>
      <Button size="sm" variant="ghost" onClick={() => setIsEditing(false)}>취소</Button>
    </div>
  </form>
) : (
  <p className="mt-1 text-sm leading-relaxed">{comment.content}</p>
)}
```

**hooks** — `useUpdateComment(postId)` hook 추가:
```ts
const mutation = useMutation({
  mutationFn: ({ commentId, content }: { commentId: number; content: string }) =>
    fetchAPI(`/posts/${postId}/comments/${commentId}`, { method: "PUT", body: JSON.stringify({ content }) }),
  onSuccess: () => queryClient.invalidateQueries({ queryKey: ["comments", postId] }),
});
```

### 3. 프론트엔드: 번역 자동 실행

**TranslateButton** 변경:
- `onValueChange` 핸들러에서 즉시 번역 호출 (별도 "번역" 버튼 제거)
- 로딩 상태를 Select trigger에 표시
- "원문 보기" / "번역 숨기기" 토글 버튼만 남김

변경 전:
```tsx
<Select value={targetLang} onValueChange={setTargetLang}>...</Select>
<Button onClick={handleTranslate}>번역</Button>
```

변경 후:
```tsx
<Select value={targetLang} onValueChange={(lang) => { setTargetLang(lang); handleTranslate(lang); }}>
  <SelectTrigger className="w-36 h-8 text-xs">
    {isLoading ? <span>번역 중...</span> : <SelectValue placeholder="언어 선택..." />}
  </SelectTrigger>
</Select>
{showTranslation && (
  <Button variant="ghost" size="sm" onClick={() => setShowTranslation(false)}>원문 보기</Button>
)}
```

### 4. 프론트엔드: 3-컬럼 레이아웃

**레이아웃 구조** (데스크탑 ≥ 1024px):
```
┌─────────────────────────────────────────────────────────┐
│ Header (full width, sticky)                              │
├──────────────┬─────────────────────────┬────────────────┤
│ LeftSidebar  │ Main Content            │ RightSidebar   │
│ w-[220px]    │ flex-1 min-w-0          │ w-[260px]      │
│ sticky top   │ max-w-2xl               │ sticky top     │
│              │                         │                │
│ - 카테고리    │ - FeedList/SearchPage   │ - 🔥 핫 게시글  │
│   네비게이션   │ - PostDetail            │ - 🏷 인기 태그  │
│ - 글쓰기 버튼  │ - My Page 등            │ - 사이트 통계   │
│ - 사용자 카드  │                         │                │
└──────────────┴─────────────────────────┴────────────────┘
```

**모바일 (< 1024px)**:
- 1컬럼, 사이드바 숨김
- Bottom tab bar 표시

**`app/(main)/layout.tsx`** 변경:
```tsx
export default function MainLayout({ children }) {
  return (
    <>
      <Header />
      <div className="container mx-auto px-4 py-6">
        <div className="flex gap-6">
          {/* Left sidebar — hidden on mobile */}
          <aside className="hidden lg:block w-[220px] shrink-0">
            <LeftSidebar />
          </aside>
          {/* Main content */}
          <main className="flex-1 min-w-0 max-w-2xl">{children}</main>
          {/* Right sidebar — hidden on mobile/tablet */}
          <aside className="hidden xl:block w-[260px] shrink-0">
            <RightSidebar />
          </aside>
        </div>
      </div>
      {/* Mobile bottom tab */}
      <MobileBottomNav />
      {/* FAB — hidden on mobile (covered by bottom tab) */}
      <div className="hidden lg:block">
        <WriteFab />
      </div>
    </>
  );
}
```

### 5. LeftSidebar 컴포넌트

**파일**: `frontend/src/components/layout/left-sidebar.tsx`

내용:
- **글쓰기 버튼** (로그인 시): full-width Button → `/posts/new`
- **카테고리 네비게이션**: 전체 + 각 카테고리 (이모지 포함)
  - 클릭 시 `/?category=DAILY` 등으로 이동
  - 현재 선택된 카테고리 하이라이트 (`useSearchParams` + `usePathname`)
- **사용자 카드** (로그인 시): 닉네임 + 게시글 수 (작은 카드)

카테고리 이모지 맵:
```ts
const CATEGORY_EMOJI: Record<string, string> = {
  DAILY:       "☀️",
  ADVICE:      "💬",
  HUMOR:       "😂",
  INFORMATION: "📚",
  QUESTION:    "❓",
  DISCUSSION:  "🗣️",
  MEDIA:       "🎬",
  FEEDBACK:    "📝",
};
```

### 6. RightSidebar 컴포넌트

**파일**: `frontend/src/components/layout/right-sidebar.tsx`

내용:
- `HotPostsSection` 이동 (기존 컴포넌트 재사용)
- **인기 태그 섹션**: `useHotTags(10)` 사용
  - 클릭 시 `/search?tags={tag}` 이동
- **통계 카드** (선택적): 전체 게시글 수, 활성 사용자 등

주의: `HotPostsSection`이 현재 메인 피드 페이지(`app/(main)/page.tsx`)에 있음 → RightSidebar로 이동.

### 7. 모바일 하단 탭 바

**파일**: `frontend/src/components/layout/mobile-bottom-nav.tsx`

```tsx
const NAV_ITEMS = [
  { href: "/",       icon: Home,   label: "홈" },
  { href: "/search", icon: Search, label: "검색" },
  { href: "/posts/new", icon: Plus, label: "글쓰기", requireAuth: true },
  { href: "/my",     icon: User,   label: "마이", requireAuth: true },
];
```

- `lg:hidden` → 모바일/태블릿에서만 표시
- `fixed bottom-0 left-0 right-0 z-50 bg-background border-t`
- 미로그인 시 글쓰기/마이 클릭 → `/login?callbackUrl=...` 리다이렉트
- 현재 경로 active 표시 (`usePathname`)

### 8. 좋아요 버튼 애니메이션

**LikeButton** 변경:
- 좋아요 클릭 시 Heart 아이콘 scale animation (CSS keyframes 또는 tailwind)
- `fill-heart` 클래스: `animate-bounce` 한 번 또는 custom scale animation

```tsx
// tailwind 커스텀 keyframe (globals.css에 추가):
// @keyframes heartbeat { 0%, 100% { transform: scale(1); } 50% { transform: scale(1.4); } }
// .animate-heartbeat { animation: heartbeat 0.3s ease-in-out; }

<Heart
  className={cn(
    "h-5 w-5 transition-transform",
    isLiked && "fill-current text-red-500",
    animating && "animate-heartbeat"
  )}
/>
```

- `animating` state: 클릭 시 true, 300ms 후 false로 리셋

### 9. 카테고리 이모지 + Empty State

**카테고리 이모지**: 이미 LeftSidebar에서 정의. `POST_CATEGORIES` 타입에 emoji 필드 추가하거나 별도 map 사용.

**Empty State 개선**:
- 피드 empty: 일러스트 (단순 SVG 또는 lucide 아이콘 조합) + "첫 번째 게시글을 작성해보세요" + 글쓰기 버튼
- 검색 empty: 이미 텍스트 있음 → 아이콘 추가
- 댓글 empty: 이미 텍스트 있음 → MessageCircle 아이콘 크게 표시

---

## 마이그레이션 주의사항

1. **HotPostsSection 이동**: 현재 `app/(main)/page.tsx`에 있음 → RightSidebar로 이동 후 page.tsx에서 제거
2. **WriteFab**: 모바일에서는 bottom tab의 "글쓰기"로 대체 → `lg:block` 조건부 표시
3. **Comment 도메인**: `content`를 `val → var`로 변경 시 불변성 원칙 위배 우려 → `update()` 메서드 내부에서만 변경 가능하게 캡슐화
4. **CommentCommandPort**: 기존 `save()`가 JPA `merge` 기반이면 update에 재사용 가능. 아닌 경우 별도 `update()` 추가

---

## 테스트 전략

- **백엔드**: `UpdateCommentUseCaseSpec.kt` (TDD — Kotest DescribeSpec)
  - 정상 수정 케이스
  - 타인 댓글 수정 시 예외
  - 삭제된 댓글 수정 시 예외
- **프론트엔드**: 빌드 성공 확인 (`npm run build`)
- **수동 검증**: 로컬 실행 후 댓글 수정, 번역 자동 실행, 3-컬럼 레이아웃 확인

---

## 구현 순서

```
Task 1 (Backend): Comment.update() + UpdateCommentUseCase + PUT endpoint
Task 2 (Frontend): Comment edit UI (CommentItem + useUpdateComment hook)
Task 3 (Frontend): Translation auto-execute (TranslateButton)
Task 4 (Frontend): LeftSidebar component
Task 5 (Frontend): RightSidebar component (with HotPostsSection migration)
Task 6 (Frontend): 3-column layout restructure (layout.tsx)
Task 7 (Frontend): MobileBottomNav component
Task 8 (Frontend): Like button heartbeat animation
Task 9 (Frontend): Category emoji + empty state improvements
Task 10 (Full): Build verification (backend + frontend)
```
