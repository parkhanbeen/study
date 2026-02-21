"use client";

import { useEffect } from "react";
import { useAuthStore } from "@/stores/auth-store";
import { fetchAPI } from "@/lib/api/client";
import { ProfileResponse, ActiveProfile } from "@/types/profile";

function toActiveProfile(p: ProfileResponse): ActiveProfile {
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

export function AuthInitializer() {
  const { isLoggedIn, setAuth, setInitialized } = useAuthStore();

  useEffect(() => {
    if (isLoggedIn) {
      setInitialized();
      return;
    }

    fetch("/api/auth/me")
      .then((res) => res.json())
      .then(async (data) => {
        if (data.isLoggedIn && data.memberId) {
          const profilesRaw = await fetchAPI<ProfileResponse[]>("/members/me/profiles");
          const profiles = profilesRaw.map(toActiveProfile);
          const activeProfile =
            profiles.find((p) => p.isDefault) ?? profiles[0];
          if (activeProfile) {
            setAuth(data.memberId, activeProfile, profiles);
          }
        }
      })
      .catch(() => {})
      .finally(() => {
        setInitialized();
      });
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  return null;
}
