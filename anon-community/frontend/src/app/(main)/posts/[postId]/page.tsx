import { notFound } from "next/navigation";
import { Eye } from "lucide-react";
import { Separator } from "@/components/ui/separator";
import { serverFetch, ApiError } from "@/lib/api/client";
import { PostDetailResponse, POST_CATEGORIES } from "@/types/api";
import { timeAgo } from "@/lib/utils";
import { LikeButton } from "@/components/post/like-button";
import { ClickableTag } from "@/components/post/clickable-tag";
import { MarkdownContent } from "@/components/post/markdown-content";
import { CommentList } from "@/components/comment/comment-list";
import { PostActions } from "@/components/post/post-actions";
import { TranslateButton } from "@/components/post/translate-button";
import { BackButton } from "@/components/post/back-button";

interface Props {
  params: Promise<{ postId: string }>;
}

function getCategoryLabel(category: string): string {
  return POST_CATEGORIES.find((c) => c.value === category)?.label ?? category;
}

export default async function PostDetailPage({ params }: Props) {
  const { postId } = await params;
  const id = Number(postId);
  if (isNaN(id)) notFound();

  let post: PostDetailResponse;

  try {
    post = await serverFetch<PostDetailResponse>(`/posts/${id}`);
  } catch (e) {
    if (e instanceof ApiError && e.status === 404) {
      notFound();
    }
    throw e;
  }

  return (
    <article className="mx-auto max-w-2xl">
      <div className="mb-6 flex items-center justify-between">
        <BackButton />
        <PostActions postId={post.id} authorId={post.authorId} />
      </div>

      <h1 className="text-2xl font-bold leading-tight">{post.title}</h1>

      <div className="mt-3 flex items-center gap-3 text-sm text-muted-foreground">
        <span className="inline-flex h-6 w-6 items-center justify-center rounded-full bg-primary/10 text-xs font-bold text-primary">
          {String(post.authorId).slice(-1)}
        </span>
        <span>익명 #{post.authorId}</span>
        <span>&middot;</span>
        <span>{timeAgo(post.createdAt)}</span>
        <span>&middot;</span>
        <span className="inline-flex items-center rounded-md bg-muted px-2 py-0.5 text-xs font-medium">
          {getCategoryLabel(post.category)}
        </span>
        <span>&middot;</span>
        <span className="flex items-center gap-1">
          <Eye className="h-3.5 w-3.5" /> {post.viewCount}
        </span>
      </div>

      {post.tags.length > 0 && (
        <div className="mt-4 flex flex-wrap gap-1.5">
          {post.tags.map((tag) => (
            <ClickableTag key={tag} tag={tag} />
          ))}
        </div>
      )}

      <Separator className="my-6" />

      <div className="prose prose-neutral dark:prose-invert max-w-none leading-relaxed">
        <MarkdownContent content={post.content} />
      </div>

      <div className="my-4">
        <TranslateButton title={post.title} content={post.content} />
      </div>

      <Separator className="my-6" />

      <div className="flex items-center gap-4">
        <LikeButton postId={post.id} likeCount={post.likeCount} />
      </div>

      <Separator className="my-6" />

      <CommentList postId={post.id} initialCommentCount={post.commentCount} />
    </article>
  );
}
