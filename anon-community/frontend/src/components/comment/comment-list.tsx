"use client";

import { useMemo, useState } from "react";
import { MessageCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { CommentItem } from "./comment-item";
import { CommentForm } from "./comment-form";
import { useComments } from "@/lib/hooks/use-comments";
import { CommentResponse } from "@/types/api";

interface CommentListProps {
  postId: number;
  initialCommentCount?: number;
}

/**
 * Groups flat comment list into threads: parent followed by its replies.
 * Backend already returns thread order, but this ensures correct rendering
 * even if replies span pagination boundaries.
 */
function groupByThread(comments: CommentResponse[]): CommentResponse[] {
  const topLevel: CommentResponse[] = [];
  const repliesByParent = new Map<number, CommentResponse[]>();

  for (const c of comments) {
    if (c.depth === 0) {
      topLevel.push(c);
    } else if (c.parentCommentId != null) {
      const arr = repliesByParent.get(c.parentCommentId) ?? [];
      arr.push(c);
      repliesByParent.set(c.parentCommentId, arr);
    }
  }

  const result: CommentResponse[] = [];
  for (const parent of topLevel) {
    result.push(parent);
    const replies = repliesByParent.get(parent.id);
    if (replies) result.push(...replies);
  }

  // Orphan replies whose parent isn't in the current pages
  const resultIds = new Set<number>(result.map((c) => c.id));
  for (const c of comments) {
    if (c.depth === 1 && !resultIds.has(c.id)) {
      result.push(c);
      resultIds.add(c.id);
    }
  }

  return result;
}

export function CommentList({ postId, initialCommentCount }: CommentListProps) {
  const [replyTo, setReplyTo] = useState<number | null>(null);
  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading, isError } = useComments(postId);

  const comments = useMemo(() => {
    const flat = data?.pages.flatMap((p) => p.content) ?? [];
    return groupByThread(flat);
  }, [data]);

  const commentCount = data?.pages[0]?.totalElements ?? initialCommentCount ?? 0;

  if (isLoading) {
    return (
      <div className="space-y-4">
        <h3 className="flex items-center gap-1.5 text-sm font-semibold">
          <MessageCircle className="h-4 w-4" /> 댓글
        </h3>
        {Array.from({ length: 3 }).map((_, i) => (
          <div key={i} className="space-y-2">
            <Skeleton className="h-4 w-32" />
            <Skeleton className="h-4 w-full" />
          </div>
        ))}
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <h3 className="flex items-center gap-1.5 text-sm font-semibold">
        <MessageCircle className="h-4 w-4" />
        댓글 {commentCount > 0 && <span className="text-muted-foreground font-normal">({commentCount})</span>}
      </h3>

      {isError && (
        <p className="py-6 text-center text-sm text-destructive">
          댓글을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.
        </p>
      )}

      {!isError && comments.length === 0 && (
        <p className="py-6 text-center text-sm text-muted-foreground">아직 댓글이 없습니다. 첫 번째 댓글을 남겨보세요!</p>
      )}

      {comments.map((comment) => (
        <div key={comment.id}>
          <CommentItem
            comment={comment}
            postId={postId}
            onReply={comment.depth === 0 ? (id) => setReplyTo(id) : undefined}
          />
          {replyTo === comment.id && (
            <div className="ml-8 mt-2">
              <CommentForm
                postId={postId}
                parentCommentId={comment.id}
                onCancel={() => setReplyTo(null)}
              />
            </div>
          )}
        </div>
      ))}

      {hasNextPage && (
        <Button
          variant="ghost"
          className="w-full"
          onClick={() => fetchNextPage()}
          disabled={isFetchingNextPage}
        >
          {isFetchingNextPage ? "불러오는 중..." : "댓글 더 보기"}
        </Button>
      )}

      {!isError && <CommentForm postId={postId} />}
    </div>
  );
}
