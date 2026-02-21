"use client";

import { useRouter } from "next/navigation";
import { Pencil, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useAuthStore } from "@/stores/auth-store";
import { useDeletePost } from "@/lib/hooks/use-posts";
import { MemberId, PostId } from "@/types/api";
import { useState } from "react";

interface PostActionsProps {
  postId: PostId;
  authorId: MemberId;
}

export function PostActions({ postId, authorId }: PostActionsProps) {
  const router = useRouter();
  const { activeProfile } = useAuthStore();
  const deleteMutation = useDeletePost();
  const [confirming, setConfirming] = useState(false);

  if (!activeProfile || activeProfile.id !== authorId) return null;

  const handleDelete = () => {
    if (!confirming) {
      setConfirming(true);
      return;
    }
    deleteMutation.mutate(postId, {
      onSuccess: () => {
        router.push("/");
        router.refresh();
      },
    });
  };

  return (
    <div className="flex items-center gap-2">
      <Button variant="outline" size="sm" onClick={() => router.push(`/posts/${postId}/edit`)}>
        <Pencil className="mr-1.5 h-3.5 w-3.5" />
        수정
      </Button>
      <Button
        variant={confirming ? "destructive" : "outline"}
        size="sm"
        onClick={handleDelete}
        disabled={deleteMutation.isPending}
        onBlur={() => setConfirming(false)}
      >
        <Trash2 className="mr-1.5 h-3.5 w-3.5" />
        {deleteMutation.isPending ? "삭제 중..." : confirming ? "확인?" : "삭제"}
      </Button>
    </div>
  );
}
