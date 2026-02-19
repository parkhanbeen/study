"use client";

import { useState } from "react";
import { Heart } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useLike, useUnlike } from "@/lib/hooks/use-likes";
import { ApiError } from "@/lib/api/client";
import { useAuthStore } from "@/stores/auth-store";
import { cn } from "@/lib/utils";

interface LikeButtonProps {
  postId: number;
  likeCount: number;
}

export function LikeButton({ postId, likeCount }: LikeButtonProps) {
  const [liked, setLiked] = useState(false);
  const { isLoggedIn } = useAuthStore();
  const likeMutation = useLike(postId);
  const unlikeMutation = useUnlike(postId);

  const handleClick = () => {
    if (!isLoggedIn) return;

    if (liked) {
      unlikeMutation.mutate(undefined, {
        onSuccess: () => setLiked(false),
        onError: (err) => {
          if (err instanceof ApiError && err.code === "LIKE_NOT_FOUND") {
            setLiked(false);
          }
        },
      });
    } else {
      likeMutation.mutate(undefined, {
        onSuccess: () => setLiked(true),
        onError: (err) => {
          if (err instanceof ApiError && err.code === "ALREADY_LIKED") {
            setLiked(true);
          }
        },
      });
    }
  };

  const isPending = likeMutation.isPending || unlikeMutation.isPending;

  return (
    <Button
      variant="ghost"
      size="sm"
      onClick={handleClick}
      disabled={isPending || !isLoggedIn}
      className="gap-1"
    >
      <Heart className={cn("h-5 w-5", liked && "fill-red-500 text-red-500")} />
      <span>{likeCount}</span>
    </Button>
  );
}
