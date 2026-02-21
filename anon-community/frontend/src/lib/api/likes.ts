import { fetchAPI } from "./client";

export const likesApi = {
  like: (postId: number) =>
    fetchAPI<void>(`/posts/${postId}/likes`, { method: "POST" }),

  unlike: (postId: number) =>
    fetchAPI<void>(`/posts/${postId}/likes`, { method: "DELETE" }),
};
