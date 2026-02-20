"use client";

import { useState, useRef, useCallback } from "react";
import { Textarea } from "@/components/ui/textarea";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";

interface MarkdownEditorProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  rows?: number;
  maxLength?: number;
  error?: string;
}

type ToolbarButton = {
  label: string;
  title: string;
  prefix: string;
  suffix: string;
  placeholder: string;
};

const TOOLBAR_BUTTONS: ToolbarButton[] = [
  { label: "B",   title: "굵게",        prefix: "**",    suffix: "**",        placeholder: "굵은 텍스트" },
  { label: "I",   title: "이탤릭",      prefix: "_",     suffix: "_",         placeholder: "기울임 텍스트" },
  { label: "~~",  title: "취소선",      prefix: "~~",    suffix: "~~",        placeholder: "취소선 텍스트" },
  { label: "`",   title: "인라인 코드", prefix: "`",     suffix: "`",         placeholder: "코드" },
  { label: "```", title: "코드 블록",   prefix: "```\n", suffix: "\n```",     placeholder: "코드 블록" },
  { label: ">",   title: "인용구",      prefix: "> ",    suffix: "",          placeholder: "인용구" },
  { label: "🔗",  title: "링크",        prefix: "[",     suffix: "](url)",    placeholder: "링크 텍스트" },
];

export function MarkdownEditor({
  value,
  onChange,
  placeholder = "내용을 작성하세요... (마크다운 지원)",
  rows = 12,
  maxLength,
  error,
}: MarkdownEditorProps) {
  const [tab, setTab] = useState<"write" | "preview">("write");
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  const insertMarkdown = useCallback(
    (btn: ToolbarButton) => {
      const el = textareaRef.current;
      if (!el) return;

      const start = el.selectionStart;
      const end = el.selectionEnd;
      const selected = value.slice(start, end) || btn.placeholder;
      const newValue = value.slice(0, start) + btn.prefix + selected + btn.suffix + value.slice(end);
      onChange(newValue);

      requestAnimationFrame(() => {
        el.focus();
        const newStart = start + btn.prefix.length;
        el.setSelectionRange(newStart, newStart + selected.length);
      });
    },
    [value, onChange],
  );

  return (
    <div className="overflow-hidden rounded-md border border-input">
      {/* Tab bar + toolbar */}
      <div className="flex items-center gap-1 border-b bg-muted/40 px-2 py-1">
        <button
          type="button"
          onClick={() => setTab("write")}
          className={`rounded px-3 py-1 text-xs font-medium transition-colors ${
            tab === "write" ? "bg-background shadow-sm" : "text-muted-foreground hover:text-foreground"
          }`}
        >
          쓰기
        </button>
        <button
          type="button"
          onClick={() => setTab("preview")}
          className={`rounded px-3 py-1 text-xs font-medium transition-colors ${
            tab === "preview" ? "bg-background shadow-sm" : "text-muted-foreground hover:text-foreground"
          }`}
        >
          미리보기
        </button>

        {tab === "write" && (
          <>
            <div className="mx-2 h-4 w-px bg-border" />
            {TOOLBAR_BUTTONS.map((btn) => (
              <button
                key={btn.label}
                type="button"
                title={btn.title}
                onClick={() => insertMarkdown(btn)}
                className="rounded px-1.5 py-0.5 font-mono text-xs text-muted-foreground transition-colors hover:bg-background hover:text-foreground"
              >
                {btn.label}
              </button>
            ))}
          </>
        )}
      </div>

      {tab === "write" && (
        <Textarea
          ref={textareaRef}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder={placeholder}
          rows={rows}
          maxLength={maxLength}
          className="resize-y rounded-none border-0 focus-visible:ring-0"
        />
      )}

      {tab === "preview" && (
        <div className="prose prose-neutral dark:prose-invert max-w-none p-4 text-sm">
          {value ? (
            <ReactMarkdown remarkPlugins={[remarkGfm]}>{value}</ReactMarkdown>
          ) : (
            <p className="text-muted-foreground">미리보기할 내용이 없습니다.</p>
          )}
        </div>
      )}

      {error && (
        <p className="border-t px-3 py-1.5 text-xs text-destructive">{error}</p>
      )}
    </div>
  );
}
