"use client";

import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { MarkdownEditor } from "./markdown-editor";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { TagInput } from "./tag-input";
import { postSchema, PostFormData } from "@/lib/validations/post";
import { useCreatePost, useUpdatePost } from "@/lib/hooks/use-posts";
import { PostResponse, POST_CATEGORIES } from "@/types/api";

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
      category: post?.category ?? "DAILY",
      tags: post?.tags ?? [],
    },
  });

  const onSubmit = (data: PostFormData) => {
    if (mode === "create") {
      createMutation.mutate(data, {
        onSuccess: (res) => {
          router.push(`/posts/${res.id}`);
          router.refresh();
        },
      });
    } else {
      updateMutation.mutate(data, {
        onSuccess: () => {
          router.push(`/posts/${post!.id}`);
          router.refresh();
        },
      });
    }
  };

  const isPending = createMutation.isPending || updateMutation.isPending;

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="title">제목</Label>
        <Input id="title" {...register("title")} placeholder="게시글 제목" maxLength={200} />
        {errors.title && <p className="text-sm text-destructive">{errors.title.message}</p>}
      </div>

      <div className="space-y-2">
        <Label>카테고리</Label>
        <Controller
          name="category"
          control={control}
          render={({ field }) => (
            <Select value={field.value} onValueChange={field.onChange}>
              <SelectTrigger>
                <SelectValue placeholder="카테고리 선택" />
              </SelectTrigger>
              <SelectContent>
                {POST_CATEGORIES.map((cat) => (
                  <SelectItem key={cat.value} value={cat.value}>
                    {cat.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          )}
        />
        {errors.category && <p className="text-sm text-destructive">{errors.category.message}</p>}
      </div>

      <div className="space-y-2">
        <Label>내용</Label>
        <Controller
          name="content"
          control={control}
          render={({ field }) => (
            <MarkdownEditor
              value={field.value}
              onChange={field.onChange}
              placeholder="게시글 내용을 작성하세요... (마크다운 지원)"
              rows={12}
              maxLength={10000}
              error={errors.content?.message}
            />
          )}
        />
      </div>

      <div className="space-y-2">
        <Label>태그</Label>
        <Controller
          name="tags"
          control={control}
          render={({ field }) => <TagInput tags={field.value} onChange={field.onChange} />}
        />
      </div>

      <div className="flex justify-between">
        <Button type="button" variant="ghost" onClick={() => router.back()}>
          취소
        </Button>
        <Button type="submit" disabled={isPending}>
          {isPending ? "저장 중..." : mode === "create" ? "게시하기" : "수정하기"}
        </Button>
      </div>
    </form>
  );
}
