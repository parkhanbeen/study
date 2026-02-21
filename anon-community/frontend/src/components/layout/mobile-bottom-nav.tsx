"use client";

import { usePathname, useRouter } from "next/navigation";
import { Home, Search, PenSquare, User } from "lucide-react";
import { useAuthStore } from "@/stores/auth-store";
import { cn } from "@/lib/utils";

const NAV_ITEMS = [
  { href: "/",          icon: Home,      label: "홈",     requireAuth: false },
  { href: "/search",    icon: Search,    label: "검색",   requireAuth: false },
  { href: "/posts/new", icon: PenSquare, label: "글쓰기", requireAuth: true  },
  { href: "/my",        icon: User,      label: "마이",   requireAuth: true  },
];

export function MobileBottomNav() {
  const pathname = usePathname();
  const router = useRouter();
  const { isLoggedIn, isInitialized } = useAuthStore();

  const handleNavClick = (href: string, requireAuth: boolean) => {
    if (requireAuth && isInitialized && !isLoggedIn) {
      router.push(`/login?callbackUrl=${encodeURIComponent(href)}`);
      return;
    }
    router.push(href);
  };

  return (
    <nav
      className="fixed bottom-0 left-0 right-0 z-50 lg:hidden border-t bg-background"
      aria-label="하단 탭 바"
    >
      <div className="flex items-center justify-around h-14">
        {NAV_ITEMS.map(({ href, icon: Icon, label, requireAuth }) => {
          const isActive = href === "/" ? pathname === "/" : pathname.startsWith(href);
          return (
            <button
              key={href}
              onClick={() => handleNavClick(href, requireAuth)}
              aria-label={label}
              aria-current={isActive ? "page" : undefined}
              className={cn(
                "flex flex-col items-center justify-center gap-0.5 flex-1 h-full text-xs font-medium transition-colors",
                isActive ? "text-primary" : "text-muted-foreground hover:text-foreground",
              )}
            >
              <Icon className="h-5 w-5" />
              {label}
            </button>
          );
        })}
      </div>
    </nav>
  );
}
