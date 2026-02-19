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
import { useState } from "react";

const LANGUAGES = [
  { value: "ko", label: "한국어" },
  { value: "en", label: "English" },
  { value: "ja", label: "日本語" },
  { value: "zh", label: "中文" },
];

export default function SettingsPage() {
  const router = useRouter();
  const { clearAuth } = useAuthStore();
  const [language, setLanguage] = useState("ko");

  const handleLanguageChange = async (value: string) => {
    try {
      await fetchAPI("/members/me/language", {
        method: "PUT",
        body: JSON.stringify({ language: value }),
      });
      setLanguage(value);
      toast.success("Language updated");
    } catch {
      toast.error("Failed to update language");
    }
  };

  const handleLogout = async () => {
    try {
      await logout();
      clearAuth();
      router.push("/");
      router.refresh();
    } catch {
      toast.error("Logout failed");
    }
  };

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">Settings</h1>

      <Card>
        <CardHeader>
          <CardTitle>Language</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-2">
            <Label>Preferred language</Label>
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
        Logout
      </Button>
    </div>
  );
}
