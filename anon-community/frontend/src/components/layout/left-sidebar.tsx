"use client";

import Link from "next/link";
import { usePathname, useSearchParams, useRouter } from "next/navigation";
import { PenSquare } from "lucide-react";
import { Button } from "@/components/ui/button";
import { POST_CATEGORIES, PostCategory } from "@/types/api";
import { useAuthStore } from "@/stores/auth-store";
import { cn } from "@/lib/utils";

const CATEGORY_EMOJI: Record<string, string> = {
  DAILY:       "☀️",
  ADVICE:      "💬",
  HUMOR:       "😂",
  INFORMATION: "📚",
  QUESTION:    "❓",
  DISCUSSION:  "🗣️",
  MEDIA:       "🎬",
  FEEDBACK:    "📝",
};

export function LeftSidebar() {
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const router = useRouter();
  const { isLoggedIn, isInitialized, activeProfile } = useAuthStore();

  const currentCategory = pathname === "/" ? searchParams.get("category") : null;

  const handleCategoryClick = (value: PostCategory | null) => {
    if (value) {
      router.push(`/?category=${value}`);
    } else {
      router.push("/");
    }
  };

  return (
    <nav className="sticky top-20 space-y-1">
      {/* Active profile card */}
      {isInitialized && isLoggedIn && activeProfile && (
        <Link href={`/profiles/${activeProfile.id}`}>
          <div className="p-3 rounded-lg border hover:bg-muted/50">
            <p className="font-medium text-sm">{activeProfile.nickname}</p>
            <p className="text-xs text-muted-foreground">게시글 {activeProfile.postCount}개</p>
          </div>
        </Link>
      )}

      {/* Write button */}
      {isInitialized && isLoggedIn && (
        <Button asChild className="w-full mb-3" size="sm">
          <Link href="/posts/new">
            <PenSquare className="mr-2 h-4 w-4" />
            글쓰기
          </Link>
        </Button>
      )}

      {/* All posts */}
      <button
        onClick={() => handleCategoryClick(null)}
        aria-current={!currentCategory ? "page" : undefined}
        className={cn(
          "w-full flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium transition-colors text-left",
          !currentCategory
            ? "bg-primary/10 text-primary"
            : "text-muted-foreground hover:bg-muted hover:text-foreground",
        )}
      >
        🏠 전체
      </button>

      {/* Categories */}
      {POST_CATEGORIES.map((cat) => (
        <button
          key={cat.value}
          onClick={() => handleCategoryClick(cat.value)}
          aria-current={currentCategory === cat.value ? "page" : undefined}
          className={cn(
            "w-full flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium transition-colors text-left",
            currentCategory === cat.value
              ? "bg-primary/10 text-primary"
              : "text-muted-foreground hover:bg-muted hover:text-foreground",
          )}
        >
          {CATEGORY_EMOJI[cat.value]} {cat.label}
        </button>
      ))}

      {/* Nav links */}
      <div className="pt-3 border-t mt-3 space-y-1">
        {[
          { href: "/search",   label: "🔍 검색" },
          { href: "/my",       label: "👤 마이페이지" },
          { href: "/settings", label: "⚙️ 설정" },
        ].map(({ href, label }) => (
          <Link
            key={href}
            href={href}
            aria-current={pathname === href ? "page" : undefined}
            className={cn(
              "flex items-center rounded-md px-3 py-2 text-sm font-medium transition-colors",
              pathname === href
                ? "bg-primary/10 text-primary"
                : "text-muted-foreground hover:bg-muted hover:text-foreground",
            )}
          >
            {label}
          </Link>
        ))}
      </div>
    </nav>
  );
}
