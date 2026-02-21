"use client";

import { ActiveProfile } from "@/types/profile";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";

interface ProfileSwitcherProps {
  profiles: ActiveProfile[];
  currentProfileId: number | null;
  onSwitch: (profileId: number) => void;
}

export function ProfileSwitcher({ profiles, currentProfileId, onSwitch }: ProfileSwitcherProps) {
  return (
    <div className="space-y-2">
      {profiles.map((profile) => (
        <div key={profile.id} className="flex items-center justify-between py-2 border-b last:border-0">
          <div className="flex items-center gap-2">
            <span className="text-sm font-medium">{profile.nickname}</span>
            {profile.isDefault && <Badge variant="secondary" className="text-xs">기본</Badge>}
            {profile.id === currentProfileId && <Badge className="text-xs">현재</Badge>}
          </div>
          <Button
            size="sm"
            variant="outline"
            disabled={profile.id === currentProfileId}
            onClick={() => onSwitch(profile.id)}
          >
            전환
          </Button>
        </div>
      ))}
    </div>
  );
}
