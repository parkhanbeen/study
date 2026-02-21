import { ErrorResponse } from "@/types/api";

export class ApiError extends Error {
  constructor(
    public status: number,
    public code: string,
    public override message: string,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

const API_BASE = "/api/proxy";

// Prevent concurrent refresh requests (race condition)
let refreshPromise: Promise<Response> | null = null;

async function refreshAccessToken(): Promise<boolean> {
  if (!refreshPromise) {
    refreshPromise = fetch("/api/auth/refresh", { method: "POST" });
  }
  try {
    const res = await refreshPromise;
    return res.ok;
  } finally {
    refreshPromise = null;
  }
}

export async function fetchAPI<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const url = `${API_BASE}${path}`;

  const doFetch = async () => {
    const res = await fetch(url, {
      ...options,
      headers: {
        "Content-Type": "application/json",
        ...options.headers,
      },
    });
    return res;
  };

  let res = await doFetch();

  // 401 → attempt token refresh → retry once
  if (res.status === 401) {
    const refreshed = await refreshAccessToken();
    if (refreshed) {
      res = await doFetch();
    } else {
      // Refresh failed — force logout
      await fetch("/api/auth/logout", { method: "POST" }).catch(() => {});
      window.location.href = "/login";
      return new Promise<never>(() => {});
    }
  }

  if (!res.ok) {
    let error: ErrorResponse;
    try {
      error = await res.json();
    } catch {
      error = { code: "UNKNOWN", message: "예기치 않은 오류가 발생했습니다" };
    }
    throw new ApiError(res.status, error.code, error.message);
  }

  if (res.status === 204) return undefined as T;
  return res.json();
}

// For Server Components — direct fetch to backend with token
export async function serverFetch<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const { cookies } = await import("next/headers");
  const cookieStore = await cookies();
  const accessToken = cookieStore.get("access_token")?.value;

  const apiUrl = process.env.API_URL || "http://localhost:9000";
  const url = `${apiUrl}/api/v1${path}`;

  const res = await fetch(url, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(accessToken && { Authorization: `Bearer ${accessToken}` }),
      ...options.headers,
    },
    next: { revalidate: 0 },
  });

  if (!res.ok) {
    let error: ErrorResponse;
    try {
      error = await res.json();
    } catch {
      error = { code: "UNKNOWN", message: res.statusText };
    }
    throw new ApiError(res.status, error.code, error.message);
  }

  if (res.status === 204) return undefined as T;
  return res.json();
}
