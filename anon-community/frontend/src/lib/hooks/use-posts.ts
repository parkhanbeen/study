import { useMutation, useQueryClient } from "@tanstack/react-query";
import { postsApi } from "@/lib/api/posts";
import { CreatePostRequest, UpdatePostRequest } from "@/types/api";
import { toast } from "sonner";

export function useCreatePost() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreatePostRequest) => postsApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["feed"] });
      toast.success("게시글이 작성되었습니다");
    },
    onError: () => toast.error("게시글 작성에 실패했습니다"),
  });
}

export function useUpdatePost(postId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: UpdatePostRequest) => postsApi.update(postId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["feed"] });
      queryClient.invalidateQueries({ queryKey: ["post", postId] });
      toast.success("게시글이 수정되었습니다");
    },
    onError: () => toast.error("게시글 수정에 실패했습니다"),
  });
}

export function useDeletePost() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (postId: number) => postsApi.delete(postId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["feed"] });
      toast.success("게시글이 삭제되었습니다");
    },
    onError: () => toast.error("게시글 삭제에 실패했습니다"),
  });
}
