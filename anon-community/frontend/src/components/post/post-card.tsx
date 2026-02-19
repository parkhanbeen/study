import Link from "next/link";
import { Heart, MessageCircle, Eye } from "lucide-react";
import { Card, CardContent, CardFooter, CardHeader } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { PostResponse, PostDetailResponse } from "@/types/api";
import { timeAgo } from "@/lib/utils";

interface PostCardProps {
  post: PostResponse | PostDetailResponse;
}

function hasStats(post: PostResponse | PostDetailResponse): post is PostDetailResponse {
  return "likeCount" in post;
}

export function PostCard({ post }: PostCardProps) {
  return (
    <Link href={`/posts/${post.id}`}>
      <Card className="transition-colors hover:bg-accent/50">
        <CardHeader className="pb-2">
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <span>Anonymous #{post.authorId}</span>
            <span>&middot;</span>
            <span>{timeAgo(post.createdAt)}</span>
          </div>
          <h3 className="text-lg font-semibold leading-tight">{post.title}</h3>
        </CardHeader>
        <CardContent className="pb-2">
          <p className="line-clamp-2 text-muted-foreground">{post.content}</p>
          {post.tags.length > 0 && (
            <div className="mt-2 flex flex-wrap gap-1">
              {post.tags.map((tag) => (
                <Badge key={tag} variant="secondary" className="text-xs">
                  {tag}
                </Badge>
              ))}
            </div>
          )}
        </CardContent>
        {hasStats(post) && (
          <CardFooter className="text-sm text-muted-foreground gap-4">
            <span className="flex items-center gap-1">
              <Heart className="h-4 w-4" /> {post.likeCount}
            </span>
            <span className="flex items-center gap-1">
              <MessageCircle className="h-4 w-4" /> {post.commentCount}
            </span>
            <span className="flex items-center gap-1">
              <Eye className="h-4 w-4" /> {post.viewCount}
            </span>
          </CardFooter>
        )}
      </Card>
    </Link>
  );
}
