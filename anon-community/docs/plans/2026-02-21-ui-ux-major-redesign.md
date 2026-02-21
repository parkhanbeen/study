# UI/UX 대규모 리디자인 구현 플랜

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task.

**Goal:** 댓글 수정 기능 추가(백엔드+프론트엔드), 번역 자동 실행, 3-컬럼 레이아웃, 모바일 하단 탭 바, 카테고리 이모지를 구현한다.

**Architecture:** Phase 1 Backend (댓글 수정 UseCase), Phase 2 Frontend Feature (댓글 수정 UI, 번역 자동화), Phase 3 Frontend Layout (사이드바, 3-컬럼, 모바일 탭), Phase 4 Polish (이모지, empty state), Phase 5 Build 검증

**Tech Stack:** Kotlin/Spring Boot 3.4, Kotest 5.9.1 (DescribeSpec), MockK, Next.js 16 App Router, TypeScript, Tailwind CSS 4, shadcn/ui, React Query v5

---

## 현재 상태 주의사항 (이미 구현됨 — 재구현 금지)

- 댓글 답글 UI (depth, parentCommentId, reply form) ✅
- 댓글 삭제 ✅
- Hot posts 정렬 likeCount DESC ✅
- Post 수정/삭제 UI (PostActions) ✅
- TranslateButton (Google API 연동) ✅
- 다크 모드 ✅
- LikeButton 애니메이션 (scale-125) ✅

---

## Task 1: 댓글 수정 — 백엔드 (TDD)

**Files:**
- Modify: `domain/comment/src/main/kotlin/com/anoncommunity/domain/comment/Comment.kt`
- Create: `app/usecase/src/main/kotlin/com/anoncommunity/app/usecase/comment/UpdateCommentUseCase.kt`
- Create: `app/usecase/src/test/kotlin/com/anoncommunity/app/usecase/comment/UpdateCommentUseCaseSpec.kt`
- Modify: `app/api/src/main/kotlin/com/anoncommunity/app/api/dto/Requests.kt`
- Modify: `app/api/src/main/kotlin/com/anoncommunity/app/api/controller/CommentController.kt`

**배경:**
- `Comment.content`가 현재 `val`로 선언됨 → `var private set`으로 변경 필요
- `CommentCommandPort.save()`가 이미 JPA merge를 사용하므로 인프라 변경 불필요
- `CommentQueryPort.findById(id: CommentId): Comment?` 이미 존재

### Step 1: 테스트 작성 (실패 확인)

`app/usecase/src/test/kotlin/com/anoncommunity/app/usecase/comment/UpdateCommentUseCaseSpec.kt` 파일을 생성:

```kotlin
package com.anoncommunity.app.usecase.comment

import com.anoncommunity.contract.model.CommentId
import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.domain.comment.Comment
import com.anoncommunity.domain.comment.CommentContent
import com.anoncommunity.domain.comment.exception.CommentNotFoundException
import com.anoncommunity.domain.comment.port.CommentCommandPort
import com.anoncommunity.domain.comment.port.CommentQueryPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class UpdateCommentUseCaseSpec : DescribeSpec({

    val commentQueryPort = mockk<CommentQueryPort>()
    val commentCommandPort = mockk<CommentCommandPort>()
    val useCase = UpdateCommentUseCase(commentQueryPort, commentCommandPort)

    val activeComment = Comment.create(
        id = CommentId(1L),
        postId = PostId(10L),
        authorId = MemberId(100L),
        content = CommentContent("Original content"),
    )

    beforeEach {
        clearMocks(commentQueryPort, commentCommandPort)
    }

    describe("UpdateCommentUseCase") {

        context("author updates own comment") {
            it("should update content and return updated comment") {
                every { commentQueryPort.findById(CommentId(1L)) } returns activeComment
                every { commentCommandPort.save(any()) } answers { firstArg() }

                val command = UpdateCommentCommand(commentId = 1L, requesterId = 100L, content = "Updated content")
                val result = useCase(command)

                result.content shouldBe CommentContent("Updated content")
                verify(exactly = 1) { commentCommandPort.save(any()) }
            }
        }

        context("non-author tries to update") {
            it("should throw CommentNotFoundException") {
                every { commentQueryPort.findById(CommentId(1L)) } returns activeComment

                val command = UpdateCommentCommand(commentId = 1L, requesterId = 999L, content = "Hack")
                shouldThrow<CommentNotFoundException> {
                    useCase(command)
                }
                verify(exactly = 0) { commentCommandPort.save(any()) }
            }
        }

        context("comment not found") {
            it("should throw CommentNotFoundException") {
                every { commentQueryPort.findById(CommentId(99L)) } returns null

                val command = UpdateCommentCommand(commentId = 99L, requesterId = 100L, content = "Update")
                shouldThrow<CommentNotFoundException> {
                    useCase(command)
                }
            }
        }

        context("updating deleted comment") {
            // CommentNotFoundException: DeleteCommentUseCase와 동일한 예외 사용 (일관성 + 404 응답)
            // IllegalStateException은 HTTP 409로 매핑되어 의미상 부적절
            it("should throw CommentNotFoundException") {
                val deletedComment = Comment.create(
                    id = CommentId(1L),
                    postId = PostId(10L),
                    authorId = MemberId(100L),
                    content = CommentContent("Original"),
                ).apply { softDelete() }

                every { commentQueryPort.findById(CommentId(1L)) } returns deletedComment

                val command = UpdateCommentCommand(commentId = 1L, requesterId = 100L, content = "Update")
                shouldThrow<CommentNotFoundException> {
                    useCase(command)
                }
                verify(exactly = 0) { commentCommandPort.save(any()) }
            }
        }
    }
})
```

