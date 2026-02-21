import { useMutation, useQueryClient } from "@tanstack/react-query";
import { likesApi } from "@/lib/api/likes";
import { ApiError } from "@/lib/api/client";
import { PostDetailResponse } from "@/types/api";
import { toast } from "sonner";

export function useLike(postId: number) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => likesApi.like(postId),
    onMutate: async () => {
      await queryClient.cancelQueries({ queryKey: ["post", postId] });
      const previous = queryClient.getQueryData<PostDetailResponse>(["post", postId]);
      if (previous) {
        queryClient.setQueryData<PostDetailResponse>(["post", postId], {
          ...previous,
          likeCount: previous.likeCount + 1,
        });
      }
      return { previous };
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["feed"] });
    },
    onError: (err, _vars, context) => {
      if (context?.previous) {
        queryClient.setQueryData(["post", postId], context.previous);
      }
      if (err instanceof ApiError && err.code === "ALREADY_LIKED") {
        return;
      }
      toast.error("좋아요에 실패했습니다");
    },
  });
}

export function useUnlike(postId: number) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => likesApi.unlike(postId),
    onMutate: async () => {
      await queryClient.cancelQueries({ queryKey: ["post", postId] });
      const previous = queryClient.getQueryData<PostDetailResponse>(["post", postId]);
      if (previous) {
        queryClient.setQueryData<PostDetailResponse>(["post", postId], {
          ...previous,
          likeCount: Math.max(0, previous.likeCount - 1),
        });
      }
      return { previous };
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["feed"] });
    },
    onError: (err, _vars, context) => {
      if (context?.previous) {
        queryClient.setQueryData(["post", postId], context.previous);
      }
      if (err instanceof ApiError && err.code === "LIKE_NOT_FOUND") {
        return;
      }
      toast.error("좋아요 취소에 실패했습니다");
    },
  });
}
