import Link from "next/link";
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { SignupForm } from "@/components/auth/signup-form";

export default function SignupPage() {
  return (
    <Card>
      <CardHeader className="text-center space-y-1">
        <div className="flex items-center justify-center gap-2 lg:hidden mb-2">
          <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-sm font-black text-primary-foreground">
            A
          </span>
          <span className="text-xl font-bold">anon-community</span>
        </div>
        <CardTitle className="text-2xl">회원가입</CardTitle>
        <p className="text-sm text-muted-foreground">지금 가입하고 익명으로 이야기해보세요</p>
      </CardHeader>
      <CardContent>
        <SignupForm />
      </CardContent>
      <CardFooter className="justify-center">
        <p className="text-sm text-muted-foreground">
          이미 계정이 있으신가요?{" "}
          <Link href="/login" className="text-primary hover:underline">
            로그인
          </Link>
        </p>
      </CardFooter>
    </Card>
  );
}
