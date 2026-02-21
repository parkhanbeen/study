import Link from "next/link";
import { serverFetch } from "@/lib/api/client";
import { PageResponse, PostDetailResponse, PostCategory, POST_CATEGORIES } from "@/types/api";
import { timeAgo } from "@/lib/utils";
import { Heart, MessageCircle } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";

interface RelatedPostsSectionProps {
  currentPostId: number;
  category: PostCategory;
}

function getCategoryLabel(category: string): string {
  return POST_CATEGORIES.find((c) => c.value === category)?.label ?? category;
}

export async function RelatedPostsSection({ currentPostId, category }: RelatedPostsSectionProps) {
  let posts: PostDetailResponse[] = [];

  try {
    const data = await serverFetch<PageResponse<PostDetailResponse>>(
      `/feed?category=${category}&page=0&size=6&sortBy=createdAt&sortDirection=DESC`
    );
    posts = data.content.filter((p) => p.id !== currentPostId).slice(0, 5);
  } catch {
    // 백엔드 unavailable — 빈 상태로 graceful fallback
    return null;
  }

  if (posts.length === 0) return null;

  return (
    <>
      <Separator className="my-6" />
      <section>
        <h2 className="mb-3 text-sm font-semibold text-muted-foreground">
          같은 카테고리의 다른 글 ({getCategoryLabel(category)})
        </h2>
        <div className="space-y-2">
          {posts.map((post) => (
            <Link key={post.id} href={`/posts/${post.id}`}>
              <Card className="transition-colors hover:border-primary/30 hover:bg-muted/30">
                <CardContent className="flex items-center gap-3 p-3">
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-medium">{post.title}</p>
                    <div className="mt-0.5 flex items-center gap-3 text-xs text-muted-foreground">
                      <span>{timeAgo(post.createdAt)}</span>
                      <span className="flex items-center gap-1">
                        <Heart className="h-3 w-3" /> {post.likeCount}
                      </span>
                      <span className="flex items-center gap-1">
                        <MessageCircle className="h-3 w-3" /> {post.commentCount}
                      </span>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>
      </section>
    </>
  );
}
