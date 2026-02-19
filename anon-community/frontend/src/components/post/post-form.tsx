"use client";

import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { TagInput } from "./tag-input";
import { postSchema, PostFormData } from "@/lib/validations/post";
import { useCreatePost, useUpdatePost } from "@/lib/hooks/use-posts";
import { PostResponse } from "@/types/api";

interface PostFormProps {
  mode: "create" | "edit";
  post?: PostResponse;
}

export function PostForm({ mode, post }: PostFormProps) {
  const router = useRouter();
  const createMutation = useCreatePost();
  const updateMutation = useUpdatePost(post?.id ?? 0);

  const {
    register,
    handleSubmit,
    control,
    formState: { errors },
  } = useForm<PostFormData>({
    resolver: zodResolver(postSchema),
    defaultValues: {
      title: post?.title ?? "",
      content: post?.content ?? "",
      tags: post?.tags ?? [],
    },
  });

  const onSubmit = (data: PostFormData) => {
    if (mode === "create") {
      createMutation.mutate(data, {
        onSuccess: (res) => router.push(`/posts/${res.id}`),
      });
    } else {
      updateMutation.mutate(data, {
        onSuccess: () => router.push(`/posts/${post!.id}`),
      });
    }
  };

  const isPending = createMutation.isPending || updateMutation.isPending;

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="title">Title</Label>
        <Input id="title" {...register("title")} placeholder="Post title" maxLength={200} />
        {errors.title && <p className="text-sm text-destructive">{errors.title.message}</p>}
      </div>

      <div className="space-y-2">
        <Label htmlFor="content">Content</Label>
        <Textarea id="content" {...register("content")} placeholder="Write your post..." rows={12} maxLength={10000} />
        {errors.content && <p className="text-sm text-destructive">{errors.content.message}</p>}
      </div>

      <div className="space-y-2">
        <Label>Tags</Label>
        <Controller
          name="tags"
          control={control}
          render={({ field }) => <TagInput tags={field.value} onChange={field.onChange} />}
        />
      </div>

      <div className="flex justify-between">
        <Button type="button" variant="ghost" onClick={() => router.back()}>
          Cancel
        </Button>
        <Button type="submit" disabled={isPending}>
          {isPending ? "Saving..." : mode === "create" ? "Publish" : "Update"}
        </Button>
      </div>
    </form>
  );
}
