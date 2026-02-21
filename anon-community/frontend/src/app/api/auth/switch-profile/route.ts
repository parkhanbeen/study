import { cookies } from "next/headers";
import { NextRequest, NextResponse } from "next/server";

const API_URL = process.env.API_URL || "http://localhost:9000";

export async function POST(request: NextRequest) {
  const cookieStore = await cookies();
  const accessToken = cookieStore.get("access_token")?.value;

  if (!accessToken) {
    return NextResponse.json({ code: "UNAUTHORIZED", message: "인증이 필요합니다" }, { status: 401 });
  }

  const body = await request.json();
  const profileId = body?.profileId;

  if (typeof profileId !== "number" || !Number.isInteger(profileId) || profileId <= 0) {
    return NextResponse.json({ code: "BAD_REQUEST", message: "유효하지 않은 프로필 ID입니다" }, { status: 400 });
  }

  const res = await fetch(`${API_URL}/api/v1/auth/switch-profile`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify({ profileId }),
  });

  if (!res.ok) {
    const error = await res.json().catch(() => ({ code: "UNKNOWN", message: "프로필 전환에 실패했습니다" }));
    return NextResponse.json(error, { status: res.status });
  }

  const data = await res.json();

  // Update the access_token cookie with the new token (which encodes the new active profile)
  cookieStore.set("access_token", data.accessToken, {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: "lax",
    path: "/",
    maxAge: 60 * 30, // 30 min
  });

  return NextResponse.json({ activeProfileId: data.activeProfileId });
}
