"use client";

import { Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { CommentResponse } from "@/types/api";
import { timeAgo } from "@/lib/utils";
import { useAuthStore } from "@/stores/auth-store";
import { useDeleteComment } from "@/lib/hooks/use-comments";

interface CommentItemProps {
  comment: CommentResponse;
  postId: number;
  onReply?: (commentId: number) => void;
}

export function CommentItem({ comment, postId, onReply }: CommentItemProps) {
  const { memberId } = useAuthStore();
  const deleteMutation = useDeleteComment(postId);
  const isAuthor = memberId === comment.authorId;

  return (
    <div className={comment.depth === 1 ? "ml-8 border-l-2 pl-4" : ""}>
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2 text-sm text-muted-foreground">
          <span className="font-medium text-foreground">Anonymous #{comment.authorId}</span>
          <span>·</span>
          <span>{timeAgo(comment.createdAt)}</span>
        </div>
        <div className="flex items-center gap-1">
          {comment.depth === 0 && onReply && (
            <Button variant="ghost" size="sm" onClick={() => onReply(comment.id)}>
              Reply
            </Button>
          )}
          {isAuthor && (
            <Button
              variant="ghost"
              size="icon"
              className="h-8 w-8 text-destructive"
              onClick={() => deleteMutation.mutate(comment.id)}
              disabled={deleteMutation.isPending}
            >
              <Trash2 className="h-4 w-4" />
            </Button>
          )}
        </div>
      </div>
      <p className="mt-1 text-sm">{comment.content}</p>
    </div>
  );
}
