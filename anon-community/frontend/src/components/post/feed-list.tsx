"use client";

import { useEffect, useRef } from "react";
import { PostCard } from "./post-card";
import { PostCardSkeleton } from "./post-card-skeleton";
import { useFeed } from "@/lib/hooks/use-feed";
import { PageResponse, PostResponse } from "@/types/api";

interface FeedListProps {
  initialData?: PageResponse<PostResponse>;
}

export function FeedList({ initialData }: FeedListProps) {
  const { data, fetchNextPage, hasNextPage, isFetchingNextPage } = useFeed({ initialData });
  const observerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasNextPage && !isFetchingNextPage) {
          fetchNextPage();
        }
      },
      { threshold: 0.1 },
    );
    const el = observerRef.current;
    if (el) observer.observe(el);
    return () => { if (el) observer.unobserve(el); };
  }, [fetchNextPage, hasNextPage, isFetchingNextPage]);

  const posts = data?.pages.flatMap((p) => p.content) ?? [];

  return (
    <div className="space-y-4">
      {posts.map((post) => (
        <PostCard key={post.id} post={post} />
      ))}

      {isFetchingNextPage && (
        <div className="space-y-4">
          <PostCardSkeleton />
          <PostCardSkeleton />
        </div>
      )}

      {!hasNextPage && posts.length > 0 && (
        <p className="py-8 text-center text-sm text-muted-foreground">
          All posts loaded
        </p>
      )}

      {posts.length === 0 && (
        <p className="py-12 text-center text-muted-foreground">No posts yet</p>
      )}

      <div ref={observerRef} className="h-1" />
    </div>
  );
}
