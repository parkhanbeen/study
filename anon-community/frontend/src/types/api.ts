// ── Identifiers ──
export type MemberId = number;
export type PostId = number;
export type CommentId = number;

// ── Auth ──
export interface LoginRequest {
  nickname: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  memberId: MemberId;
}

export interface RefreshRequest {
  refreshToken: string;
}

export interface RefreshResponse {
  accessToken: string;
  refreshToken: string;
  memberId: MemberId;
}

export interface LogoutRequest {
  refreshToken: string;
}

// ── Member ──
export interface MemberResponse {
  id: MemberId;
  nickname: string;
  bio: string;
  postCount: number;
  preferredLanguage: string | null;
  createdAt: string;
}

export interface CreateMemberRequest {
  nickname: string;
  password: string;
  bio?: string;
}

export interface CreateMemberResponse {
  member: MemberResponse;
  accessToken: string;
  refreshToken: string;
}

export interface UpdateLanguageRequest {
  language: string;
}

// ── Post ──
export type PostStatus = "PUBLISHED" | "HIDDEN" | "UNDER_REVIEW" | "REMOVED" | "DELETED";

export interface PostResponse {
  id: PostId;
  authorId: MemberId;
  title: string;
  content: string;
  tags: string[];
  status: PostStatus;
  createdAt: string;
  updatedAt: string;
}

export interface PostDetailResponse extends PostResponse {
  likeCount: number;
  commentCount: number;
  viewCount: number;
}

export interface CreatePostRequest {
  title: string;
  content: string;
  tags?: string[];
}

export interface UpdatePostRequest {
  title?: string;
  content?: string;
  tags?: string[];
}

// ── Comment ──
export type CommentStatus = "ACTIVE" | "DELETED";

export interface CommentResponse {
  id: CommentId;
  postId: PostId;
  authorId: MemberId;
  content: string;
  parentCommentId: CommentId | null;
  depth: number;
  status: CommentStatus;
  createdAt: string;
}

export interface CreateCommentRequest {
  content: string;
  parentCommentId?: CommentId;
}

// ── Pagination ──
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface CursorResponse<T> {
  content: T[];
  size: number;
  searchAfter: string | null;
  hasNext: boolean;
}

// ── Search ──
export interface SearchPostsParams {
  keyword?: string;
  tags?: string[];
  language?: string;
  page?: number;
  size?: number;
}

export interface CursorSearchPostsParams {
  keyword?: string;
  tags?: string[];
  language?: string;
  size?: number;
  searchAfter?: string;
}

// ── Error ──
export interface ErrorResponse {
  code: string;
  message: string;
}
