import Link from "next/link";
import { Heart, MessageCircle, Eye } from "lucide-react";
import { Card, CardContent, CardFooter, CardHeader } from "@/components/ui/card";
import { PostResponse, PostDetailResponse, POST_CATEGORIES } from "@/types/api";
import { timeAgo } from "@/lib/utils";
import { ClickableTag } from "./clickable-tag";

interface PostCardProps {
  post: PostResponse | PostDetailResponse;
}

function hasStats(post: PostResponse | PostDetailResponse): post is PostDetailResponse {
  return "likeCount" in post;
}

function getCategoryLabel(category: string): string {
  return POST_CATEGORIES.find((c) => c.value === category)?.label ?? category;
}

const CATEGORY_STYLES: Record<string, string> = {
  DAILY:       "bg-sky-100     text-sky-700     dark:bg-sky-900/30     dark:text-sky-400",
  ADVICE:      "bg-purple-100  text-purple-700  dark:bg-purple-900/30  dark:text-purple-400",
  HUMOR:       "bg-yellow-100  text-yellow-700  dark:bg-yellow-900/30  dark:text-yellow-400",
  INFORMATION: "bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400",
  QUESTION:    "bg-orange-100  text-orange-700  dark:bg-orange-900/30  dark:text-orange-400",
  DISCUSSION:  "bg-red-100     text-red-700     dark:bg-red-900/30     dark:text-red-400",
  MEDIA:       "bg-pink-100    text-pink-700    dark:bg-pink-900/30    dark:text-pink-400",
  FEEDBACK:    "bg-indigo-100  text-indigo-700  dark:bg-indigo-900/30  dark:text-indigo-400",
};

function getCategoryStyle(category: string): string {
  return CATEGORY_STYLES[category] ?? "bg-muted text-muted-foreground";
}

export function PostCard({ post }: PostCardProps) {
  return (
    <Link href={`/posts/${post.id}`}>
      <Card className="group transition-all hover:border-primary/30 hover:shadow-md">
        <CardHeader className="pb-2">
          <div className="flex items-center gap-2 text-xs text-muted-foreground">
            <span className="inline-flex h-5 w-5 items-center justify-center rounded-full bg-primary/10 text-[10px] font-bold text-primary">
              {String(post.authorId).slice(-1)}
            </span>
            <span>Anonymous #{post.authorId}</span>
            <span>&middot;</span>
            <span>{timeAgo(post.createdAt)}</span>
            <span className="ml-auto">
              <span
                className={`inline-flex items-center rounded-md px-1.5 py-0.5 text-[10px] font-semibold ${getCategoryStyle(post.category)}`}
              >
                {getCategoryLabel(post.category)}
              </span>
            </span>
          </div>
          <h3 className="text-base font-semibold leading-snug transition-colors group-hover:text-primary">
            {post.title}
          </h3>
        </CardHeader>
        <CardContent className="pb-2">
          <p className="line-clamp-2 text-sm text-muted-foreground">{post.content}</p>
          {post.tags.length > 0 && (
            <div className="mt-2 flex flex-wrap gap-1">
              {post.tags.map((tag) => (
                <ClickableTag key={tag} tag={tag} />
              ))}
            </div>
          )}
        </CardContent>
        {hasStats(post) && (
          <CardFooter className="gap-4 text-xs text-muted-foreground">
            <span className="flex items-center gap-1">
              <Heart className="h-3.5 w-3.5" /> {post.likeCount}
            </span>
            <span className="flex items-center gap-1">
              <MessageCircle className="h-3.5 w-3.5" /> {post.commentCount}
            </span>
            <span className="flex items-center gap-1">
              <Eye className="h-3.5 w-3.5" /> {post.viewCount}
            </span>
          </CardFooter>
        )}
      </Card>
    </Link>
  );
}
