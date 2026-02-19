import { PostForm } from "@/components/post/post-form";

export default function NewPostPage() {
  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold">New Post</h1>
      <PostForm mode="create" />
    </div>
  );
}
