"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useAuthStore } from "@/stores/auth-store";
import { memberApi } from "@/lib/api/member";
import { PostDetailResponse, PageResponse } from "@/types/api";
import { PostCard } from "@/components/post/post-card";
import { Button } from "@/components/ui/button";

type Tab = "posts" | "liked";

export default function MyPage() {
  const router = useRouter();
  const { isLoggedIn, isInitialized, activeProfile } = useAuthStore();
  const [tab, setTab] = useState<Tab>("posts");
  const [posts, setPosts] = useState<PostDetailResponse[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!isInitialized) return;
    if (!isLoggedIn) {
      router.replace("/login?callbackUrl=/my");
    }
  }, [isInitialized, isLoggedIn, router]);

  useEffect(() => {
    if (!isLoggedIn) return;
    let isCancelled = false;
    setLoading(true);
    const fetchFn = tab === "posts" ? memberApi.myPosts(page) : memberApi.myLikedPosts(page);
    fetchFn
      .then((data: PageResponse<PostDetailResponse>) => {
        if (!isCancelled) {
          setPosts(data.content);
          setTotalPages(data.totalPages);
        }
      })
      .catch(() => { if (!isCancelled) setPosts([]); })
      .finally(() => { if (!isCancelled) setLoading(false); });
    return () => { isCancelled = true; };
  }, [tab, page, isLoggedIn]);

  const handleTabChange = (newTab: Tab) => {
    setTab(newTab);
    setPage(0);
    setPosts([]);
  };

  if (!isInitialized || !isLoggedIn) return null;

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">마이페이지</h1>

      {activeProfile && (
        <Link href={`/profiles/${activeProfile.id}`}>
          <div className="p-4 rounded-lg border hover:bg-muted/50">
            <p className="font-semibold text-base">{activeProfile.nickname}</p>
            <div className="flex gap-4 mt-1 text-sm text-muted-foreground">
              <span>게시글 {activeProfile.postCount}개</span>
              <span>팔로워 {activeProfile.followerCount}명</span>
              <span>팔로잉 {activeProfile.followingCount}명</span>
            </div>
          </div>
        </Link>
      )}

      <div className="flex gap-2 border-b">
        <button
          onClick={() => handleTabChange("posts")}
          className={`pb-2 px-1 text-sm font-medium border-b-2 transition-colors ${
            tab === "posts"
              ? "border-primary text-primary"
              : "border-transparent text-muted-foreground hover:text-foreground"
          }`}
        >
          내가 쓴 글
        </button>
        <button
          onClick={() => handleTabChange("liked")}
          className={`pb-2 px-1 text-sm font-medium border-b-2 transition-colors ${
            tab === "liked"
              ? "border-primary text-primary"
              : "border-transparent text-muted-foreground hover:text-foreground"
          }`}
        >
          좋아요한 글
        </button>
      </div>

      {loading && <p className="text-sm text-muted-foreground">불러오는 중...</p>}

      {!loading && posts.length === 0 && (
        <p className="text-sm text-muted-foreground py-8 text-center">
          {tab === "posts" ? "작성한 글이 없습니다" : "좋아요한 글이 없습니다"}
        </p>
      )}

      <div className="space-y-3">
        {posts.map((post) => (
          <PostCard key={post.id} post={post} />
        ))}
      </div>

      {totalPages > 1 && (
        <div className="flex justify-center gap-2 pt-4">
          <Button
            variant="outline"
            size="sm"
            disabled={page === 0}
            onClick={() => setPage((p) => p - 1)}
          >
            이전
          </Button>
          <span className="text-sm self-center">{page + 1} / {totalPages}</span>
          <Button
            variant="outline"
            size="sm"
            disabled={page >= totalPages - 1}
            onClick={() => setPage((p) => p + 1)}
          >
            다음
          </Button>
        </div>
      )}
    </div>
  );
}
