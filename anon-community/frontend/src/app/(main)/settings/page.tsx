"use client";

import { useRouter } from "next/navigation";
import { useTheme } from "next-themes";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Separator } from "@/components/ui/separator";
import { Label } from "@/components/ui/label";
import { useAuthStore } from "@/stores/auth-store";
import { logout } from "@/lib/api/auth";
import { fetchAPI } from "@/lib/api/client";
import { MemberResponse } from "@/types/api";
import { Badge } from "@/components/ui/badge";
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
  const { clearAuth, isLoggedIn, isInitialized, profiles, accounts, activeProfile, switchProfile } = useAuthStore();
  const { theme, setTheme } = useTheme();
  const [language, setLanguage] = useState("ko");
  const [member, setMember] = useState<MemberResponse | null>(null);

  useEffect(() => {
    if (!isInitialized) return;
    if (!isLoggedIn) {
      router.replace("/login?callbackUrl=/settings");
    }
  }, [isInitialized, isLoggedIn, router]);

  useEffect(() => {
    fetchAPI<MemberResponse>("/members/me")
      .then((data) => {
        setMember(data);
        if (data.preferredLanguage) setLanguage(data.preferredLanguage);
      })
      .catch(() => {});
  }, []);

  if (!isInitialized) return null;
  if (!isLoggedIn) return null;

  const handleSwitchProfile = async (profileId: number) => {
    try {
      await switchProfile(profileId);
      toast.success("프로필이 전환되었습니다");
    } catch {
      toast.error("프로필 전환에 실패했습니다");
    }
  };

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

      {member && (
        <Card>
          <CardHeader>
            <CardTitle>계정 정보</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">닉네임</span>
              <span className="font-medium">{member.nickname}</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">게시글 수</span>
              <span className="font-medium">{member.postCount}개</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">가입일</span>
              <span className="font-medium">
                {new Date(member.createdAt).toLocaleDateString("ko-KR")}
              </span>
            </div>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader>
          <CardTitle>프로필 관리</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2">
          {profiles.map((profile) => (
            <div key={profile.id} className="flex items-center justify-between py-2 border-b last:border-0">
              <div className="flex items-center gap-2">
                <span className="text-sm font-medium">{profile.nickname}</span>
                {profile.isDefault && <Badge variant="secondary" className="text-xs">기본</Badge>}
                {profile.id === activeProfile?.id && <Badge className="text-xs">현재</Badge>}
              </div>
              <div className="flex gap-2">
                <Button
                  size="sm"
                  variant="outline"
                  disabled={profile.id === activeProfile?.id}
                  onClick={() => handleSwitchProfile(profile.id)}
                >
                  전환
                </Button>
                <Button size="sm" variant="outline" onClick={() => toast.info("준비중입니다")}>
                  수정
                </Button>
                {!profile.isDefault && (
                  <Button size="sm" variant="destructive" onClick={() => toast.info("준비중입니다")}>
                    삭제
                  </Button>
                )}
              </div>
            </div>
          ))}
          {profiles.length < 5 && (
            <Button size="sm" variant="outline" className="mt-2" onClick={() => toast.info("준비중입니다")}>
              + 프로필 추가
            </Button>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>계정 관리</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2">
          {accounts.map((account) => (
            <div key={account.memberId} className="flex justify-between text-sm py-1 border-b last:border-0">
              <span className="text-muted-foreground">닉네임</span>
              <span className="font-medium">{account.nickname}</span>
            </div>
          ))}
          <Button size="sm" variant="outline" className="mt-2" onClick={() => toast.info("준비중입니다")}>
            계정 추가
          </Button>
        </CardContent>
      </Card>

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

      <Card>
        <CardHeader>
          <CardTitle>화면</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-2">
            <Label>테마</Label>
            <Select value={theme ?? "system"} onValueChange={setTheme}>
              <SelectTrigger className="w-48">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="system">시스템 설정</SelectItem>
                <SelectItem value="light">라이트</SelectItem>
                <SelectItem value="dark">다크</SelectItem>
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
