import { useInfiniteQuery } from "@tanstack/react-query";
import { searchApi } from "@/lib/api/search";

export function useSearchPosts(keyword: string, tags: string[]) {
  return useInfiniteQuery({
    queryKey: ["search", keyword, tags],
    queryFn: ({ pageParam }) =>
      searchApi.postsCursor({
        keyword: keyword || undefined,
        tags: tags.length > 0 ? tags : undefined,
        size: 20,
        searchAfter: pageParam ?? undefined,
      }),
    initialPageParam: null as string | null,
    getNextPageParam: (lastPage) => (lastPage.hasNext ? lastPage.searchAfter : undefined),
    enabled: keyword.length > 0 || tags.length > 0,
  });
}
