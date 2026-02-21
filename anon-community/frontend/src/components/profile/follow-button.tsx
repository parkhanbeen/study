"use client";
import { Button } from "@/components/ui/button";
import { useFollow } from "@/lib/hooks/use-follow";
import { useAuthStore } from "@/stores/auth-store";
import { useRouter } from "next/navigation";

interface FollowButtonProps {
  profileId: number;
  isFollowing: boolean;
  isMe: boolean;
}

export function FollowButton({ profileId, isFollowing, isMe }: FollowButtonProps) {
  const { isLoggedIn } = useAuthStore();
  const router = useRouter();
  const { follow, unfollow, isFollowPending, isUnfollowPending } =
    useFollow(profileId);

  if (isMe) return null;

  const handleClick = () => {
    if (!isLoggedIn) {
      router.push(`/login?callbackUrl=/profiles/${profileId}`);
      return;
    }
    if (isFollowing) {
      unfollow();
    } else {
      follow();
    }
  };

  return (
    <Button
      variant={isFollowing ? "outline" : "default"}
      size="sm"
      onClick={handleClick}
      disabled={isFollowPending || isUnfollowPending}
    >
      {isFollowing ? "언팔로우" : "팔로우"}
    </Button>
  );
}
