"use client";

import { Suspense, useCallback, useEffect, useRef, useState } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import { X } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { PostCard } from "@/components/post/post-card";
import { PostCardSkeleton } from "@/components/post/post-card-skeleton";
import { SearchInput } from "@/components/search/search-input";
import { useSearchPosts, useHotTags } from "@/lib/hooks/use-search";
import { PostCategory, POST_CATEGORIES, SearchSortBy, SearchDateRange, PostSearchHitResponse } from "@/types/api";

function SearchContent() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const [keyword, setKeyword] = useState(searchParams.get("keyword") ?? "");
  const [tags, setTags] = useState<string[]>(searchParams.getAll("tags"));
  const [category, setCategory] = useState<PostCategory | undefined>(
    (searchParams.get("category") as PostCategory) || undefined,
  );
  const [sortBy, setSortBy] = useState<SearchSortBy>(
    (searchParams.get("sortBy") as SearchSortBy) ?? "createdAt",
  );
  const [dateRange, setDateRange] = useState<SearchDateRange | undefined>(
    (searchParams.get("dateRange") as SearchDateRange) || undefined,
  );

  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading } = useSearchPosts(
    keyword,
    tags,
    category,
    sortBy,
    dateRange,
  );
  const { data: hotTagsData } = useHotTags(10);
  const hotTags = hotTagsData?.tags ?? [];

  const observerRef = useRef<HTMLDivElement>(null);

  // Sync state to URL
  useEffect(() => {
    const params = new URLSearchParams();
    if (keyword) params.set("keyword", keyword);
    tags.forEach((t) => params.append("tags", t));
    if (category) params.set("category", category);
    if (sortBy !== "createdAt") params.set("sortBy", sortBy);
    if (dateRange) params.set("dateRange", dateRange);
    router.replace(`/search?${params.toString()}`, { scroll: false });
  }, [keyword, tags, category, sortBy, dateRange, router]);

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

  const removeTag = useCallback((tag: string) => {
    setTags((prev) => prev.filter((t) => t !== tag));
  }, []);

  const posts = data?.pages.flatMap((p) => p.content) ?? [];
  const hasQuery = keyword.length > 0 || tags.length > 0 || !!category;

  return (
    <div className="space-y-4">
      <SearchInput value={keyword} onChange={setKeyword} />

      {/* Category filter tabs */}
      <div className="flex flex-wrap gap-1.5">
        <button
          onClick={() => setCategory(undefined)}
          className={`rounded-full px-3 py-1 text-xs font-medium transition-colors ${
            !category
              ? "bg-primary text-primary-foreground"
              : "bg-muted text-muted-foreground hover:bg-muted/80"
          }`}
        >
          전체
        </button>
        {POST_CATEGORIES.map((cat) => (
          <button
            key={cat.value}
            onClick={() => setCategory(category === cat.value ? undefined : cat.value)}
            className={`rounded-full px-3 py-1 text-xs font-medium transition-colors ${
              category === cat.value
                ? "bg-primary text-primary-foreground"
                : "bg-muted text-muted-foreground hover:bg-muted/80"
            }`}
          >
            {cat.label}
          </button>
        ))}
      </div>

      {tags.length > 0 && (
        <div className="flex flex-wrap items-center gap-2">
          <span className="text-xs text-muted-foreground">태그:</span>
          {tags.map((tag) => (
            <Badge
              key={tag}
              variant="default"
              className="gap-1 pl-2 pr-1"
            >
              #{tag}
              <button
                onClick={() => removeTag(tag)}
                className="ml-0.5 rounded-full p-0.5 hover:bg-primary-foreground/20"
              >
                <X className="h-3 w-3" />
              </button>
            </Badge>
          ))}
          <button
            onClick={() => setTags([])}
            className="text-xs text-muted-foreground hover:text-foreground transition-colors"
          >
            전체 삭제
          </button>
        </div>
      )}

      {/* Sort / date-range dropdowns — shown only when there is an active query */}
      {hasQuery && (
        <div className="flex flex-wrap items-center gap-2 mb-4">
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value as SearchSortBy)}
            className="rounded-md border border-input bg-background px-3 py-1.5 text-sm"
          >
            <option value="createdAt">최신순</option>
            <option value="likeCount">좋아요순</option>
            <option value="commentCount">댓글순</option>
            <option value="viewCount">조회순</option>
            <option value="relevance">관련도순</option>
          </select>

          <select
            value={dateRange ?? ""}
            onChange={(e) => setDateRange((e.target.value as SearchDateRange) || undefined)}
            className="rounded-md border border-input bg-background px-3 py-1.5 text-sm"
          >
            <option value="">전체 기간</option>
            <option value="today">오늘</option>
            <option value="this_week">이번 주</option>
            <option value="this_month">이번 달</option>
          </select>
        </div>
      )}

      {isLoading && (
        <div className="space-y-4">
          <PostCardSkeleton />
          <PostCardSkeleton />
          <PostCardSkeleton />
        </div>
      )}

      {!isLoading && posts.length === 0 && hasQuery && (
        <div className="flex flex-col items-center py-16 text-center">
          <span className="text-5xl mb-4">🔍</span>
          <p className="text-lg font-medium text-muted-foreground">검색 결과가 없습니다</p>
          <p className="mt-1 text-sm text-muted-foreground">
            {keyword ? `"${keyword}"에 대한 검색 결과가 없습니다` : `선택한 필터에 해당하는 게시글이 없습니다`}
          </p>
        </div>
      )}

      {/* Empty state — shown before any query is entered */}
      {!isLoading && !hasQuery && (
        <div className="py-8 space-y-6">
          {hotTags.length > 0 && (
            <div className="space-y-2">
              <p className="text-sm font-medium text-muted-foreground">인기 태그</p>
              <div className="flex flex-wrap gap-2">
                {hotTags.map((tag) => (
                  <button
                    key={tag}
                    onClick={() => setTags((prev) => prev.includes(tag) ? prev : [...prev, tag])}
                    className="rounded-full border border-border px-3 py-1 text-sm text-muted-foreground hover:border-primary hover:text-primary transition-colors"
                  >
                    #{tag}
                  </button>
                ))}
              </div>
            </div>
          )}
          <div className="text-center py-8">
            <p className="text-lg font-medium text-muted-foreground">게시글을 검색해보세요</p>
            <p className="mt-1 text-sm text-muted-foreground">
              키워드, 태그 또는 카테고리로 검색할 수 있습니다
            </p>
          </div>
        </div>
      )}

      {posts.map((post) => (
        <PostCard key={post.id} post={post} highlights={post.highlights} />
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
