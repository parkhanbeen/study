import { useCallback } from "react";
import { PostFormData } from "@/lib/validations/post";

const DRAFT_KEY = "post_draft";

interface DraftData extends PostFormData {
  savedAt: number;
}

export function useDraft() {
  const saveDraft = useCallback((data: PostFormData) => {
    try {
      const draft: DraftData = { ...data, savedAt: Date.now() };
      localStorage.setItem(DRAFT_KEY, JSON.stringify(draft));
    } catch {
      // localStorage unavailable (SSR guard or private mode)
    }
  }, []);

  const loadDraft = useCallback((): DraftData | null => {
    try {
      const raw = localStorage.getItem(DRAFT_KEY);
      return raw ? (JSON.parse(raw) as DraftData) : null;
    } catch {
      return null;
    }
  }, []);

  const clearDraft = useCallback(() => {
    try {
      localStorage.removeItem(DRAFT_KEY);
    } catch {
      // ignore
    }
  }, []);

  const hasDraft = useCallback((): boolean => {
    try {
      return localStorage.getItem(DRAFT_KEY) !== null;
    } catch {
      return false;
    }
  }, []);

  return { saveDraft, loadDraft, clearDraft, hasDraft };
}
