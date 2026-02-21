import { useInfiniteQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { commentsApi } from "@/lib/api/comments";
import { CreateCommentRequest } from "@/types/api";
import { toast } from "sonner";

export function useComments(postId: number) {
  return useInfiniteQuery({
    queryKey: ["comments", postId],
    queryFn: ({ pageParam = 0 }) => commentsApi.list(postId, pageParam),
    initialPageParam: 0,
    getNextPageParam: (lastPage) =>
      lastPage.page + 1 < lastPage.totalPages ? lastPage.page + 1 : undefined,
  });
}

export function useCreateComment(postId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateCommentRequest) => commentsApi.create(postId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["comments", postId] });
      queryClient.invalidateQueries({ queryKey: ["post", postId] });
      toast.success("댓글이 등록되었습니다");
    },
    onError: () => {
      toast.error("댓글 등록에 실패했습니다");
    },
  });
}

export function useDeleteComment(postId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (commentId: number) => commentsApi.delete(postId, commentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["comments", postId] });
      queryClient.invalidateQueries({ queryKey: ["post", postId] });
      toast.success("댓글이 삭제되었습니다");
    },
    onError: () => {
      toast.error("댓글 삭제에 실패했습니다");
    },
  });
}