### Step 2: 테스트 실행 (실패 확인)

```bash
/Users/parkhanbin/git/study/anon-community/gradlew :app:usecase:test --tests "com.anoncommunity.app.usecase.comment.UpdateCommentUseCaseSpec" 2>&1 | tail -20
```

Expected: 컴파일 에러 (UpdateCommentUseCase not found)

### Step 3: Comment 도메인에 update() 메서드 추가

`domain/comment/src/main/kotlin/com/anoncommunity/domain/comment/Comment.kt` 수정:

`val content: CommentContent`를 `var content: CommentContent` `private set`으로 변경하고 `update()` 메서드 추가:

```kotlin
class Comment(
    val id: CommentId,
    val postId: PostId,
    val authorId: MemberId,
    content: CommentContent,           // ← constructor parameter
    val parentCommentId: CommentId?,
    val depth: Int,
    status: CommentStatus,
    val createdAt: Instant,
    updatedAt: Instant,
) : AggregateRoot() {

    var content: CommentContent = content  // ← var private set (add this)
        private set

    var status: CommentStatus = status
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun update(newContent: CommentContent) {
        check(status == CommentStatus.ACTIVE) { "Cannot update a deleted comment" }
        this.content = newContent
        this.updatedAt = Instant.now()
    }

    fun softDelete() { ... }  // 기존 유지

    companion object { ... }  // 기존 유지
}
```

**주의**: 기존 `val content: CommentContent`를 constructor parameter에서 제거하고 class body에 `var content: CommentContent = content`를 추가. primary constructor parameter는 `content: CommentContent` (val 없이).

### Step 4: UpdateCommentUseCase 구현

`app/usecase/src/main/kotlin/com/anoncommunity/app/usecase/comment/UpdateCommentUseCase.kt` 생성:

```kotlin
package com.anoncommunity.app.usecase.comment

import com.anoncommunity.contract.model.CommentId
import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.usecase.CommandUseCase
import com.anoncommunity.domain.comment.Comment
import com.anoncommunity.domain.comment.CommentContent
import com.anoncommunity.domain.comment.CommentStatus
import com.anoncommunity.domain.comment.exception.CommentNotFoundException
import com.anoncommunity.domain.comment.port.CommentCommandPort
import com.anoncommunity.domain.comment.port.CommentQueryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class UpdateCommentCommand(
    val commentId: Long,
    val requesterId: Long,
    val content: String,
)

@Service
class UpdateCommentUseCase(
    private val commentQueryPort: CommentQueryPort,
    private val commentCommandPort: CommentCommandPort,
) : CommandUseCase<UpdateCommentCommand, Comment> {

    @Transactional
    override fun invoke(command: UpdateCommentCommand): Comment {
        val commentId = CommentId(command.commentId)
        val comment = commentQueryPort.findById(commentId)
            ?: throw CommentNotFoundException(commentId)

        // 타인 댓글 수정 방지: authorId 노출 방지를 위해 CommentNotFoundException 사용
        if (comment.authorId != MemberId(command.requesterId)) {
            throw CommentNotFoundException(commentId)
        }

        // 삭제된 댓글 수정 방지: DeleteCommentUseCase와 동일한 예외로 일관성 유지
        if (comment.status != CommentStatus.ACTIVE) {
            throw CommentNotFoundException(commentId)
        }

        comment.update(CommentContent(command.content))
        return commentCommandPort.save(comment)
    }
}
```

### Step 5: 테스트 실행 (통과 확인)

```bash
/Users/parkhanbin/git/study/anon-community/gradlew :app:usecase:test --tests "com.anoncommunity.app.usecase.comment.UpdateCommentUseCaseSpec"
```

Expected: BUILD SUCCESSFUL, 4 tests passed

### Step 6: DTO 추가

`app/api/src/main/kotlin/com/anoncommunity/app/api/dto/Requests.kt`에 추가:

```kotlin
data class UpdateCommentRequest(
    @field:NotBlank(message = "댓글 내용은 비어있을 수 없습니다")
    @field:Size(max = 1000, message = "댓글은 최대 1000자까지 입력 가능합니다")
    val content: String,
)
```

