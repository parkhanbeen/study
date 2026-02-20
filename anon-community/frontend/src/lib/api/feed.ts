import { fetchAPI } from "./client";
import { PostDetailResponse, PageResponse, PostCategory } from "@/types/api";

export type FeedSort = "latest" | "oldest";

export const feedApi = {
  get: (page = 0, size = 20, category?: PostCategory, sort: FeedSort = "latest") => {
    const params = new URLSearchParams();
    params.set("page", String(page));
    params.set("size", String(size));
    if (category) params.set("category", category);
    params.set("sortBy", "createdAt");
    params.set("sortDirection", sort === "latest" ? "DESC" : "ASC");
    return fetchAPI<PageResponse<PostDetailResponse>>(`/feed?${params.toString()}`);
  },
  getHot: (limit = 5) => {
    const params = new URLSearchParams();
    params.set("page", "0");
    params.set("size", String(limit));
    params.set("sortBy", "likeCount");
    params.set("sortDirection", "DESC");
    return fetchAPI<PageResponse<PostDetailResponse>>(`/feed?${params.toString()}`);
  },
};
