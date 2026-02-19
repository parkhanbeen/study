import { FeedList } from "@/components/post/feed-list";
import { serverFetch } from "@/lib/api/client";
import { PageResponse, PostResponse } from "@/types/api";

export default async function FeedPage() {
  let initialData: PageResponse<PostResponse> | undefined;

  try {
    initialData = await serverFetch<PageResponse<PostResponse>>("/feed?page=0&size=20");
  } catch {
    // Backend unavailable — FeedList renders empty state
  }

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold">Feed</h1>
      <FeedList initialData={initialData} />
    </div>
  );
}
