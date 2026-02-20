"use client";

import { useEffect, useRef, useState } from "react";
import { PenSquare, LogIn } from "lucide-react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { PostCard } from "./post-card";
import { PostCardSkeleton } from "./post-card-skeleton";
import { useFeed } from "@/lib/hooks/use-feed";
import { FeedSort } from "@/lib/api/feed";
import { PageResponse, PostDetailResponse, PostCategory, POST_CATEGORIES } from "@/types/api";
import { useAuthStore } from "@/stores/auth-store";
import { cn } from "@/lib/utils";

interface FeedListProps {
  initialData?: PageResponse<PostDetailResponse>;
}

const CATEGORY_TABS: { value: PostCategory | undefined; label: string }[] = [
  { value: undefined, label: "전체" },
  ...POST_CATEGORIES,
];

export function FeedList({ initialData }: FeedListProps) {
  const [selectedCategory, setSelectedCategory] = useState<PostCategory | undefined>(undefined);
  const [selectedSort, setSelectedSort] = useState<FeedSort>("latest");
  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading } = useFeed({
    category: selectedCategory,
    sort: selectedSort,
    initialData,
  });
  const { isLoggedIn } = useAuthStore();
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
      <div className="sticky top-14 z-40 -mx-4 bg-background px-4 pb-3 pt-1">
        <div className="flex items-center justify-between">
          <div className="flex gap-1.5 overflow-x-auto pb-1 scrollbar-hide">
            {CATEGORY_TABS.map((tab) => (
              <button
                key={tab.value ?? "ALL"}
                onClick={() => setSelectedCategory(tab.value)}
                className={cn(
                  "shrink-0 rounded-full px-3.5 py-1.5 text-sm font-medium transition-colors",
                  selectedCategory === tab.value
                    ? "bg-primary text-primary-foreground"
                    : "bg-muted text-muted-foreground hover:bg-muted/80",
                )}
              >
                {tab.label}
              </button>
            ))}
          </div>
          <select
            value={selectedSort}
            onChange={(e) => setSelectedSort(e.target.value as FeedSort)}
            className="shrink-0 rounded-md border bg-background px-2 py-1 text-xs text-muted-foreground"
          >
            <option value="latest">최신순</option>
            <option value="oldest">오래된순</option>
          </select>
        </div>
      </div>

      {!isLoggedIn && posts.length > 0 && (
        <div className="rounded-lg border bg-muted/50 p-4 text-center">
          <p className="text-sm text-muted-foreground">익명 커뮤니티에 가입하고 글을 작성해보세요</p>
          <div className="mt-2 flex items-center justify-center gap-2">
            <Button size="sm" variant="outline" asChild>
              <Link href="/login">
                <LogIn className="mr-1.5 h-3.5 w-3.5" />
                로그인
              </Link>
            </Button>
            <Button size="sm" asChild>
              <Link href="/signup">가입하기</Link>
            </Button>
          </div>
        </div>
      )}

      {posts.map((post) => (
        <PostCard key={post.id} post={post} />
      ))}

      {isFetchingNextPage && (
        <div className="flex flex-col items-center gap-3 py-4">
          <div className="space-y-3 w-full">
            <PostCardSkeleton />
            <PostCardSkeleton />
          </div>
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

      {isLoading && (
        <div className="space-y-4">
          <PostCardSkeleton />
          <PostCardSkeleton />
          <PostCardSkeleton />
        </div>
      )}

      {!isLoading && posts.length === 0 && (
        <div className="py-16 text-center">
          <p className="text-lg font-medium text-muted-foreground">
            {selectedCategory ? "이 카테고리에 게시글이 없습니다" : "아직 게시글이 없습니다"}
          </p>
          <p className="mt-1 text-sm text-muted-foreground">
            {selectedCategory ? "다른 카테고리를 둘러보거나 첫 글을 작성해보세요!" : "첫 번째로 무언가를 공유해보세요!"}
          </p>
          {isLoggedIn ? (
            <Button size="sm" className="mt-4" asChild>
              <Link href="/posts/new">
                <PenSquare className="mr-1.5 h-4 w-4" />
                글 쓰기
              </Link>
            </Button>
          ) : (
            <Button size="sm" className="mt-4" asChild>
              <Link href="/signup">가입하고 글 쓰기</Link>
            </Button>
          )}
        </div>
      )}

      <div ref={observerRef} className="h-1" />
    </div>
  );
}
