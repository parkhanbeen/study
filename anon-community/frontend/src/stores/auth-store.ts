import { create } from "zustand";
import { ActiveProfile, LinkedAccount } from "@/types/profile";

const LINKED_ACCOUNTS_KEY = "linked_accounts";

function loadLinkedAccounts(): LinkedAccount[] {
  if (typeof window === "undefined") return [];
  try {
    const raw = localStorage.getItem(LINKED_ACCOUNTS_KEY);
    return raw ? (JSON.parse(raw) as LinkedAccount[]) : [];
  } catch {
    return [];
  }
}

function saveLinkedAccounts(accounts: LinkedAccount[]): void {
  if (typeof window === "undefined") return;
  try {
    localStorage.setItem(LINKED_ACCOUNTS_KEY, JSON.stringify(accounts));
  } catch {
    // Ignore storage errors
  }
}

interface AuthState {
  memberId: number | null;
  activeProfile: ActiveProfile | null;
  profiles: ActiveProfile[];
  accounts: LinkedAccount[];
  isInitialized: boolean;

  // computed
  isLoggedIn: boolean;

  // actions
  setAuth: (memberId: number, activeProfile: ActiveProfile, profiles: ActiveProfile[]) => void;
  switchProfile: (profileId: number) => Promise<void>;
  addAccount: (account: LinkedAccount) => void;
  removeAccount: (memberId: number) => void;
  logout: () => void;
  /** @deprecated Use logout() instead */
  clearAuth: () => void;
  setInitialized: () => void;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  memberId: null,
  activeProfile: null,
  profiles: [],
  accounts: loadLinkedAccounts(),
  isInitialized: false,
  isLoggedIn: false,

  setAuth: (memberId, activeProfile, profiles) =>
    set({ isLoggedIn: true, memberId, activeProfile, profiles }),

  switchProfile: async (profileId: number) => {
    const res = await fetch("/api/auth/switch-profile", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ profileId }),
    });

    if (!res.ok) {
      throw new Error("프로필 전환에 실패했습니다");
    }

    const profiles = get().profiles;
    const newActive = profiles.find((p) => p.id === profileId);
    if (newActive) {
      set({ activeProfile: newActive });
    }
  },

  addAccount: (account: LinkedAccount) => {
    const current = get().accounts;
    const updated = [
      ...current.filter((a) => a.memberId !== account.memberId),
      account,
    ];
    saveLinkedAccounts(updated);
    set({ accounts: updated });
  },

  removeAccount: (memberId: number) => {
    const updated = get().accounts.filter((a) => a.memberId !== memberId);
    saveLinkedAccounts(updated);
    set({ accounts: updated });
  },

  logout: () => {
    set({
      isLoggedIn: false,
      memberId: null,
      activeProfile: null,
      profiles: [],
    });
  },

  clearAuth: () => {
    get().logout();
  },

  setInitialized: () => set({ isInitialized: true }),
}));
