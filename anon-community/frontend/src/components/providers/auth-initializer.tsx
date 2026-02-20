"use client";

import { useEffect } from "react";
import { useAuthStore } from "@/stores/auth-store";

export function AuthInitializer() {
  const { isLoggedIn, setAuth, setInitialized } = useAuthStore();

  useEffect(() => {
    if (isLoggedIn) {
      setInitialized();
      return;
    }

    fetch("/api/auth/me")
      .then((res) => res.json())
      .then((data) => {
        if (data.isLoggedIn && data.memberId) {
          setAuth(data.memberId, `Anon#${data.memberId}`);
        }
      })
      .catch(() => {})
      .finally(() => {
        setInitialized();
      });
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  return null;
}
