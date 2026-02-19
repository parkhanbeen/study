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
      toast.success("Post created");
    },
    onError: () => toast.error("Failed to create post"),
  });
}

export function useUpdatePost(postId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: UpdatePostRequest) => postsApi.update(postId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["feed"] });
      queryClient.invalidateQueries({ queryKey: ["post", postId] });
      toast.success("Post updated");
    },
    onError: () => toast.error("Failed to update post"),
  });
}

export function useDeletePost() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (postId: number) => postsApi.delete(postId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["feed"] });
      toast.success("Post deleted");
    },
    onError: () => toast.error("Failed to delete post"),
  });
}
