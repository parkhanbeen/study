import { z } from "zod";

export const loginSchema = z.object({
  nickname: z.string().min(1, "Nickname is required"),
  password: z.string().min(1, "Password is required"),
});

export const signupSchema = z.object({
  nickname: z
    .string()
    .min(2, "At least 2 characters")
    .max(20, "At most 20 characters")
    .regex(/^[\w\-.가-힣ぁ-んァ-ヶ一-龥]+$/, "Letters, numbers, dots, hyphens, underscores only"),
  password: z.string().min(8, "At least 8 characters").max(100),
  bio: z.string().max(200, "At most 200 characters").optional(),
});

export type LoginFormData = z.infer<typeof loginSchema>;
export type SignupFormData = z.infer<typeof signupSchema>;
