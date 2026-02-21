"use client";

import { Button } from "@/components/ui/button";

export default function GlobalError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4">
      <h2 className="text-2xl font-bold">오류가 발생했습니다</h2>
      <p className="text-muted-foreground">{error.message || "예기치 않은 오류가 발생했습니다"}</p>
      <Button onClick={reset}>다시 시도</Button>
    </div>
  );
}
