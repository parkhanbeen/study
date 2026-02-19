import { useInfiniteQuery } from "@tanstack/react-query";
import { feedApi } from "@/lib/api/feed";
import { PageResponse, PostResponse } from "@/types/api";

interface UseFeedOptions {
  initialData?: PageResponse<PostResponse>;
}

export function useFeed({ initialData }: UseFeedOptions = {}) {
  return useInfiniteQuery({
    queryKey: ["feed"],
    queryFn: ({ pageParam = 0 }) => feedApi.get(pageParam, 20),
    initialPageParam: 0,
    getNextPageParam: (lastPage) =>
      lastPage.page + 1 < lastPage.totalPages ? lastPage.page + 1 : undefined,
    ...(initialData && {
      initialData: {
        pages: [initialData],
        pageParams: [0],
      },
    }),
  });
}
