import Link from "next/link";
import { Button } from "@/components/ui/button";

export default function PostNotFound() {
  return (
    <div className="flex flex-col items-center justify-center py-20">
      <h2 className="text-2xl font-bold">Post not found</h2>
      <p className="mt-2 text-muted-foreground">The post you&apos;re looking for doesn&apos;t exist.</p>
      <Button asChild className="mt-4">
        <Link href="/">Go back to feed</Link>
      </Button>
    </div>
  );
}
