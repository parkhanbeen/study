"use client";

import { useEffect } from "react";
import { useAuthStore } from "@/stores/auth-store";
import { fetchAPI } from "@/lib/api/client";
import { ProfileResponse } from "@/types/profile";
import { toActiveProfile } from "@/lib/utils/profile";

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
          try {
            const profilesRaw = await fetchAPI<ProfileResponse[]>("/members/me/profiles");
            const profiles = profilesRaw.map(toActiveProfile);
            const activeProfile = profiles.find((p) => p.isDefault) ?? profiles[0] ?? null;
            if (activeProfile) {
              setAuth(data.memberId, activeProfile, profiles);
            } else {
              // No profiles found — still mark as logged in with empty profile list
              // This shouldn't happen in normal flow
              console.warn("No profiles found for logged-in user:", data.memberId);
            }
          } catch (err) {
            console.error("Failed to fetch profiles during auth initialization:", err);
            // Still continue — user is logged in, profiles can be fetched later
          } finally {
            setInitialized();
          }
        } else {
          setInitialized();
        }
      })
      .catch(() => {
        setInitialized();
      });
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  return null;
}
