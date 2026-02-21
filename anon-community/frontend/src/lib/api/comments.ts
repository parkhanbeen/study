import { fetchAPI } from "./client";
import { CommentResponse, CreateCommentRequest, PageResponse } from "@/types/api";

export const commentsApi = {
  list: (postId: number, page = 0, size = 20) =>
    fetchAPI<PageResponse<CommentResponse>>(`/posts/${postId}/comments?page=${page}&size=${size}`),

  create: (postId: number, data: CreateCommentRequest) =>
    fetchAPI<CommentResponse>(`/posts/${postId}/comments`, { method: "POST", body: JSON.stringify(data) }),

  delete: (postId: number, commentId: number) =>
    fetchAPI<void>(`/posts/${postId}/comments/${commentId}`, { method: "DELETE" }),

  update: (postId: number, commentId: number, content: string) =>
    fetchAPI<CommentResponse>(`/posts/${postId}/comments/${commentId}`, {
      method: "PUT",
      body: JSON.stringify({ content }),
    }),
};
