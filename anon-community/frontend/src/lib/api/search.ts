import { fetchAPI } from "./client";
import { PostResponse, PageResponse, CursorResponse, SearchPostsParams, CursorSearchPostsParams, HotTagsResponse } from "@/types/api";

function buildSearchParams(params: Record<string, string | string[] | number | undefined>): string {
  const sp = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined) return;
    if (Array.isArray(value)) {
      value.forEach((v) => sp.append(key, v));
    } else {
      sp.set(key, String(value));
    }
  });
  return sp.toString();
}

export const searchApi = {
  posts: (params: SearchPostsParams) =>
    fetchAPI<PageResponse<PostResponse>>(`/search/posts?${buildSearchParams(params as Record<string, string | string[] | number | undefined>)}`),

  postsCursor: (params: CursorSearchPostsParams) =>
    fetchAPI<CursorResponse<PostResponse>>(`/search/posts/cursor?${buildSearchParams(params as Record<string, string | string[] | number | undefined>)}`),

  hotTags: (limit = 10) =>
    fetchAPI<HotTagsResponse>(`/search/hot-tags?limit=${limit}`),
};