### Step 7: 컨트롤러 엔드포인트 추가

`app/api/src/main/kotlin/com/anoncommunity/app/api/controller/CommentController.kt`:

```kotlin
// 기존 CommentController에 추가
// 1) 생성자에 updateCommentUseCase 추가:
class CommentController(
    private val createCommentUseCase: CreateCommentUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val updateCommentUseCase: UpdateCommentUseCase,  // ← 추가
)

// 2) 메서드 추가:
@PutMapping("/{commentId}")
fun updateComment(
    @PathVariable postId: Long,
    @PathVariable commentId: Long,
    @RequestHeader("X-Member-Id") memberId: Long,
    @Valid @RequestBody request: UpdateCommentRequest,
): ResponseEntity<CommentResponse> {
    val comment = updateCommentUseCase(
        UpdateCommentCommand(
            commentId = commentId,
            requesterId = memberId,
            content = request.content,
        )
    )
    return ResponseEntity.ok(CommentResponse.from(comment))
}
```

### Step 8: 전체 usecase 모듈 테스트

```bash
/Users/parkhanbin/git/study/anon-community/gradlew :app:usecase:test
```

Expected: BUILD SUCCESSFUL

### Step 9: 커밋

```bash
git add domain/comment/src/main/kotlin/com/anoncommunity/domain/comment/Comment.kt
git add app/usecase/src/main/kotlin/com/anoncommunity/app/usecase/comment/UpdateCommentUseCase.kt
git add app/usecase/src/test/kotlin/com/anoncommunity/app/usecase/comment/UpdateCommentUseCaseSpec.kt
git add app/api/src/main/kotlin/com/anoncommunity/app/api/dto/Requests.kt
git add app/api/src/main/kotlin/com/anoncommunity/app/api/controller/CommentController.kt
git commit -m "feat(be): add comment update endpoint with TDD"
```

---

## Task 2: 댓글 수정 — 프론트엔드 UI

**Files:**
- Modify: `frontend/src/lib/api/comments.ts`
- Modify: `frontend/src/lib/hooks/use-comments.ts`
- Modify: `frontend/src/components/comment/comment-item.tsx`

**배경:**
- 기존에 `commentsApi.delete`, `useDeleteComment` 패턴과 동일하게 update 추가
- CommentItem에 Pencil 아이콘 버튼 + inline edit 폼 추가

### Step 1: commentsApi에 update 추가

`frontend/src/lib/api/comments.ts`:

```ts
import { fetchAPI } from "./client";
import { CommentResponse, CreateCommentRequest, PageResponse } from "@/types/api";

export const commentsApi = {
  list: (postId: number, page = 0, size = 20) =>
    fetchAPI<PageResponse<CommentResponse>>(`/posts/${postId}/comments?page=${page}&size=${size}`),

  create: (postId: number, data: CreateCommentRequest) =>
    fetchAPI<CommentResponse>(`/posts/${postId}/comments`, { method: "POST", body: JSON.stringify(data) }),

  delete: (postId: number, commentId: number) =>
    fetchAPI<void>(`/posts/${postId}/comments/${commentId}`, { method: "DELETE" }),

  update: (postId: number, commentId: number, content: string) =>
    fetchAPI<CommentResponse>(`/posts/${postId}/comments/${commentId}`, {
      method: "PUT",
      body: JSON.stringify({ content }),
    }),
};
```

### Step 2: useUpdateComment 훅 추가

`frontend/src/lib/hooks/use-comments.ts`에 추가 (기존 useDeleteComment 다음):

```ts
export function useUpdateComment(postId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ commentId, content }: { commentId: number; content: string }) =>
      commentsApi.update(postId, commentId, content),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["comments", postId] });
      queryClient.invalidateQueries({ queryKey: ["post", postId] });
      toast.success("댓글이 수정되었습니다");
    },
    onError: () => {
      toast.error("댓글 수정에 실패했습니다");
    },
  });
}
```

### Step 3: CommentItem에 수정 UI 추가

`frontend/src/components/comment/comment-item.tsx` 전체 교체:

