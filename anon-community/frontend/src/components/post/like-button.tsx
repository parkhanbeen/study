"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Heart } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useLike, useUnlike } from "@/lib/hooks/use-likes";
import { ApiError } from "@/lib/api/client";
import { useAuthStore } from "@/stores/auth-store";
import { cn } from "@/lib/utils";
import { toast } from "sonner";

interface LikeButtonProps {
  postId: number;
  likeCount: number;
}

export function LikeButton({ postId, likeCount }: LikeButtonProps) {
  const [liked, setLiked] = useState(false);
  const [count, setCount] = useState(likeCount);
  const [animate, setAnimate] = useState(false);
  const { isLoggedIn } = useAuthStore();
  const router = useRouter();
  const likeMutation = useLike(postId);
  const unlikeMutation = useUnlike(postId);

  const handleClick = () => {
    if (!isLoggedIn) {
      toast.info("로그인이 필요합니다", {
        action: {
          label: "로그인",
          onClick: () => router.push(`/login?callbackUrl=${encodeURIComponent(`/posts/${postId}`)}`),
        },
      });
      return;
    }

    if (liked) {
      unlikeMutation.mutate(undefined, {
        onSuccess: () => {
          setLiked(false);
          setCount((c) => Math.max(0, c - 1));
        },
        onError: (err) => {
          if (err instanceof ApiError && err.code === "LIKE_NOT_FOUND") {
            setLiked(false);
          }
        },
      });
    } else {
      likeMutation.mutate(undefined, {
        onSuccess: () => {
          setLiked(true);
          setCount((c) => c + 1);
          setAnimate(true);
          setTimeout(() => setAnimate(false), 300);
        },
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
      disabled={isPending}
      className="gap-1.5"
    >
      <Heart
        className={cn(
          "h-5 w-5 transition-all duration-200",
          liked && "fill-red-500 text-red-500",
          animate && "scale-125",
        )}
      />
      <span className={cn("tabular-nums", liked && "text-red-500 font-medium")}>{count}</span>
    </Button>
  );
}
