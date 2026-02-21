import { Suspense } from "react";
import { Header } from "@/components/layout/header";
import { LeftSidebar } from "@/components/layout/left-sidebar";
import { RightSidebar } from "@/components/layout/right-sidebar";
import { MobileBottomNav } from "@/components/layout/mobile-bottom-nav";
// WriteFab removed: desktop → LeftSidebar write button, mobile → MobileBottomNav write tab

export default function MainLayout({ children }: { children: React.ReactNode }) {
  return (
    <>
      <Header />
      <div className="container mx-auto px-4 py-6">
        <div className="flex gap-6 items-start">
          {/* Left sidebar: desktop only (lg+) */}
          <aside className="hidden lg:block w-[220px] shrink-0">
            <Suspense fallback={null}>
              <LeftSidebar />
            </Suspense>
          </aside>

          {/* Main content */}
          <main className="flex-1 min-w-0 max-w-2xl">{children}</main>

          {/* Right sidebar: large desktop only (1200px+) */}
          <aside className="hidden min-[1200px]:block w-[260px] shrink-0">
            <RightSidebar />
          </aside>
        </div>
      </div>

      {/* Mobile bottom nav (hidden on lg+) */}
      <MobileBottomNav />

      {/* Spacer to prevent content from being hidden behind mobile nav */}
      <div className="h-14 lg:hidden" />
    </>
  );
}
