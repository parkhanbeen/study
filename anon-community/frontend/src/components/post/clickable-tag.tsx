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
    <button type="button" onClick={handleTagClick} className="inline-block">
      <Badge
        variant="secondary"
        className="cursor-pointer text-xs transition-colors hover:bg-primary hover:text-primary-foreground"
      >
        #{tag}
      </Badge>
    </button>
  );
}
