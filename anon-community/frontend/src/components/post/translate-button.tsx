"use client";

import { useState } from "react";
import { Languages } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";

const TRANSLATE_LANGUAGES = [
  { value: "ko", label: "한국어" },
  { value: "en", label: "English" },
  { value: "ja", label: "日本語" },
  { value: "zh-CN", label: "中文(简体)" },
  { value: "zh-TW", label: "中文(繁體)" },
];

interface TranslateButtonProps {
  title: string;
  content: string;
}

export function TranslateButton({ title, content }: TranslateButtonProps) {
  const [targetLang, setTargetLang] = useState<string | undefined>(undefined);
  const [translatedTitle, setTranslatedTitle] = useState<string | null>(null);
  const [translatedContent, setTranslatedContent] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [showTranslation, setShowTranslation] = useState(false);
  const [isError, setIsError] = useState(false);

  const doTranslate = async (lang: string) => {
    setIsLoading(true);
    setShowTranslation(false);
    setIsError(false);
    try {
      const text = `${title}\n---\n${content}`;
      const url = `https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=${lang}&dt=t&q=${encodeURIComponent(text)}`;
      const res = await fetch(url);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      const fullTranslation = data[0]?.map((item: [string]) => item[0]).join("") ?? "";
      const parts = fullTranslation.split("\n---\n");
      setTranslatedTitle(parts[0] ?? title);
      setTranslatedContent(parts.slice(1).join("\n---\n") || fullTranslation);
      setShowTranslation(true);
    } catch {
      setIsError(true);
      const fallbackUrl = `https://translate.google.com/?sl=auto&tl=${lang}&text=${encodeURIComponent(title + "\n\n" + content)}`;
      window.open(fallbackUrl, "_blank");
    } finally {
      setIsLoading(false);
    }
  };

  const handleLangChange = (lang: string) => {
    setTargetLang(lang);
    doTranslate(lang);
  };

  return (
    <div className="space-y-3">
      <div className="flex items-center gap-2">
        <Languages className="h-4 w-4 text-muted-foreground" />
        <Select value={targetLang} onValueChange={handleLangChange} disabled={isLoading}>
          <SelectTrigger className="w-44 h-8 text-xs">
            <SelectValue placeholder={isLoading ? "번역 중..." : "언어 선택 시 자동 번역"} />
          </SelectTrigger>
          <SelectContent>
            {TRANSLATE_LANGUAGES.map((lang) => (
              <SelectItem key={lang.value} value={lang.value}>
                {lang.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        {showTranslation && (
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setShowTranslation(false)}
            className="h-8 text-xs text-muted-foreground"
          >
            원문 보기
          </Button>
        )}
      </div>
      {isError && (
        <p className="text-xs text-destructive">번역에 실패했습니다. 잠시 후 다시 시도해주세요.</p>
      )}

      {showTranslation && translatedTitle && (
        <div className="rounded-lg border bg-muted/30 p-4 space-y-2">
          <p className="text-xs font-medium text-muted-foreground">번역 결과</p>
          <h2 className="text-lg font-semibold">{translatedTitle}</h2>
          <div className="whitespace-pre-wrap text-sm leading-relaxed">{translatedContent}</div>
        </div>
      )}
    </div>
  );
}
