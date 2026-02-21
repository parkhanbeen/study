"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { Search, PenSquare, LogIn, LogOut, Settings, User, Sun, Moon, Monitor } from "lucide-react";
import { useTheme } from "next-themes";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { useAuthStore } from "@/stores/auth-store";
import { logout } from "@/lib/api/auth";
import { toast } from "sonner";

export function Header() {
  const router = useRouter();
  const { isLoggedIn, nickname, clearAuth } = useAuthStore();
  const { theme, setTheme } = useTheme();

  const toggleTheme = () => {
    if (theme === "dark") setTheme("light");
    else if (theme === "light") setTheme("system");
    else setTheme("dark");
  };

  const ThemeIcon = theme === "dark" ? Moon : theme === "light" ? Sun : Monitor;

  const handleLogout = async () => {
    try {
      await logout();
      clearAuth();
      router.push("/");
      router.refresh();
      toast.success("로그아웃되었습니다");
    } catch {
      toast.error("로그아웃에 실패했습니다");
    }
  };

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container mx-auto flex h-14 items-center justify-between px-4">
        <Link href="/" className="flex items-center gap-1.5 font-bold tracking-tight">
          <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-primary text-xs text-primary-foreground">
            A
          </span>
          <span className="text-lg">anon</span>
        </Link>

        <div className="flex items-center gap-1">
          <Button
            variant="ghost"
            size="icon"
            onClick={toggleTheme}
            aria-label="테마 전환"
            title={theme === "dark" ? "다크 모드" : theme === "light" ? "라이트 모드" : "시스템 모드"}
          >
            <ThemeIcon className="h-5 w-5" />
          </Button>

          <Button variant="ghost" size="icon" asChild>
            <Link href="/search">
              <Search className="h-5 w-5" />
            </Link>
          </Button>

          {isLoggedIn ? (
            <>
              <Button variant="ghost" size="icon" asChild>
                <Link href="/posts/new">
                  <PenSquare className="h-5 w-5" />
                </Link>
              </Button>

              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" size="icon">
                    <Avatar className="h-8 w-8">
                      <AvatarFallback className="bg-primary/10 text-sm font-medium text-primary">
                        {nickname?.[0]?.toUpperCase() ?? "?"}
                      </AvatarFallback>
                    </Avatar>
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end">
                  <DropdownMenuItem onClick={() => router.push("/my")}>
                    <User className="mr-2 h-4 w-4" />
                    마이페이지
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => router.push("/settings")}>
                    <Settings className="mr-2 h-4 w-4" />
                    설정
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={handleLogout}>
                    <LogOut className="mr-2 h-4 w-4" />
                    로그아웃
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </>
          ) : (
            <Button size="sm" asChild>
              <Link href="/login">
                <LogIn className="mr-1.5 h-4 w-4" />
                로그인
              </Link>
            </Button>
          )}
        </div>
      </div>
    </header>
  );
}
