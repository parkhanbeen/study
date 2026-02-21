"use client";
import { use, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { profilesApi } from "@/lib/api/profiles";
import { ProfileHeader } from "@/components/profile/profile-header";
import { PostCard } from "@/components/post/post-card";
import { ProfileResponse } from "@/types/profile";
import Link from "next/link";

type ProfileTab = "posts" | "followers" | "following";

const TAB_LABELS: Record<ProfileTab, string> = {
  posts: "게시글",
  followers: "팔로워",
  following: "팔로잉",
};

function ProfileMiniCard({ profile }: { profile: ProfileResponse }) {
  return (
    <div className="flex items-center gap-3 p-4">
      <div className="h-8 w-8 rounded-full bg-primary/10 flex items-center justify-center text-sm font-bold text-primary shrink-0">
        {profile.nickname[0]?.toUpperCase() ?? "A"}
      </div>
      <Link
        href={`/profiles/${profile.id}`}
        className="text-sm font-medium hover:underline"
      >
        {profile.nickname}
      </Link>
    </div>
  );
}

interface Props {
  params: Promise<{ profileId: string }>;
}

export default function ProfilePage({ params }: Props) {
  const { profileId: profileIdStr } = use(params);
  const profileId = Number(profileIdStr);
  const [activeTab, setActiveTab] = useState<ProfileTab>("posts");

  const {
    data: profile,
    isLoading,
    isError,
  } = useQuery({
    queryKey: ["profile", profileId],
    queryFn: () => profilesApi.getProfile(profileId),
    enabled: !isNaN(profileId),
  });

  const { data: postsData, isLoading: postsLoading } = useQuery({
    queryKey: ["profile-posts", profileId],
    queryFn: () => profilesApi.getProfilePosts(profileId),
    enabled: activeTab === "posts" && !isNaN(profileId),
    retry: false,
  });

  const { data: followersData, isLoading: followersLoading } = useQuery({
    queryKey: ["profile-followers", profileId],
    queryFn: () => profilesApi.getFollowers(profileId),
    enabled: activeTab === "followers" && !isNaN(profileId),
  });

  const { data: followingData, isLoading: followingLoading } = useQuery({
    queryKey: ["profile-following", profileId],
    queryFn: () => profilesApi.getFollowing(profileId),
    enabled: activeTab === "following" && !isNaN(profileId),
  });

  if (isNaN(profileId)) {
    return (
      <div className="p-8 text-center text-muted-foreground">
        잘못된 프로필 주소입니다.
      </div>
    );
  }

  if (isLoading) {
    return <div className="p-8 text-center text-muted-foreground">로딩 중...</div>;
  }

  if (isError || !profile) {
    return (
      <div className="p-8 text-center text-muted-foreground">
        프로필을 불러올 수 없습니다.
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto">
      <ProfileHeader profile={profile} />

      {/* Tabs */}
      <div className="flex border-b sticky top-14 z-30 bg-background">
        {(["posts", "followers", "following"] as ProfileTab[]).map((tab) => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={`px-6 py-3 text-sm font-medium transition-colors ${
              activeTab === tab
                ? "border-b-2 border-primary text-primary"
                : "text-muted-foreground hover:text-foreground"
            }`}
          >
            {TAB_LABELS[tab]}
          </button>
        ))}
      </div>

      {/* Tab Content */}
      <div className="divide-y">
        {activeTab === "posts" && (
          <>
            {postsLoading ? (
              <div className="p-8 text-center text-muted-foreground">
                로딩 중...
              </div>
            ) : !postsData || postsData.content.length === 0 ? (
              <p className="p-8 text-center text-muted-foreground">
                게시글이 없습니다.
              </p>
            ) : (
              <div className="flex flex-col gap-3 p-4">
                {postsData.content.map((post) => (
                  <PostCard key={post.id} post={post} />
                ))}
              </div>
            )}
          </>
        )}

        {activeTab === "followers" && (
          <>
            {followersLoading ? (
              <div className="p-8 text-center text-muted-foreground">
                로딩 중...
              </div>
            ) : !followersData || followersData.content.length === 0 ? (
              <p className="p-8 text-center text-muted-foreground">
                팔로워가 없습니다.
              </p>
            ) : (
              followersData.content.map((p) => (
                <ProfileMiniCard key={p.id} profile={p} />
              ))
            )}
          </>
        )}

        {activeTab === "following" && (
          <>
            {followingLoading ? (
              <div className="p-8 text-center text-muted-foreground">
                로딩 중...
              </div>
            ) : !followingData || followingData.content.length === 0 ? (
              <p className="p-8 text-center text-muted-foreground">
                팔로잉이 없습니다.
              </p>
            ) : (
              followingData.content.map((p) => (
                <ProfileMiniCard key={p.id} profile={p} />
              ))
            )}
          </>
        )}
      </div>
    </div>
  );
}
