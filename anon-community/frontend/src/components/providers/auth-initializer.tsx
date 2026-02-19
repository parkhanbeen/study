"use client";

import { useEffect } from "react";
import { useAuthStore } from "@/stores/auth-store";

export function AuthInitializer() {
  const { isLoggedIn, setAuth } = useAuthStore();

  useEffect(() => {
    if (isLoggedIn) return;

    fetch("/api/auth/me")
      .then((res) => res.json())
      .then((data) => {
        if (data.isLoggedIn && data.memberId) {
          setAuth(data.memberId, `Anon#${data.memberId}`);
        }
      })
      .catch(() => {});
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  return null;
}
