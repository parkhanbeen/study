import { Suspense } from "react";
import { FeedList } from "@/components/post/feed-list";
import { serverFetch } from "@/lib/api/client";
import { PageResponse, PostDetailResponse } from "@/types/api";

function FeedSkeleton() {
  return (
    <div className="space-y-4 pt-4">
      {[1, 2, 3].map((i) => (
        <div key={i} className="h-36 animate-pulse rounded-lg bg-muted" />
      ))}
    </div>
  );
}

export default async function FeedPage() {
  let initialData: PageResponse<PostDetailResponse> | undefined;

  try {
    initialData = await serverFetch<PageResponse<PostDetailResponse>>("/feed?page=0&size=20");
  } catch {
    // Backend unavailable — FeedList renders empty state
  }

  return (
    <Suspense fallback={<FeedSkeleton />}>
      <FeedList initialData={initialData} />
    </Suspense>
  );
}
