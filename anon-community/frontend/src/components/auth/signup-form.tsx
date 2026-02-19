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
import { ApiError } from "@/lib/api/client";
import { signupSchema, SignupFormData } from "@/lib/validations/auth";
import { toast } from "sonner";
import { useState } from "react";

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
      setAuth(res.memberId, data.nickname);
      router.push("/");
      router.refresh();
      toast.success("Welcome!");
    } catch (e) {
      if (e instanceof ApiError && e.code === "NICKNAME_DUPLICATED") {
        setError("nickname", { message: "This nickname is already taken" });
      } else {
        toast.error(e instanceof ApiError ? e.message : "Signup failed");
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="nickname">Nickname</Label>
        <Input id="nickname" {...register("nickname")} placeholder="2-20 characters" />
        {errors.nickname && <p className="text-sm text-destructive">{errors.nickname.message}</p>}
      </div>

      <div className="space-y-2">
        <Label htmlFor="password">Password</Label>
        <Input id="password" type="password" {...register("password")} placeholder="At least 8 characters" />
        {errors.password && <p className="text-sm text-destructive">{errors.password.message}</p>}
      </div>

      <div className="space-y-2">
        <Label htmlFor="bio">Bio (optional)</Label>
        <Textarea id="bio" {...register("bio")} placeholder="Tell us about yourself" rows={3} />
        {errors.bio && <p className="text-sm text-destructive">{errors.bio.message}</p>}
      </div>

      <Button type="submit" className="w-full" disabled={isLoading}>
        {isLoading ? "Creating account..." : "Sign up"}
      </Button>
    </form>
  );
}
