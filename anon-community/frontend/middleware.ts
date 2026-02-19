import { NextRequest, NextResponse } from "next/server";

const PROTECTED_ROUTES = ["/posts/new", "/settings"];

function isProtectedRoute(pathname: string): boolean {
  return PROTECTED_ROUTES.some((route) => pathname.startsWith(route))
    || pathname.match(/^\/posts\/\d+\/edit$/) !== null;
}

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  const accessToken = request.cookies.get("access_token")?.value;

  // Protected routes — redirect to login if no token
  if (isProtectedRoute(pathname) && !accessToken) {
    const loginUrl = new URL("/login", request.url);
    loginUrl.searchParams.set("callbackUrl", pathname);
    return NextResponse.redirect(loginUrl);
  }

  // Auth pages — redirect to home if already logged in
  if ((pathname === "/login" || pathname === "/signup") && accessToken) {
    return NextResponse.redirect(new URL("/", request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    "/((?!api|_next/static|_next/image|favicon.ico).*)",
  ],
};
