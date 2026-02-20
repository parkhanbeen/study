import { redirect } from "next/navigation";
import { cookies } from "next/headers";
import { PostForm } from "@/components/post/post-form";

export default async function NewPostPage() {
  const cookieStore = await cookies();
  const accessToken = cookieStore.get("access_token")?.value;

  if (!accessToken) {
    redirect("/login?callbackUrl=/posts/new");
  }

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold">새 글 작성</h1>
      <PostForm mode="create" />
    </div>
  );
}
