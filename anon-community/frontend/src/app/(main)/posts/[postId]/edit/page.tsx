import { notFound, redirect } from "next/navigation";
import { serverFetch, ApiError } from "@/lib/api/client";
import { PostDetailResponse, MemberResponse } from "@/types/api";
import { PostForm } from "@/components/post/post-form";

interface Props {
  params: Promise<{ postId: string }>;
}

export default async function EditPostPage({ params }: Props) {
  const { postId } = await params;
  const id = Number(postId);
  if (isNaN(id)) notFound();

  let post: PostDetailResponse;
  try {
    post = await serverFetch<PostDetailResponse>(`/posts/${id}`);
  } catch (e) {
    if (e instanceof ApiError && e.status === 404) notFound();
    throw e;
  }

  // Verify the current user is the post author before rendering the edit form.
  // Keep redirect() outside the catch block — Next.js redirect() throws internally
  // and would be swallowed if called inside a broad catch clause.
  let me: MemberResponse;
  try {
    me = await serverFetch<MemberResponse>("/members/me");
  } catch (e) {
    if (e instanceof ApiError && e.status === 401) {
      redirect(`/login?callbackUrl=${encodeURIComponent(`/posts/${id}/edit`)}`);
    }
    // For any other error (network failure, 500, etc.), fail safe.
    redirect(`/posts/${id}`);
  }

  if (me.id !== post.authorId) {
    redirect(`/posts/${id}`);
  }

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold">글 수정</h1>
      <PostForm mode="edit" post={post} />
    </div>
  );
}
