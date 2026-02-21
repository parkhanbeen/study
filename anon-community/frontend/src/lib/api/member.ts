import { fetchAPI } from "./client";
import { PageResponse, PostDetailResponse } from "@/types/api";

export const memberApi = {
  myPosts: (page = 0, size = 20) =>
    fetchAPI<PageResponse<PostDetailResponse>>(`/members/me/posts?page=${page}&size=${size}`),

  myLikedPosts: (page = 0, size = 20) =>
    fetchAPI<PageResponse<PostDetailResponse>>(`/members/me/liked-posts?page=${page}&size=${size}`),
};
