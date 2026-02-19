import { cookies } from "next/headers";
import { NextResponse } from "next/server";

export async function GET() {
  const cookieStore = await cookies();
  const accessToken = cookieStore.get("access_token")?.value;

  if (!accessToken) {
    return NextResponse.json({ isLoggedIn: false });
  }

  try {
    const payload = JSON.parse(atob(accessToken.split(".")[1]));
    return NextResponse.json({
      isLoggedIn: true,
      memberId: payload.sub ? Number(payload.sub) : null,
    });
  } catch {
    return NextResponse.json({ isLoggedIn: false });
  }
}
