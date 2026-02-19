import { z } from "zod";

export const postSchema = z.object({
  title: z.string().min(1, "Title is required").max(200, "Title must be at most 200 characters"),
  content: z.string().min(1, "Content is required").max(10000, "Content must be at most 10,000 characters"),
  tags: z.array(z.string().max(30)).max(10),
});

export type PostFormData = z.infer<typeof postSchema>;
