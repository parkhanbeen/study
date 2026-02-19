import { notFound } from "next/navigation";
import Link from "next/link";
import { ArrowLeft, Eye, MessageCircle } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { serverFetch, ApiError } from "@/lib/api/client";
import { PostDetailResponse } from "@/types/api";
import { timeAgo } from "@/lib/utils";
import { LikeButton } from "@/components/post/like-button";
import { CommentList } from "@/components/comment/comment-list";

interface Props {
  params: Promise<{ postId: string }>;
}

export default async function PostDetailPage({ params }: Props) {
  const { postId } = await params;
  let post: PostDetailResponse;

  try {
    post = await serverFetch<PostDetailResponse>(`/posts/${postId}`);
  } catch (e) {
    if (e instanceof ApiError && e.status === 404) {
      notFound();
    }
    throw e;
  }

  return (
    <article>
      <div className="mb-4">
        <Button variant="ghost" size="sm" asChild>
          <Link href="/">
            <ArrowLeft className="mr-1 h-4 w-4" /> Back
          </Link>
        </Button>
      </div>

      <h1 className="text-2xl font-bold">{post.title}</h1>

      <div className="mt-2 flex items-center gap-2 text-sm text-muted-foreground">
        <span>Anonymous #{post.authorId}</span>
        <span>·</span>
        <span>{timeAgo(post.createdAt)}</span>
        <span>·</span>
        <span className="flex items-center gap-1">
          <Eye className="h-3 w-3" /> {post.viewCount}
        </span>
      </div>

      {post.tags.length > 0 && (
        <div className="mt-3 flex flex-wrap gap-1">
          {post.tags.map((tag) => (
            <Badge key={tag} variant="secondary">
              {tag}
            </Badge>
          ))}
        </div>
      )}

      <Separator className="my-4" />

      <div className="prose prose-neutral max-w-none whitespace-pre-wrap">
        {post.content}
      </div>

      <Separator className="my-4" />

      <div className="flex items-center gap-4">
        <LikeButton postId={post.id} likeCount={post.likeCount} />
        <span className="flex items-center gap-1 text-sm text-muted-foreground">
          <MessageCircle className="h-5 w-5" /> {post.commentCount}
        </span>
      </div>

      <Separator className="my-4" />

      <CommentList postId={post.id} />
    </article>
  );
}
