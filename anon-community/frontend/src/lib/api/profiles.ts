import { fetchAPI } from "./client";
import { ProfileResponse } from "@/types/profile";
import { PageResponse, PostDetailResponse } from "@/types/api";

export const profilesApi = {
  getProfile: (profileId: number) =>
    fetchAPI<ProfileResponse>(`/profiles/${profileId}`),

  getProfilePosts: (profileId: number, page = 0) =>
    fetchAPI<PageResponse<PostDetailResponse>>(
      `/profiles/${profileId}/posts?page=${page}&size=20`,
    ),

  getFollowers: (profileId: number, page = 0) =>
    fetchAPI<PageResponse<ProfileResponse>>(
      `/profiles/${profileId}/followers?page=${page}&size=20`,
    ),

  getFollowing: (profileId: number, page = 0) =>
    fetchAPI<PageResponse<ProfileResponse>>(
      `/profiles/${profileId}/following?page=${page}&size=20`,
    ),

  follow: (profileId: number) =>
    fetchAPI<void>(`/profiles/${profileId}/follow`, { method: "POST" }),

  unfollow: (profileId: number) =>
    fetchAPI<void>(`/profiles/${profileId}/follow`, { method: "DELETE" }),
};
