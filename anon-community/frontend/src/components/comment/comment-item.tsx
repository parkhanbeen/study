"use client";

import { useState, useEffect } from "react";
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

  useEffect(() => {
    if (!isEditing) {
      setEditContent(comment.content);
    }
  }, [comment.content, isEditing]);

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    if (!editContent.trim()) return;
    updateMutation.mutate(
      { commentId: comment.id, content: editContent.trim() },
      { onSuccess: () => setIsEditing(false) },
    );
  };

  return (
    <div className={comment.depth >= 1 ? "ml-8 border-l-2 border-muted pl-4" : ""}>
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
                aria-label="댓글 수정"
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
                    aria-label="댓글 삭제"
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
            aria-label="댓글 내용 수정"
          />
          {updateMutation.isError && (
            <p className="text-xs text-destructive">저장에 실패했습니다. 다시 시도해주세요.</p>
          )}
          <div className="flex gap-2">
            <Button size="sm" type="submit" disabled={updateMutation.isPending || !editContent.trim()}>
              {updateMutation.isPending ? "저장 중..." : "저장"}
            </Button>
            <Button size="sm" variant="ghost" type="button" onClick={() => { setIsEditing(false); updateMutation.reset(); }}>
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
