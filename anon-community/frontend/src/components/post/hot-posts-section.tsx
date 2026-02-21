"use client";

import { Flame } from "lucide-react";
import Link from "next/link";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { useHotPosts } from "@/lib/hooks/use-hot-posts";
import { timeAgo } from "@/lib/utils";
import { POST_CATEGORIES } from "@/types/api";

function getCategoryLabel(category: string): string {
  return POST_CATEGORIES.find((c) => c.value === category)?.label ?? category;
}

export function HotPostsSection() {
  const { data, isLoading } = useHotPosts(5);

  return (
    <div className="mb-4">
      <div className="mb-2 flex items-center gap-1.5">
        <Flame className="h-4 w-4 text-orange-500" />
        <span className="text-sm font-semibold">지금 핫한 글</span>
      </div>

      {isLoading && (
        <div className="space-y-1.5">
          {[...Array(3)].map((_, i) => (
            <Skeleton key={i} className="h-14 w-full rounded-lg" />
          ))}
        </div>
      )}

      {!isLoading && !data?.content.length && null}

      {!isLoading && !!data?.content.length && (
        <div className="space-y-1.5">
          {data.content.map((post, idx) => (
            <Link key={post.id} href={`/posts/${post.id}`}>
              <Card className="transition-colors hover:border-orange-300">
                <CardContent className="flex items-center gap-3 p-3">
                  <span className="w-5 text-center text-xl font-black tabular-nums text-orange-400/50">
                    {idx + 1}
                  </span>
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-medium">
                      {post.title}
                      <span className="text-xs text-muted-foreground ml-1">{getCategoryLabel(post.category)}</span>
                    </p>
                    <p className="text-xs text-muted-foreground">
                      ♥ {post.likeCount} · {timeAgo(post.createdAt)}
                    </p>
                  </div>
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
