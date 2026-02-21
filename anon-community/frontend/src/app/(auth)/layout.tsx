export default function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex min-h-screen bg-gradient-to-br from-primary/5 via-background to-secondary/10">
      {/* Left panel: service intro (desktop only) */}
      <div className="hidden lg:flex lg:flex-1 lg:flex-col lg:items-center lg:justify-center lg:px-12 lg:py-16">
        <div className="max-w-md space-y-6">
          <div className="flex items-center gap-3">
            <span className="flex h-12 w-12 items-center justify-center rounded-2xl bg-primary text-2xl font-black text-primary-foreground">
              A
            </span>
            <span className="text-3xl font-bold tracking-tight">anon-community</span>
          </div>
          <div className="space-y-4">
            <h2 className="text-2xl font-semibold leading-tight text-foreground">
              익명으로 솔직하게,<br />자유롭게 이야기해요
            </h2>
            <ul className="space-y-3 text-muted-foreground">
              <li className="flex items-start gap-2 text-sm">
                <span className="mt-0.5 text-primary">✓</span>
                완전 익명으로 글을 쓰고 댓글을 달 수 있어요
              </li>
              <li className="flex items-start gap-2 text-sm">
                <span className="mt-0.5 text-primary">✓</span>
                일상, 고민, 유머, 정보 등 다양한 카테고리
              </li>
              <li className="flex items-start gap-2 text-sm">
                <span className="mt-0.5 text-primary">✓</span>
                좋아요와 댓글로 공감을 표현하세요
              </li>
              <li className="flex items-start gap-2 text-sm">
                <span className="mt-0.5 text-primary">✓</span>
                한국어·영어·일본어·중국어 다국어 지원
              </li>
            </ul>
          </div>
        </div>
      </div>

      {/* Right panel: auth form */}
      <div className="flex flex-1 items-center justify-center px-4 py-12 lg:flex-none lg:w-[480px] lg:border-l">
        <div className="w-full max-w-sm">{children}</div>
      </div>
    </div>
  );
}
