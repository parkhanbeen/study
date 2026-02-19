import { Header } from "@/components/layout/header";

export default function MainLayout({ children }: { children: React.ReactNode }) {
  return (
    <>
      <Header />
      <main className="container mx-auto max-w-2xl px-4 py-6">{children}</main>
    </>
  );
}
