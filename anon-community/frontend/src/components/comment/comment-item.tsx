"use client";

import { Reply, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
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
          {comment.depth === 0 && onReply && (
            <Button
              variant="ghost"
              size="sm"
              className="h-7 gap-1 px-2 text-xs text-muted-foreground hover:text-foreground"
              onClick={() => onReply(comment.id)}
            >
              <Reply className="h-3 w-3" /> 답글
            </Button>
          )}
          {isAuthor && (
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
          )}
        </div>
      </div>
      <p className="mt-1 text-sm leading-relaxed">{comment.content}</p>
    </div>
  );
}
