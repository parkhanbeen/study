"use client";
import { ProfileResponse } from "@/types/profile";
import { FollowButton } from "./follow-button";

interface ProfileHeaderProps {
  profile: ProfileResponse;
}

export function ProfileHeader({ profile }: ProfileHeaderProps) {
  return (
    <div className="flex flex-col gap-4 p-6 border-b">
      <div className="flex items-start justify-between">
        <div className="flex flex-col gap-1">
          <div className="flex items-center gap-3">
            <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center text-lg font-bold text-primary shrink-0">
              {profile.nickname[0]?.toUpperCase() ?? "A"}
            </div>
            <div>
              <div className="flex items-center gap-1.5">
                <h1 className="text-xl font-bold">{profile.nickname}</h1>
                {profile.isMe && (
                  <span className="text-xs text-muted-foreground">(나)</span>
                )}
              </div>
            </div>
          </div>
          {profile.bio && (
            <p className="text-sm text-muted-foreground mt-1 ml-[60px]">
              {profile.bio}
            </p>
          )}
        </div>
        <FollowButton
          profileId={profile.id}
          isFollowing={profile.isFollowing}
          isMe={profile.isMe}
        />
      </div>
      <div className="flex gap-6 text-sm">
        <div className="flex flex-col items-center">
          <span className="font-bold">{profile.postCount}</span>
          <span className="text-muted-foreground text-xs">게시글</span>
        </div>
        <div className="flex flex-col items-center">
          <span className="font-bold">{profile.followerCount}</span>
          <span className="text-muted-foreground text-xs">팔로워</span>
        </div>
        <div className="flex flex-col items-center">
          <span className="font-bold">{profile.followingCount}</span>
          <span className="text-muted-foreground text-xs">팔로잉</span>
        </div>
      </div>
    </div>
  );
}
