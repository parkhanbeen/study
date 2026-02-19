"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useRouter, useSearchParams } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useAuthStore } from "@/stores/auth-store";
import { login } from "@/lib/api/auth";
import { ApiError } from "@/lib/api/client";
import { loginSchema, LoginFormData } from "@/lib/validations/auth";
import { toast } from "sonner";
import { useState } from "react";

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
      setAuth(res.memberId, data.nickname);
      const callbackUrl = searchParams.get("callbackUrl") || "/";
      router.push(callbackUrl);
      router.refresh();
    } catch (e) {
      toast.error(e instanceof ApiError ? e.message : "Login failed");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="nickname">Nickname</Label>
        <Input id="nickname" {...register("nickname")} placeholder="Enter your nickname" />
        {errors.nickname && <p className="text-sm text-destructive">{errors.nickname.message}</p>}
      </div>

      <div className="space-y-2">
        <Label htmlFor="password">Password</Label>
        <Input id="password" type="password" {...register("password")} placeholder="Enter your password" />
        {errors.password && <p className="text-sm text-destructive">{errors.password.message}</p>}
      </div>

      <Button type="submit" className="w-full" disabled={isLoading}>
        {isLoading ? "Logging in..." : "Login"}
      </Button>
    </form>
  );
}