```tsx
"use client";

import { useState } from "react";
import { Reply, Trash2, Pencil } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { CommentResponse } from "@/types/api";
import { timeAgo } from "@/lib/utils";
import { useAuthStore } from "@/stores/auth-store";
import { useDeleteComment, useUpdateComment } from "@/lib/hooks/use-comments";

interface CommentItemProps {
  comment: CommentResponse;
  postId: number;
  onReply?: (commentId: number) => void;
}

export function CommentItem({ comment, postId, onReply }: CommentItemProps) {
  const { memberId } = useAuthStore();
  const [isEditing, setIsEditing] = useState(false);
  const [editContent, setEditContent] = useState(comment.content);
  const deleteMutation = useDeleteComment(postId);
  const updateMutation = useUpdateComment(postId);
  const isAuthor = memberId === comment.authorId;

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    if (!editContent.trim()) return;
    updateMutation.mutate(
      { commentId: comment.id, content: editContent.trim() },
      { onSuccess: () => setIsEditing(false) },
    );
  };

  return (
    <div className={comment.depth === 1 ? "ml-8 border-l-2 border-muted pl-4" : ""}>
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2 text-xs text-muted-foreground">
          <span className="inline-flex h-5 w-5 items-center justify-center rounded-full bg-primary/10 text-[10px] font-bold text-primary">
            {String(comment.authorId).slice(-1)}
          </span>
          <span className="font-medium text-foreground">익명 #{comment.authorId}</span>
          <span>&middot;</span>
          <span>{timeAgo(comment.createdAt)}</span>
        </div>
        <div className="flex items-center gap-0.5">
          {comment.depth === 0 && onReply && !isEditing && (
            <Button
              variant="ghost"
              size="sm"
              className="h-7 gap-1 px-2 text-xs text-muted-foreground hover:text-foreground"
              onClick={() => onReply(comment.id)}
            >
              <Reply className="h-3 w-3" /> 답글
            </Button>
          )}
          {isAuthor && !isEditing && (
            <>
              <Button
                variant="ghost"
                size="icon"
                className="h-7 w-7 text-muted-foreground hover:text-foreground"
                onClick={() => { setEditContent(comment.content); setIsEditing(true); }}
              >
                <Pencil className="h-3.5 w-3.5" />
              </Button>
              <AlertDialog>
                <AlertDialogTrigger asChild>
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-7 w-7 text-muted-foreground hover:text-destructive"
                    disabled={deleteMutation.isPending}
                  >
                    <Trash2 className="h-3.5 w-3.5" />
                  </Button>
                </AlertDialogTrigger>
                <AlertDialogContent>
                  <AlertDialogHeader>
                    <AlertDialogTitle>댓글 삭제</AlertDialogTitle>
                    <AlertDialogDescription>
                      이 댓글을 삭제하시겠습니까? 삭제된 댓글은 복구할 수 없습니다.
                    </AlertDialogDescription>
                  </AlertDialogHeader>
                  <AlertDialogFooter>
                    <AlertDialogCancel>취소</AlertDialogCancel>
                    <AlertDialogAction
                      onClick={() => deleteMutation.mutate(comment.id)}
                      className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                    >
                      삭제
                    </AlertDialogAction>
                  </AlertDialogFooter>
                </AlertDialogContent>
              </AlertDialog>
            </>
          )}
        </div>
      </div>

      {isEditing ? (
        <form onSubmit={handleSave} className="mt-2 space-y-2">
          <Textarea
            value={editContent}
            onChange={(e) => setEditContent(e.target.value)}
            className="min-h-[60px] text-sm resize-none"
            maxLength={1000}
          />
          <div className="flex gap-2">
            <Button size="sm" type="submit" disabled={updateMutation.isPending || !editContent.trim()}>
              {updateMutation.isPending ? "저장 중..." : "저장"}
            </Button>
            <Button size="sm" variant="ghost" type="button" onClick={() => setIsEditing(false)}>
              취소
            </Button>
          </div>
        </form>
      ) : (
        <p className="mt-1 text-sm leading-relaxed">{comment.content}</p>
      )}
    </div>
  );
}
```

### Step 4: 프론트엔드 빌드 확인

```bash
cd /Users/parkhanbin/git/study/anon-community/frontend && npm run build 2>&1 | tail -20
```

Expected: ✓ Compiled successfully

### Step 5: 커밋

```bash
git add frontend/src/lib/api/comments.ts
git add frontend/src/lib/hooks/use-comments.ts
git add "frontend/src/components/comment/comment-item.tsx"
git commit -m "feat(fe): add comment edit UI with inline editing"
```

---

## Task 3: 번역 자동 실행

**Files:**
- Modify: `frontend/src/components/post/translate-button.tsx`

**배경:**
- 현재: 언어 선택 후 별도 "번역" 버튼 클릭 필요
- 변경: 언어 선택 즉시 번역 실행
- Google Translate API는 이미 구현되어 있음

### Step 1: TranslateButton 수정

`frontend/src/components/post/translate-button.tsx` 전체 교체:

