"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { CommentItem } from "./comment-item";
import { CommentForm } from "./comment-form";
import { useComments } from "@/lib/hooks/use-comments";

interface CommentListProps {
  postId: number;
}

export function CommentList({ postId }: CommentListProps) {
  const [replyTo, setReplyTo] = useState<number | null>(null);
  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading } = useComments(postId);

  const comments = data?.pages.flatMap((p) => p.content) ?? [];

  if (isLoading) {
    return (
      <div className="space-y-4">
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
      <CommentForm postId={postId} />

      {comments.length === 0 && (
        <p className="py-4 text-center text-sm text-muted-foreground">No comments yet</p>
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
          {isFetchingNextPage ? "Loading..." : "Load more comments"}
        </Button>
      )}
    </div>
  );
}
