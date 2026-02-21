"use client";

import { useState } from "react";
import { cn } from "@/lib/utils";
import { FeedList } from "./feed-list";
import { FollowingFeedList } from "./following-feed-list";
import { PageResponse, PostDetailResponse } from "@/types/api";
import { useAuthStore } from "@/stores/auth-store";

interface FeedTabsProps {
  initialData?: PageResponse<PostDetailResponse>;
}

export function FeedTabs({ initialData }: FeedTabsProps) {
  const [tab, setTab] = useState<"all" | "following">("all");
  const { isLoggedIn, isInitialized } = useAuthStore();

  return (
    <div>
      {isInitialized && isLoggedIn && (
        <div className="mb-4 flex border-b">
          <button
            onClick={() => setTab("all")}
            className={cn(
              "px-4 py-2 text-sm font-medium transition-colors",
              tab === "all"
                ? "border-b-2 border-primary text-primary"
                : "text-muted-foreground hover:text-foreground",
            )}
          >
            전체
          </button>
          <button
            onClick={() => setTab("following")}
            className={cn(
              "px-4 py-2 text-sm font-medium transition-colors",
              tab === "following"
                ? "border-b-2 border-primary text-primary"
                : "text-muted-foreground hover:text-foreground",
            )}
          >
            팔로잉
          </button>
        </div>
      )}
      {tab === "all" ? (
        <FeedList initialData={initialData} />
      ) : (
        <FollowingFeedList />
      )}
    </div>
  );
}
