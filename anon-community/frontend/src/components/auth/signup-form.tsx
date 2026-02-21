"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { useAuthStore } from "@/stores/auth-store";
import { signup } from "@/lib/api/auth";
import { fetchAPI, ApiError } from "@/lib/api/client";
import { signupSchema, SignupFormData } from "@/lib/validations/auth";
import { toast } from "sonner";
import { useState } from "react";
import { ProfileResponse, ActiveProfile } from "@/types/profile";

function toActiveProfile(p: ProfileResponse): ActiveProfile {
  return {
    id: p.id,
    nickname: p.nickname,
    bio: p.bio,
    postCount: p.postCount,
    followerCount: p.followerCount,
    followingCount: p.followingCount,
    isDefault: p.isDefault,
  };
}

export function SignupForm() {
  const router = useRouter();
  const { setAuth } = useAuthStore();
  const [isLoading, setIsLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
    setError,
  } = useForm<SignupFormData>({
    resolver: zodResolver(signupSchema),
  });

  const onSubmit = async (data: SignupFormData) => {
    setIsLoading(true);
    try {
      const res = await signup(data);
      const profilesRaw = await fetchAPI<ProfileResponse[]>("/members/me/profiles");
      const profiles = profilesRaw.map(toActiveProfile);
      const activeProfile =
        profiles.find((p) => p.isDefault) ?? profiles[0];
      if (!activeProfile) throw new Error("프로필 정보를 불러올 수 없습니다");
      setAuth(res.memberId, activeProfile, profiles);
      router.push("/");
      router.refresh();
      toast.success("가입을 환영합니다!");
    } catch (e) {
      if (e instanceof ApiError && e.code === "NICKNAME_DUPLICATED") {
        setError("nickname", { message: "이미 사용 중인 닉네임입니다" });
      } else {
        toast.error(e instanceof ApiError ? e.message : "가입에 실패했습니다");
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="nickname">닉네임</Label>
        <Input id="nickname" {...register("nickname")} placeholder="2-20자" />
        {errors.nickname && <p className="text-sm text-destructive">{errors.nickname.message}</p>}
      </div>

      <div className="space-y-2">
        <Label htmlFor="password">비밀번호</Label>
        <Input id="password" type="password" {...register("password")} placeholder="8자 이상" />
        {errors.password && <p className="text-sm text-destructive">{errors.password.message}</p>}
      </div>

      <div className="space-y-2">
        <Label htmlFor="bio">자기소개 (선택)</Label>
        <Textarea id="bio" {...register("bio")} placeholder="자기소개를 입력하세요" rows={3} />
        {errors.bio && <p className="text-sm text-destructive">{errors.bio.message}</p>}
      </div>

      <Button type="submit" className="w-full" disabled={isLoading}>
        {isLoading ? "계정 생성 중..." : "가입하기"}
      </Button>
    </form>
  );
}
