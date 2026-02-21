"use client";

import { Fragment } from "react";
import Link from "next/link";

interface HashtagContentProps {
  content: string;
}

const HASHTAG_REGEX = /#([\w\uAC00-\uD7A3\u3040-\u309F\u30A0-\u30FF\u4E00-\u9FFF]+)/g;

export function HashtagContent({ content }: HashtagContentProps) {
  const parts: { type: "text" | "tag"; value: string }[] = [];
  let lastIndex = 0;

  for (const match of content.matchAll(HASHTAG_REGEX)) {
    const index = match.index!;
    if (index > lastIndex) {
      parts.push({ type: "text", value: content.slice(lastIndex, index) });
    }
    parts.push({ type: "tag", value: match[1] });
    lastIndex = index + match[0].length;
  }

  if (lastIndex < content.length) {
    parts.push({ type: "text", value: content.slice(lastIndex) });
  }

  if (parts.length === 0) {
    return <span>{content}</span>;
  }

  return (
    <>
      {parts.map((part, i) =>
        part.type === "tag" ? (
          <Link
            key={i}
            href={`/search?tags=${encodeURIComponent(part.value)}`}
            className="text-primary font-medium hover:underline"
            onClick={(e) => e.stopPropagation()}
          >
            #{part.value}
          </Link>
        ) : (
          <Fragment key={i}>{part.value}</Fragment>
        ),
      )}
    </>
  );
}
