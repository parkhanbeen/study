import { useInfiniteQuery, useQuery } from "@tanstack/react-query";
import { searchApi } from "@/lib/api/search";
import { PostCategory, SearchSortBy, SearchDateRange } from "@/types/api";

export function useSearchPosts(
  keyword: string,
  tags: string[],
  category?: PostCategory,
  sortBy: SearchSortBy = "createdAt",
  dateRange?: SearchDateRange,
) {
  return useInfiniteQuery({
    queryKey: ["search", keyword, tags, category, sortBy, dateRange],
    queryFn: ({ pageParam }) =>
      searchApi.postsCursor({
        keyword: keyword || undefined,
        tags: tags.length > 0 ? tags : undefined,
        category: category || undefined,
        size: 20,
        searchAfter: pageParam ?? undefined,
        sortBy,
        dateRange,
      }),
    initialPageParam: null as string | null,
    getNextPageParam: (lastPage) => (lastPage.hasNext ? lastPage.searchAfter : undefined),
    enabled: keyword.length > 0 || tags.length > 0 || !!category,
  });
}

export function useHotTags(limit = 10) {
  return useQuery({
    queryKey: ["hot-tags", limit],
    queryFn: () => searchApi.hotTags(limit),
    staleTime: 60 * 60 * 1000,  // 1h — matches Redis TTL on the backend
    gcTime: 65 * 60 * 1000,
  });
}
