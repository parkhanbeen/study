"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useRouter, useSearchParams } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useAuthStore } from "@/stores/auth-store";
import { login } from "@/lib/api/auth";
import { fetchAPI, ApiError } from "@/lib/api/client";
import { loginSchema, LoginFormData } from "@/lib/validations/auth";
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

export function LoginForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { setAuth } = useAuthStore();
  const [isLoading, setIsLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormData) => {
    setIsLoading(true);
    try {
      const res = await login(data);
      const profilesRaw = await fetchAPI<ProfileResponse[]>("/members/me/profiles");
      const profiles = profilesRaw.map(toActiveProfile);
      const activeProfile =
        profiles.find((p) => p.id === res.activeProfileId) ??
        profiles.find((p) => p.isDefault) ??
        profiles[0];
      if (!activeProfile) throw new Error("프로필 정보를 불러올 수 없습니다");
      setAuth(res.memberId, activeProfile, profiles);
      const raw = searchParams.get("callbackUrl") || "/";
      const callbackUrl = raw.startsWith("/") && !raw.startsWith("//") ? raw : "/";
      router.push(callbackUrl);
      router.refresh();
    } catch (e) {
      toast.error(e instanceof ApiError ? e.message : "로그인에 실패했습니다");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="nickname">닉네임</Label>
        <Input id="nickname" {...register("nickname")} placeholder="닉네임을 입력하세요" />
        {errors.nickname && <p className="text-sm text-destructive">{errors.nickname.message}</p>}
      </div>

      <div className="space-y-2">
        <Label htmlFor="password">비밀번호</Label>
        <Input id="password" type="password" {...register("password")} placeholder="비밀번호를 입력하세요" />
        {errors.password && <p className="text-sm text-destructive">{errors.password.message}</p>}
      </div>

      <Button type="submit" className="w-full" disabled={isLoading}>
        {isLoading ? "로그인 중..." : "로그인"}
      </Button>
    </form>
  );
}
