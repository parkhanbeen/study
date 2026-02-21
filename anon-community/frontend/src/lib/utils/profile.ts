import { ActiveProfile, ProfileResponse } from "@/types/profile";

export function toActiveProfile(p: ProfileResponse): ActiveProfile {
  return {
    id: p.id,
    nickname: p.nickname,
    bio: p.bio,
    postCount: p.postCount,
    followerCount: p.followerCount,
    followingCount: p.followingCount,
    isDefault: p.isDefault,
  };
}
