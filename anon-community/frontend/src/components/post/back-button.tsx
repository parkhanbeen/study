"use client";

import { ArrowLeft } from "lucide-react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";

export function BackButton() {
  const router = useRouter();

  const handleBack = () => {
    if (window.history.length <= 1) {
      router.push("/");
    } else {
      router.back();
    }
  };

  return (
    <Button variant="ghost" size="sm" onClick={handleBack}>
      <ArrowLeft className="mr-1 h-4 w-4" /> 뒤로
    </Button>
  );
}
