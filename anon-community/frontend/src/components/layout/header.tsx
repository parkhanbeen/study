"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { Search, PenSquare, LogIn, LogOut, Settings } from "lucide-react";
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

  const handleLogout = async () => {
    try {
      await logout();
      clearAuth();
      router.push("/");
      router.refresh();
      toast.success("Logged out");
    } catch {
      toast.error("Logout failed");
    }
  };

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container mx-auto flex h-14 items-center justify-between px-4">
        <Link href="/" className="text-xl font-bold">
          anon
        </Link>

        <div className="flex items-center gap-2">
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
                      <AvatarFallback>{nickname?.[0]?.toUpperCase() ?? "?"}</AvatarFallback>
                    </Avatar>
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end">
                  <DropdownMenuItem onClick={() => router.push("/settings")}>
                    <Settings className="mr-2 h-4 w-4" />
                    Settings
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={handleLogout}>
                    <LogOut className="mr-2 h-4 w-4" />
                    Logout
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </>
          ) : (
            <Button variant="ghost" size="sm" asChild>
              <Link href="/login">
                <LogIn className="mr-2 h-4 w-4" />
                Login
              </Link>
            </Button>
          )}
        </div>
      </div>
    </header>
  );
}
