import { cookies } from "next/headers";
import { NextResponse } from "next/server";
import { serverFetch, ApiError } from "@/lib/api/client";

interface MemberResponse {
  id: number;
}

export async function GET() {
  const cookieStore = await cookies();
  const accessToken = cookieStore.get("access_token")?.value;

  if (!accessToken) {
    return NextResponse.json({ isLoggedIn: false });
  }

  try {
    const member = await serverFetch<MemberResponse>("/members/me");
    return NextResponse.json({ isLoggedIn: true, memberId: member.id });
  } catch (e) {
    // 401 = token invalid/expired, anything else = service error → treat as logged out
    if (e instanceof ApiError && e.status === 401) {
      return NextResponse.json({ isLoggedIn: false });
    }
    // Network error or other — fail gracefully
    return NextResponse.json({ isLoggedIn: false });
  }
}
