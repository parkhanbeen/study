"use client";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { profilesApi } from "@/lib/api/profiles";

export function useFollow(profileId: number) {
  const queryClient = useQueryClient();

  const followMutation = useMutation({
    mutationFn: () => profilesApi.follow(profileId),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: ["profile", profileId] }),
  });

  const unfollowMutation = useMutation({
    mutationFn: () => profilesApi.unfollow(profileId),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: ["profile", profileId] }),
  });

  return {
    follow: followMutation.mutate,
    unfollow: unfollowMutation.mutate,
    isFollowPending: followMutation.isPending,
    isUnfollowPending: unfollowMutation.isPending,
  };
}
