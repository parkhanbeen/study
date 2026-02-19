import { fetchAPI } from "./client";
import { PostResponse, PostDetailResponse, CreatePostRequest, UpdatePostRequest } from "@/types/api";

export const postsApi = {
  create: (data: CreatePostRequest) =>
    fetchAPI<PostResponse>("/posts", { method: "POST", body: JSON.stringify(data) }),

  get: (postId: number) =>
    fetchAPI<PostDetailResponse>(`/posts/${postId}`),

  update: (postId: number, data: UpdatePostRequest) =>
    fetchAPI<PostResponse>(`/posts/${postId}`, { method: "PUT", body: JSON.stringify(data) }),

  delete: (postId: number) =>
    fetchAPI<void>(`/posts/${postId}`, { method: "DELETE" }),
};
