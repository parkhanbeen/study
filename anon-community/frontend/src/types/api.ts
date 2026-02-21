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
export type PostCategory = "DAILY" | "ADVICE" | "HUMOR" | "INFORMATION" | "QUESTION" | "DISCUSSION" | "MEDIA" | "FEEDBACK";

export const POST_CATEGORIES: { value: PostCategory; label: string; emoji: string }[] = [
  { value: "DAILY",       label: "일상",   emoji: "☀️" },
  { value: "ADVICE",      label: "고민",   emoji: "💬" },
  { value: "HUMOR",       label: "유머",   emoji: "😂" },
  { value: "INFORMATION", label: "정보",   emoji: "📚" },
  { value: "QUESTION",    label: "질문",   emoji: "❓" },
  { value: "DISCUSSION",  label: "토론",   emoji: "🗣️" },
  { value: "MEDIA",       label: "미디어", emoji: "🎬" },
  { value: "FEEDBACK",    label: "피드백", emoji: "📝" },
];

export type PostStatus = "PUBLISHED" | "HIDDEN" | "UNDER_REVIEW" | "REMOVED" | "DELETED";

export interface PostResponse {
  id: PostId;
  authorId: MemberId;
  title: string;
  content: string;
  category: PostCategory;
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
  category: PostCategory;
  tags?: string[];
}

export interface UpdatePostRequest {
  title?: string;
  content?: string;
  category?: PostCategory;
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
  updatedAt: string;
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
export type SearchSortBy = "createdAt" | "likeCount" | "commentCount" | "viewCount" | "relevance";
export type SearchDateRange = "today" | "this_week" | "this_month";

export interface HotTagsResponse {
  tags: string[];
}

export interface SearchPostsParams {
  keyword?: string;
  tags?: string[];
  category?: PostCategory;
  language?: string;
  page?: number;
  size?: number;
}

export interface CursorSearchPostsParams {
  keyword?: string;
  tags?: string[];
  category?: PostCategory;
  language?: string;
  size?: number;
  searchAfter?: string;
  sortBy?: SearchSortBy;
  dateRange?: SearchDateRange;
}

export interface PostSearchHitResponse extends PostResponse {
  highlights: Record<string, string[]>;
}

// ── Error ──
export interface ErrorResponse {
  code: string;
  message: string;
}
