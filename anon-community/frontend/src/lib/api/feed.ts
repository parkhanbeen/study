import { fetchAPI } from "./client";
import { PostDetailResponse, PageResponse, PostCategory, CursorResponse } from "@/types/api";

export type FeedSort = "latest" | "oldest" | "likes" | "comments" | "views";

const SORT_MAP: Record<FeedSort, { sortBy: string; sortDirection: string }> = {
  latest:   { sortBy: "createdAt",    sortDirection: "DESC" },
  oldest:   { sortBy: "createdAt",    sortDirection: "ASC"  },
  likes:    { sortBy: "likeCount",    sortDirection: "DESC" },
  comments: { sortBy: "commentCount", sortDirection: "DESC" },
  views:    { sortBy: "viewCount",    sortDirection: "DESC" },
};

export const feedApi = {
  get: (page = 0, size = 20, category?: PostCategory, sort: FeedSort = "latest") => {
    const params = new URLSearchParams();
    params.set("page", String(page));
    params.set("size", String(size));
    if (category) params.set("category", category);
    const { sortBy, sortDirection } = SORT_MAP[sort];
    params.set("sortBy", sortBy);
    params.set("sortDirection", sortDirection);
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
  getFollowing: (cursor?: string, size = 20) =>
    fetchAPI<CursorResponse<PostDetailResponse>>(
      `/feed/following?${new URLSearchParams({ ...(cursor ? { cursor } : {}), size: String(size) }).toString()}`
    ),
};
