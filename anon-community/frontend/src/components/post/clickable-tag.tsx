"use client";

import { useRouter } from "next/navigation";
import { Badge } from "@/components/ui/badge";

interface ClickableTagProps {
  tag: string;
}

export function ClickableTag({ tag }: ClickableTagProps) {
  const router = useRouter();

  const handleTagClick = (e: React.MouseEvent<HTMLButtonElement>) => {
    e.stopPropagation();
    router.push(`/search?tags=${encodeURIComponent(tag)}`);
  };

  return (
    <button
      type="button"
      onClick={handleTagClick}
      aria-label={`${tag} 태그 검색`}
      className="inline-block rounded-full focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-1"
    >
      <Badge
        variant="secondary"
        className="cursor-pointer text-xs transition-colors hover:bg-primary hover:text-primary-foreground"
      >
        #{tag}
      </Badge>
    </button>
  );
}
