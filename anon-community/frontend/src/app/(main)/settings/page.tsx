"use client";

import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Separator } from "@/components/ui/separator";
import { Label } from "@/components/ui/label";
import { useAuthStore } from "@/stores/auth-store";
import { logout } from "@/lib/api/auth";
import { fetchAPI } from "@/lib/api/client";
import { toast } from "sonner";
import { useState, useEffect } from "react";

const LANGUAGES = [
  { value: "ko", label: "한국어" },
  { value: "en", label: "English" },
  { value: "ja", label: "日本語" },
  { value: "zh-CN", label: "中文(简体)" },
  { value: "zh-TW", label: "中文(繁體)" },
];

export default function SettingsPage() {
  const router = useRouter();
  const { clearAuth, isLoggedIn } = useAuthStore();
  const [language, setLanguage] = useState("ko");

  useEffect(() => {
    if (!isLoggedIn) {
      router.replace("/login?callbackUrl=/settings");
    }
  }, [isLoggedIn, router]);

  useEffect(() => {
    fetchAPI<{ preferredLanguage: string | null }>("/members/me")
      .then((member) => {
        if (member.preferredLanguage) setLanguage(member.preferredLanguage);
      })
      .catch(() => {});
  }, []);

  if (!isLoggedIn) return null;

  const handleLanguageChange = async (value: string) => {
    try {
      await fetchAPI("/members/me/language", {
        method: "PUT",
        body: JSON.stringify({ language: value }),
      });
      setLanguage(value);
      toast.success("언어가 변경되었습니다");
    } catch {
      toast.error("언어 변경에 실패했습니다");
    }
  };

  const handleLogout = async () => {
    try {
      await logout();
      clearAuth();
      router.push("/");
      router.refresh();
    } catch {
      toast.error("로그아웃에 실패했습니다");
    }
  };

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">설정</h1>

      <Card>
        <CardHeader>
          <CardTitle>언어</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-2">
            <Label>선호 언어</Label>
            <Select value={language} onValueChange={handleLanguageChange}>
              <SelectTrigger className="w-48">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {LANGUAGES.map((lang) => (
                  <SelectItem key={lang.value} value={lang.value}>
                    {lang.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      <Separator />

      <Button variant="destructive" onClick={handleLogout}>
        로그아웃
      </Button>
    </div>
  );
}
