import { fetchAPI } from "./client";
import { PostResponse, PageResponse } from "@/types/api";

export const feedApi = {
  get: (page = 0, size = 20) =>
    fetchAPI<PageResponse<PostResponse>>(`/feed?page=${page}&size=${size}`),
};
