"use client";

import { useEffect, useRef } from "react";
import { useInfiniteQuery } from "@tanstack/react-query";
import Link from "next/link";
import { PostCard } from "./post-card";
import { PostCardSkeleton } from "./post-card-skeleton";
import { feedApi } from "@/lib/api/feed";
import { Button } from "@/components/ui/button";
import { useAuthStore } from "@/stores/auth-store";

export function FollowingFeedList() {
  const { isLoggedIn } = useAuthStore();
  const observerRef = useRef<HTMLDivElement>(null);

  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading } =
    useInfiniteQuery({
      queryKey: ["following-feed"],
      queryFn: ({ pageParam }) =>
        feedApi.getFollowing(pageParam as string | undefined),
      initialPageParam: undefined as string | undefined,
      getNextPageParam: (lastPage) =>
        lastPage.hasNext ? (lastPage.searchAfter ?? undefined) : undefined,
      enabled: isLoggedIn,
    });

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        if (
          entries[0].isIntersecting &&
          hasNextPage &&
          !isFetchingNextPage
        ) {
          fetchNextPage();
        }
      },
      { threshold: 0.1 },
    );
    const el = observerRef.current;
    if (el) observer.observe(el);
    return () => {
      if (el) observer.unobserve(el);
    };
  }, [fetchNextPage, hasNextPage, isFetchingNextPage]);

  const posts = data?.pages.flatMap((p) => p.content) ?? [];

  if (!isLoggedIn) {
    return (
      <div className="flex flex-col items-center justify-center py-20 text-center">
        <div className="mb-4 flex h-20 w-20 items-center justify-center rounded-full bg-muted">
          <span className="text-4xl">👥</span>
        </div>
        <h3 className="text-lg font-semibold">팔로잉 피드</h3>
        <p className="mt-1 text-sm text-muted-foreground">
          로그인하면 팔로우한 사람들의 게시글을 볼 수 있어요
        </p>
        <Button asChild className="mt-4" size="sm">
          <Link href="/login">로그인</Link>
        </Button>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="space-y-4">
        <PostCardSkeleton />
        <PostCardSkeleton />
        <PostCardSkeleton />
      </div>
    );
  }

  if (posts.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-20 text-center">
        <div className="mb-4 flex h-20 w-20 items-center justify-center rounded-full bg-muted">
          <span className="text-4xl">👥</span>
        </div>
        <h3 className="text-lg font-semibold">팔로잉한 사람이 없습니다</h3>
        <p className="mt-1 text-sm text-muted-foreground">
          관심 있는 사람을 팔로우해보세요!
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {posts.map((post) => (
        <PostCard key={post.id} post={post} />
      ))}
      {isFetchingNextPage && (
        <div className="flex flex-col items-center gap-3 py-4">
          <PostCardSkeleton />
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <div className="h-4 w-4 animate-spin rounded-full border-2 border-primary border-t-transparent" />
            <span>더 불러오는 중...</span>
          </div>
        </div>
      )}
      {!hasNextPage && posts.length > 0 && (
        <p className="py-8 text-center text-xs text-muted-foreground">
          총 {posts.length}개 게시글 · 모두 불러왔습니다
        </p>
      )}
      <div ref={observerRef} className="h-1" />
    </div>
  );
}
