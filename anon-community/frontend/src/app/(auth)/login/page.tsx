import Link from "next/link";
import { Suspense } from "react";
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { LoginForm } from "@/components/auth/login-form";

export default function LoginPage() {
  return (
    <Card>
      <CardHeader className="text-center space-y-1">
        <div className="flex items-center justify-center gap-2 lg:hidden mb-2">
          <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-sm font-black text-primary-foreground">
            A
          </span>
          <span className="text-xl font-bold">anon-community</span>
        </div>
        <CardTitle className="text-2xl">로그인</CardTitle>
        <p className="text-sm text-muted-foreground">익명 커뮤니티에 오신 것을 환영합니다</p>
      </CardHeader>
      <CardContent>
        <Suspense>
          <LoginForm />
        </Suspense>
      </CardContent>
      <CardFooter className="justify-center">
        <p className="text-sm text-muted-foreground">
          계정이 없으신가요?{" "}
          <Link href="/signup" className="text-primary hover:underline">
            가입하기
          </Link>
        </p>
      </CardFooter>
    </Card>
  );
}
