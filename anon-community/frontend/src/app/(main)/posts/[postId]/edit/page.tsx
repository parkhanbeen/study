import { notFound } from "next/navigation";
import { serverFetch, ApiError } from "@/lib/api/client";
import { PostDetailResponse } from "@/types/api";
import { PostForm } from "@/components/post/post-form";

interface Props {
  params: Promise<{ postId: string }>;
}

export default async function EditPostPage({ params }: Props) {
  const { postId } = await params;

  let post: PostDetailResponse;
  try {
    post = await serverFetch<PostDetailResponse>(`/posts/${postId}`);
  } catch (e) {
    if (e instanceof ApiError && e.status === 404) notFound();
    throw e;
  }

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold">Edit Post</h1>
      <PostForm mode="edit" post={post} />
    </div>
  );
}
