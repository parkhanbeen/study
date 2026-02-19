"use client";

import { Suspense, useEffect, useRef, useState } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import { PostCard } from "@/components/post/post-card";
import { PostCardSkeleton } from "@/components/post/post-card-skeleton";
import { SearchInput } from "@/components/search/search-input";
import { useSearchPosts } from "@/lib/hooks/use-search";

function SearchContent() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const [keyword, setKeyword] = useState(searchParams.get("keyword") ?? "");
  const [tags] = useState<string[]>(searchParams.getAll("tags"));
  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading } = useSearchPosts(keyword, tags);
  const observerRef = useRef<HTMLDivElement>(null);

  // Sync keyword to URL
  useEffect(() => {
    const params = new URLSearchParams();
    if (keyword) params.set("keyword", keyword);
    tags.forEach((t) => params.append("tags", t));
    router.replace(`/search?${params.toString()}`);
  }, [keyword, tags, router]);

  // Infinite scroll
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
      <SearchInput value={keyword} onChange={setKeyword} />

      {isLoading && (
        <div className="space-y-4">
          <PostCardSkeleton />
          <PostCardSkeleton />
          <PostCardSkeleton />
        </div>
      )}

      {!isLoading && posts.length === 0 && keyword && (
        <p className="py-12 text-center text-muted-foreground">No results found for &ldquo;{keyword}&rdquo;</p>
      )}

      {posts.map((post) => (
        <PostCard key={post.id} post={post} />
      ))}

      {isFetchingNextPage && <PostCardSkeleton />}

      <div ref={observerRef} className="h-1" />
    </div>
  );
}

export default function SearchPage() {
  return (
    <Suspense
      fallback={
        <div className="space-y-4">
          <PostCardSkeleton />
          <PostCardSkeleton />
          <PostCardSkeleton />
        </div>
      }
    >
      <SearchContent />
    </Suspense>
  );
}
