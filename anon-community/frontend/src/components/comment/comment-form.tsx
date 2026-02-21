"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { useCreateComment } from "@/lib/hooks/use-comments";
import { useAuthStore } from "@/stores/auth-store";

interface CommentFormProps {
  postId: number;
  parentCommentId?: number;
  onCancel?: () => void;
}

export function CommentForm({ postId, parentCommentId, onCancel }: CommentFormProps) {
  const [content, setContent] = useState("");
  const { isLoggedIn } = useAuthStore();
  const createMutation = useCreateComment(postId);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!content.trim()) return;
    createMutation.mutate(
      { content: content.trim(), parentCommentId },
      {
        onSuccess: () => {
          setContent("");
          onCancel?.();
        },
      },
    );
  };

  if (!isLoggedIn) {
    return <p className="text-sm text-muted-foreground">댓글을 작성하려면 로그인하세요</p>;
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-2">
      <Textarea
        value={content}
        onChange={(e) => setContent(e.target.value)}
        placeholder={parentCommentId ? "답글을 작성하세요..." : "댓글을 작성하세요..."}
        rows={3}
        maxLength={1000}
      />
      <div className="flex justify-end gap-2">
        {onCancel && (
          <Button type="button" variant="ghost" size="sm" onClick={onCancel}>
            취소
          </Button>
        )}
        <Button type="submit" size="sm" disabled={!content.trim() || createMutation.isPending}>
          {createMutation.isPending ? "등록 중..." : "등록"}
        </Button>
      </div>
    </form>
  );
}
