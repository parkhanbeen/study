import { LoginRequest, CreateMemberRequest, ErrorResponse } from "@/types/api";
import { ApiError } from "./client";

// Route handler returns only non-sensitive data (tokens are in httpOnly cookies)
interface AuthProxyResponse {
  memberId: number;
  nickname?: string;
}

export async function login(data: LoginRequest): Promise<AuthProxyResponse> {
  const res = await fetch("/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!res.ok) {
    const error: ErrorResponse = await res.json().catch(() => ({ code: "UNKNOWN", message: "Login failed" }));
    throw new ApiError(res.status, error.code, error.message);
  }
  return res.json();
}

export async function signup(data: CreateMemberRequest): Promise<AuthProxyResponse> {
  const res = await fetch("/api/auth/signup", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!res.ok) {
    const error: ErrorResponse = await res.json().catch(() => ({ code: "UNKNOWN", message: "Signup failed" }));
    throw new ApiError(res.status, error.code, error.message);
  }
  return res.json();
}

export async function logout(): Promise<void> {
  await fetch("/api/auth/logout", { method: "POST" });
}

export async function refreshToken(): Promise<AuthProxyResponse> {
  const res = await fetch("/api/auth/refresh", { method: "POST" });
  if (!res.ok) {
    throw new ApiError(res.status, "UNAUTHORIZED", "Refresh failed");
  }
  return res.json();
}
