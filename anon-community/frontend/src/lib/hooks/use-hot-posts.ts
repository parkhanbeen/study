import { useQuery } from "@tanstack/react-query";
import { feedApi } from "@/lib/api/feed";

export function useHotPosts(limit = 5) {
  return useQuery({
    queryKey: ["hot-posts", limit],
    queryFn: () => feedApi.getHot(limit),
    staleTime: 5 * 60 * 1000, // 5-minute cache — hot ranking doesn't change instantly
  });
}
