import Link from "next/link";
import { Button } from "@/components/ui/button";

export default function NotFound() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4">
      <h2 className="text-2xl font-bold">404 - 페이지를 찾을 수 없습니다</h2>
      <p className="text-muted-foreground">요청하신 페이지가 존재하지 않습니다.</p>
      <Button asChild>
        <Link href="/">홈으로</Link>
      </Button>
    </div>
  );
}
