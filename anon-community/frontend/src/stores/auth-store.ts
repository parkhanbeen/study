import { create } from "zustand";
import { MemberId } from "@/types/api";

interface AuthState {
  isLoggedIn: boolean;
  isInitialized: boolean;
  memberId: MemberId | null;
  nickname: string | null;
  setAuth: (memberId: MemberId, nickname: string) => void;
  clearAuth: () => void;
  setInitialized: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  isLoggedIn: false,
  isInitialized: false,
  memberId: null,
  nickname: null,
  setAuth: (memberId, nickname) =>
    set({ isLoggedIn: true, memberId, nickname }),
  clearAuth: () =>
    set({ isLoggedIn: false, memberId: null, nickname: null }),
  setInitialized: () => set({ isInitialized: true }),
}));
