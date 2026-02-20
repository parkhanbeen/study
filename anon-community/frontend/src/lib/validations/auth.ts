import { z } from "zod";

export const loginSchema = z.object({
  nickname: z.string().min(1, "닉네임을 입력해주세요"),
  password: z.string().min(1, "비밀번호를 입력해주세요"),
});

export const signupSchema = z.object({
  nickname: z
    .string()
    .min(2, "닉네임은 최소 2자 이상이어야 합니다")
    .max(20, "닉네임은 최대 20자까지 입력 가능합니다")
    .regex(/^[\w\-.가-힣ぁ-んァ-ヶ一-龥]+$/, "닉네임에 허용되지 않는 문자가 포함되어 있습니다"),
  password: z.string().min(8, "비밀번호는 최소 8자 이상이어야 합니다").max(100),
  bio: z.string().max(200, "자기소개는 최대 200자까지 입력 가능합니다").optional(),
});

export type LoginFormData = z.infer<typeof loginSchema>;
export type SignupFormData = z.infer<typeof signupSchema>;
