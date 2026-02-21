"use client";

import { PenSquare } from "lucide-react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { useAuthStore } from "@/stores/auth-store";

export function WriteFab() {
  const { isLoggedIn } = useAuthStore();

  if (!isLoggedIn) return null;

  return (
    <Button
      className="fixed bottom-6 right-6 z-50 h-14 rounded-full px-5 shadow-lg transition-all hover:shadow-xl gap-2"
      asChild
    >
      <Link href="/posts/new">
        <PenSquare className="h-5 w-5" />
        <span>글쓰기</span>
      </Link>
    </Button>
  );
}