```tsx
"use client";

import { useState } from "react";
import { Languages } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";

const TRANSLATE_LANGUAGES = [
  { value: "ko", label: "한국어" },
  { value: "en", label: "English" },
  { value: "ja", label: "日本語" },
  { value: "zh-CN", label: "中文(简体)" },
  { value: "zh-TW", label: "中文(繁體)" },
];

interface TranslateButtonProps {
  title: string;
  content: string;
}

export function TranslateButton({ title, content }: TranslateButtonProps) {
  const [targetLang, setTargetLang] = useState<string | undefined>(undefined);
  const [translatedTitle, setTranslatedTitle] = useState<string | null>(null);
  const [translatedContent, setTranslatedContent] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [showTranslation, setShowTranslation] = useState(false);

  const doTranslate = async (lang: string) => {
    setIsLoading(true);
    setShowTranslation(false);
    try {
      const text = `${title}\n---\n${content}`;
      const url = `https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=${lang}&dt=t&q=${encodeURIComponent(text)}`;
      const res = await fetch(url);
      const data = await res.json();
      const fullTranslation = data[0]?.map((item: [string]) => item[0]).join("") ?? "";
      const parts = fullTranslation.split("\n---\n");
      setTranslatedTitle(parts[0] ?? title);
      setTranslatedContent(parts.slice(1).join("\n---\n") || fullTranslation);
      setShowTranslation(true);
    } catch {
      const fallbackUrl = `https://translate.google.com/?sl=auto&tl=${lang}&text=${encodeURIComponent(title + "\n\n" + content)}`;
      window.open(fallbackUrl, "_blank");
    } finally {
      setIsLoading(false);
    }
  };

  const handleLangChange = (lang: string) => {
    setTargetLang(lang);
    doTranslate(lang);
  };

  return (
    <div className="space-y-3">
      <div className="flex items-center gap-2">
        <Languages className="h-4 w-4 text-muted-foreground" />
        <Select value={targetLang} onValueChange={handleLangChange} disabled={isLoading}>
          <SelectTrigger className="w-40 h-8 text-xs">
            <SelectValue placeholder={isLoading ? "번역 중..." : "언어 선택 시 자동 번역"} />
          </SelectTrigger>
          <SelectContent>
            {TRANSLATE_LANGUAGES.map((lang) => (
              <SelectItem key={lang.value} value={lang.value}>
                {lang.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        {showTranslation && (
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setShowTranslation(false)}
            className="h-8 text-xs text-muted-foreground"
          >
            원문 보기
          </Button>
        )}
      </div>

      {showTranslation && translatedTitle && (
        <div className="rounded-lg border bg-muted/30 p-4 space-y-2">
          <p className="text-xs font-medium text-muted-foreground">번역 결과</p>
          <h2 className="text-lg font-semibold">{translatedTitle}</h2>
          <div className="whitespace-pre-wrap text-sm leading-relaxed">{translatedContent}</div>
        </div>
      )}
    </div>
  );
}
```

### Step 2: 빌드 확인

```bash
cd /Users/parkhanbin/git/study/anon-community/frontend && npm run build 2>&1 | tail -20
```

Expected: ✓ Compiled successfully

### Step 3: 커밋

```bash
git add "frontend/src/components/post/translate-button.tsx"
git commit -m "feat(fe): auto-translate on language selection"
```

---

## Task 4: LeftSidebar 컴포넌트

**Files:**
- Create: `frontend/src/components/layout/left-sidebar.tsx`

**배경:**
- 데스크탑 전용 (lg:block, 모바일에서 hidden)
- 카테고리 네비게이션 + 글쓰기 버튼 + 사용자 카드
- `usePathname` + `useSearchParams`로 현재 카테고리 감지

### Step 1: LeftSidebar 컴포넌트 생성

`frontend/src/components/layout/left-sidebar.tsx`:

```tsx
"use client";

import Link from "next/link";
import { usePathname, useSearchParams, useRouter } from "next/navigation";
import { PenSquare } from "lucide-react";
import { Button } from "@/components/ui/button";
import { POST_CATEGORIES, PostCategory } from "@/types/api";
import { useAuthStore } from "@/stores/auth-store";
import { cn } from "@/lib/utils";

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

export function LeftSidebar() {
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const router = useRouter();
  const { isLoggedIn, isInitialized } = useAuthStore();

  const currentCategory = pathname === "/" ? searchParams.get("category") : null;

  const handleCategoryClick = (value: PostCategory | null) => {
    if (value) {
      router.push(`/?category=${value}`);
    } else {
      router.push("/");
    }
  };

  return (
    <nav className="sticky top-20 space-y-1">
      {/* Write button */}
      {isInitialized && isLoggedIn && (
        <Button asChild className="w-full mb-3" size="sm">
          <Link href="/posts/new">
            <PenSquare className="mr-2 h-4 w-4" />
            글쓰기
          </Link>
        </Button>
      )}

      {/* All posts */}
      <button
        onClick={() => handleCategoryClick(null)}
        className={cn(
          "w-full flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium transition-colors text-left",
          !currentCategory
            ? "bg-primary/10 text-primary"
            : "text-muted-foreground hover:bg-muted hover:text-foreground",
        )}
      >
        🏠 전체
      </button>

      {/* Categories */}
      {POST_CATEGORIES.map((cat) => (
        <button
          key={cat.value}
          onClick={() => handleCategoryClick(cat.value)}
          className={cn(
            "w-full flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium transition-colors text-left",
            currentCategory === cat.value
              ? "bg-primary/10 text-primary"
              : "text-muted-foreground hover:bg-muted hover:text-foreground",
          )}
        >
          {CATEGORY_EMOJI[cat.value]} {cat.label}
        </button>
      ))}

      {/* Nav links */}
      <div className="pt-3 border-t mt-3 space-y-1">
        {[
          { href: "/search", label: "🔍 검색" },
          { href: "/my",     label: "👤 마이페이지" },
          { href: "/settings", label: "⚙️ 설정" },
        ].map(({ href, label }) => (
          <Link
            key={href}
            href={href}
            className={cn(
              "flex items-center rounded-md px-3 py-2 text-sm font-medium transition-colors",
              pathname === href
                ? "bg-primary/10 text-primary"
                : "text-muted-foreground hover:bg-muted hover:text-foreground",
            )}
          >
            {label}
          </Link>
        ))}
      </div>
    </nav>
  );
}
```

### Step 2: 빌드 확인

```bash
cd /Users/parkhanbin/git/study/anon-community/frontend && npm run build 2>&1 | tail -20
```

Expected: ✓ Compiled successfully

### Step 3: 커밋

```bash
git add "frontend/src/components/layout/left-sidebar.tsx"
git commit -m "feat(fe): add LeftSidebar with category navigation"
```

---

## Task 5: RightSidebar 컴포넌트 (HotPostsSection 이동)

**Files:**
- Create: `frontend/src/components/layout/right-sidebar.tsx`
- Modify: `frontend/src/app/(main)/page.tsx` (HotPostsSection 제거)

**배경:**
- `HotPostsSection`을 현재 `app/(main)/page.tsx`에서 RightSidebar로 이동
- `useHotTags(10)` 재사용 (검색 페이지에서 이미 사용 중)

### Step 1: RightSidebar 컴포넌트 생성

`frontend/src/components/layout/right-sidebar.tsx`:

```tsx
"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { HotPostsSection } from "@/components/post/hot-posts-section";
import { useHotTags } from "@/lib/hooks/use-search";

export function RightSidebar() {
  const router = useRouter();
  const { data: hotTagsData } = useHotTags(10);
  const hotTags = hotTagsData?.tags ?? [];

  return (
    <div className="sticky top-20 space-y-6">
      {/* Hot posts */}
      <HotPostsSection />

      {/* Hot tags */}
      {hotTags.length > 0 && (
        <div>
          <p className="mb-2 text-sm font-semibold">🏷️ 인기 태그</p>
          <div className="flex flex-wrap gap-1.5">
            {hotTags.map((tag) => (
              <button
                key={tag}
                onClick={() => router.push(`/search?tags=${encodeURIComponent(tag)}`)}
                className="inline-flex items-center rounded-full border border-border px-2.5 py-0.5 text-xs text-muted-foreground hover:border-primary hover:text-primary transition-colors"
              >
                #{tag}
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
```

### Step 2: page.tsx에서 HotPostsSection 제거

`frontend/src/app/(main)/page.tsx` 수정:

```tsx
import { FeedList } from "@/components/post/feed-list";
import { serverFetch } from "@/lib/api/client";
import { PageResponse, PostDetailResponse } from "@/types/api";

export default async function FeedPage() {
  let initialData: PageResponse<PostDetailResponse> | undefined;

  try {
    initialData = await serverFetch<PageResponse<PostDetailResponse>>("/feed?page=0&size=20");
  } catch {
    // Backend unavailable — FeedList renders empty state
  }

  return <FeedList initialData={initialData} />;
}
```

HotPostsSection import와 `<HotPostsSection />` 모두 제거. RightSidebar가 이를 담당.

### Step 3: 빌드 확인

```bash
cd /Users/parkhanbin/git/study/anon-community/frontend && npm run build 2>&1 | tail -20
```

Expected: ✓ Compiled successfully

### Step 4: 커밋

```bash
git add "frontend/src/components/layout/right-sidebar.tsx"
git add "frontend/src/app/(main)/page.tsx"
git commit -m "feat(fe): add RightSidebar with hot posts and hot tags"
```

---

## Task 6: 3-컬럼 레이아웃 + 모바일 하단 탭 바

**Files:**
- Create: `frontend/src/components/layout/mobile-bottom-nav.tsx`
- Modify: `frontend/src/app/(main)/layout.tsx`

**배경:**
- 데스크탑(≥lg): 3-컬럼 (LeftSidebar + main + RightSidebar)
- 태블릿(≥xl에서만 RightSidebar): 2-컬럼
- 모바일(<lg): 1-컬럼 + 하단 탭 바
- WriteFab은 데스크탑에서만 표시 (모바일은 탭 바의 글쓰기 버튼 사용)

### Step 1: MobileBottomNav 컴포넌트 생성

`frontend/src/components/layout/mobile-bottom-nav.tsx`:

```tsx
"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { Home, Search, PenSquare, User } from "lucide-react";
import { useAuthStore } from "@/stores/auth-store";
import { cn } from "@/lib/utils";

const NAV_ITEMS = [
  { href: "/",       icon: Home,      label: "홈",    requireAuth: false },
  { href: "/search", icon: Search,    label: "검색",  requireAuth: false },
  { href: "/posts/new", icon: PenSquare, label: "글쓰기", requireAuth: true },
  { href: "/my",     icon: User,      label: "마이",  requireAuth: true },
];

export function MobileBottomNav() {
  const pathname = usePathname();
  const router = useRouter();
  const { isLoggedIn, isInitialized } = useAuthStore();

  const handleNavClick = (href: string, requireAuth: boolean) => {
    if (requireAuth && isInitialized && !isLoggedIn) {
      router.push(`/login?callbackUrl=${encodeURIComponent(href)}`);
      return;
    }
    router.push(href);
  };

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-50 lg:hidden border-t bg-background">
      <div className="flex items-center justify-around h-14">
        {NAV_ITEMS.map(({ href, icon: Icon, label, requireAuth }) => {
          const isActive = href === "/" ? pathname === "/" : pathname.startsWith(href);
          return (
            <button
              key={href}
              onClick={() => handleNavClick(href, requireAuth)}
              className={cn(
                "flex flex-col items-center justify-center gap-0.5 flex-1 h-full text-xs font-medium transition-colors",
                isActive ? "text-primary" : "text-muted-foreground hover:text-foreground",
              )}
            >
              <Icon className={cn("h-5 w-5", isActive && "fill-primary/10")} />
              {label}
            </button>
          );
        })}
      </div>
    </nav>
  );
}
```

### Step 2: layout.tsx 3-컬럼으로 변경

`frontend/src/app/(main)/layout.tsx` 전체 교체:

```tsx
import { Suspense } from "react";
import { Header } from "@/components/layout/header";
import { LeftSidebar } from "@/components/layout/left-sidebar";
import { RightSidebar } from "@/components/layout/right-sidebar";
import { MobileBottomNav } from "@/components/layout/mobile-bottom-nav";
// WriteFab 제거: 데스크탑은 LeftSidebar 글쓰기 버튼, 모바일은 MobileBottomNav 글쓰기 탭으로 커버

export default function MainLayout({ children }: { children: React.ReactNode }) {
  return (
    <>
      <Header />
      <div className="container mx-auto px-4 py-6">
        <div className="flex gap-6 items-start">
          {/* Left sidebar: desktop only */}
          <aside className="hidden lg:block w-[220px] shrink-0">
            <Suspense fallback={null}>
              <LeftSidebar />
            </Suspense>
          </aside>

          {/* Main content */}
          <main className="flex-1 min-w-0 max-w-2xl">{children}</main>

          {/* Right sidebar: large desktop only */}
          <aside className="hidden xl:block w-[260px] shrink-0">
            <RightSidebar />
          </aside>
        </div>
      </div>

      {/* Mobile bottom nav (lg 미만에서만 표시) */}
      <MobileBottomNav />

      {/* Bottom padding for mobile nav */}
      <div className="h-14 lg:hidden" />
    </>
  );
}
```

**주의**: `LeftSidebar`는 `useSearchParams`를 사용하므로 `Suspense`로 감싸야 함. `RightSidebar`는 client component이므로 직접 사용 가능.

### Step 3: 빌드 확인

```bash
cd /Users/parkhanbin/git/study/anon-community/frontend && npm run build 2>&1 | tail -20
```

Expected: ✓ Compiled successfully

### Step 4: 커밋

```bash
git add "frontend/src/components/layout/mobile-bottom-nav.tsx"
git add "frontend/src/app/(main)/layout.tsx"
git commit -m "feat(fe): 3-column layout with left/right sidebars and mobile bottom nav"
```

---

## Task 7: 카테고리 이모지 배지 + Empty State 개선

**Files:**
- Modify: `frontend/src/types/api.ts` (POST_CATEGORIES에 emoji 추가)
- Modify: `frontend/src/components/post/post-card.tsx` (category badge 개선)
- Modify: `frontend/src/components/post/feed-list.tsx` (empty state 개선)

**배경:**
- `POST_CATEGORIES`에 emoji 필드 추가
- PostCard의 카테고리 배지에 이모지 표시
- Feed empty state에 일러스트(아이콘 기반) + 글쓰기 유도

### Step 1: POST_CATEGORIES에 emoji 추가

`frontend/src/types/api.ts`에서 `POST_CATEGORIES` 수정:

```ts
export const POST_CATEGORIES: { value: PostCategory; label: string; emoji: string }[] = [
  { value: "DAILY",       label: "일상",   emoji: "☀️" },
  { value: "ADVICE",      label: "고민",   emoji: "💬" },
  { value: "HUMOR",       label: "유머",   emoji: "😂" },
  { value: "INFORMATION", label: "정보",   emoji: "📚" },
  { value: "QUESTION",    label: "질문",   emoji: "❓" },
  { value: "DISCUSSION",  label: "토론",   emoji: "🗣️" },
  { value: "MEDIA",       label: "미디어", emoji: "🎬" },
  { value: "FEEDBACK",    label: "피드백", emoji: "📝" },
];
```

### Step 2: PostCard의 getCategoryLabel에서 emoji 포함

`frontend/src/components/post/post-card.tsx`에서 `getCategoryLabel` 함수 수정:

```ts
function getCategoryLabel(category: string): string {
  const cat = POST_CATEGORIES.find((c) => c.value === category);
  return cat ? `${cat.emoji} ${cat.label}` : category;
}
```

### Step 3: FeedList empty state 개선

`frontend/src/components/post/feed-list.tsx`에서 empty state 찾아서 개선:

현재 FeedList의 empty state 패턴을 찾아 아이콘 기반 일러스트로 교체:

```tsx
// Empty state (posts.length === 0 이면서 로딩 아닌 경우)
<div className="flex flex-col items-center justify-center py-20 text-center">
  <div className="mb-4 flex h-20 w-20 items-center justify-center rounded-full bg-muted">
    <span className="text-4xl">📝</span>
  </div>
  <h3 className="text-lg font-semibold">아직 게시글이 없습니다</h3>
  <p className="mt-1 text-sm text-muted-foreground">첫 번째 게시글을 작성해보세요!</p>
  <Button asChild className="mt-4" size="sm">
    <Link href="/posts/new">글쓰기</Link>
  </Button>
</div>
```

`feed-list.tsx`를 먼저 읽어보고 정확한 위치에 적용.

### Step 4: Search page의 empty state 개선

`frontend/src/app/(main)/search/page.tsx`에서 "검색 결과가 없습니다" 섹션에 아이콘 추가:

```tsx
{!isLoading && posts.length === 0 && hasQuery && (
  <div className="flex flex-col items-center py-16 text-center">
    <span className="text-5xl mb-4">🔍</span>
    <p className="text-lg font-medium text-muted-foreground">검색 결과가 없습니다</p>
    <p className="mt-1 text-sm text-muted-foreground">
      {keyword ? `"${keyword}"에 대한 검색 결과가 없습니다` : `선택한 필터에 해당하는 게시글이 없습니다`}
    </p>
  </div>
)}
```

### Step 5: 빌드 확인

```bash
cd /Users/parkhanbin/git/study/anon-community/frontend && npm run build 2>&1 | tail -20
```

Expected: ✓ Compiled successfully

### Step 6: 커밋

```bash
git add frontend/src/types/api.ts
git add "frontend/src/components/post/post-card.tsx"
git add "frontend/src/components/post/feed-list.tsx"
git add "frontend/src/app/(main)/search/page.tsx"
git commit -m "feat(fe): add emoji to category badges and improve empty states"
```

---

## Task 8: 전체 빌드 검증

**Files:** 없음 (검증만)

### Step 1: 백엔드 전체 테스트

```bash
/Users/parkhanbin/git/study/anon-community/gradlew test 2>&1 | tail -30
```

Expected: BUILD SUCCESSFUL

### Step 2: 프론트엔드 빌드

```bash
cd /Users/parkhanbin/git/study/anon-community/frontend && npm run build 2>&1 | tail -30
```

Expected: ✓ Compiled successfully, 모든 라우트 정상

### Step 3: 필수 라우트 확인

빌드 출력에서 다음 라우트가 존재하는지 확인:
- `/` (홈 피드)
- `/posts/[postId]` (게시글 상세)
- `/search` (검색)
- `/my` (마이페이지)
- `/settings` (설정)

### Step 4: 최종 커밋 (변경사항 없으면 스킵)

빌드가 성공하면 완료.

---

## 구현 주의사항

### 백엔드 (Task 1)
- `Comment.content`를 `val` → `var private set`으로 변경할 때, 기존 `Comment.create()` 호출부에서 영향 없음을 확인
- `CommentCommandPortAdapter.save()`는 JPA merge를 사용하므로 별도 update 메서드 불필요
- 보안: 타인 댓글 수정 시 `CommentNotFoundException`을 throw (not found처럼 보여서 authorId 노출 방지)
- `@Transactional`: `UpdateCommentUseCase`는 read-write이므로 `@Transactional` 필요

### 프론트엔드 (Tasks 2-7)
- `LeftSidebar`는 `useSearchParams`를 사용하므로 반드시 `<Suspense>` 안에서 사용
- 기존 `HotPostsSection`은 삭제하지 말고 import 위치만 변경 (page.tsx → right-sidebar.tsx)
- `MobileBottomNav`는 `lg:hidden`으로 모바일/태블릿에서만 표시
- `WriteFab`은 `hidden lg:block`으로 데스크탑에서만 표시
- 하단 탭 바 높이(h-14) 만큼의 spacer div를 layout.tsx 하단에 추가 (모바일 콘텐츠 가림 방지)
- git add 시 경로에 소괄호가 있으면 반드시 따옴표로 감쌀 것: `git add "frontend/src/app/(main)/layout.tsx"`
