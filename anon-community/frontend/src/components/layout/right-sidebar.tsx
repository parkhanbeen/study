"use client";

import Link from "next/link";
import { HotPostsSection } from "@/components/post/hot-posts-section";
import { useHotTags } from "@/lib/hooks/use-search";

export function RightSidebar() {
  const { data: hotTagsData } = useHotTags(10);
  const hotTags = hotTagsData?.tags ?? [];

  return (
    <div className="sticky top-20 space-y-6">
      {/* Hot posts */}
      <HotPostsSection />

      {/* Hot tags */}
      {hotTags.length > 0 && (
        <div>
          <p className="mb-2 text-sm font-semibold">🏷️ 인기 태그</p>
          <div className="flex flex-wrap gap-1.5">
            {hotTags.map((tag) => (
              <Link
                key={tag}
                href={`/search?tags=${encodeURIComponent(tag)}`}
                aria-label={`${tag} 태그 검색`}
                className="inline-flex items-center rounded-full border border-border px-2.5 py-0.5 text-xs text-muted-foreground hover:border-primary hover:text-primary transition-colors"
              >
                #{tag}
              </Link>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
