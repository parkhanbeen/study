import Link from "next/link";
import { Button } from "@/components/ui/button";

export default function PostNotFound() {
  return (
    <div className="flex flex-col items-center justify-center py-20">
      <h2 className="text-2xl font-bold">게시글을 찾을 수 없습니다</h2>
      <p className="mt-2 text-muted-foreground">요청하신 게시글이 존재하지 않습니다.</p>
      <Button asChild className="mt-4">
        <Link href="/">피드로 돌아가기</Link>
      </Button>
    </div>
  );
}
