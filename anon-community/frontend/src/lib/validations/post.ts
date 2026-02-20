import { z } from "zod";

export const postSchema = z.object({
  title: z
    .string()
    .min(1, "제목을 입력해주세요")
    .max(200, "제목은 최대 200자까지 입력 가능합니다"),
  content: z
    .string()
    .min(1, "내용을 입력해주세요")
    .max(10000, "내용은 최대 10,000자까지 입력 가능합니다"),
  category: z.enum([
    "DAILY",
    "ADVICE",
    "HUMOR",
    "INFORMATION",
    "QUESTION",
    "DISCUSSION",
    "MEDIA",
    "FEEDBACK",
  ]),
  tags: z
    .array(z.string().max(30, "태그는 최대 30자까지 입력 가능합니다"))
    .max(10, "태그는 최대 10개까지 추가 가능합니다"),
});

export type PostFormData = z.infer<typeof postSchema>;
