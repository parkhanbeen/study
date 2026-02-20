import { FeedList } from "@/components/post/feed-list";
import { HotPostsSection } from "@/components/post/hot-posts-section";
import { serverFetch } from "@/lib/api/client";
import { PageResponse, PostDetailResponse } from "@/types/api";

export default async function FeedPage() {
  let initialData: PageResponse<PostDetailResponse> | undefined;

  try {
    initialData = await serverFetch<PageResponse<PostDetailResponse>>("/feed?page=0&size=20");
  } catch {
    // Backend unavailable — FeedList renders empty state
  }

  return (
    <div>
      <HotPostsSection />
      <FeedList initialData={initialData} />
    </div>
  );
}
