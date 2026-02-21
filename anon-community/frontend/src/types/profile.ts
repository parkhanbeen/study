export interface ProfileResponse {
  id: number;
  memberId: number;
  nickname: string;
  bio: string | null;
  postCount: number;
  followerCount: number;
  followingCount: number;
  isDefault: boolean;
  isFollowing: boolean;
  isMe: boolean;
  createdAt: string;
}

export interface ActiveProfile {
  id: number;
  nickname: string;
  bio: string | null;
  postCount: number;
  followerCount: number;
  followingCount: number;
  isDefault: boolean;
}

export interface LinkedAccount {
  memberId: number;
  activeProfileId: number;
  nickname: string; // active profile nickname
  accessToken: string; // NOT stored in httpOnly cookie — used for account switching UI only
}
