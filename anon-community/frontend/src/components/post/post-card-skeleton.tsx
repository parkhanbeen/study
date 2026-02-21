import { Card, CardContent, CardFooter, CardHeader } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";

export function PostCardSkeleton() {
  return (
    <Card>
      <CardHeader className="pb-2">
        {/* Author row: avatar · name · time · category badge */}
        <div className="flex items-center gap-2">
          <Skeleton className="h-5 w-5 rounded-full" />
          <Skeleton className="h-3 w-28" />
          <Skeleton className="ml-auto h-5 w-14 rounded-md" />
        </div>
        {/* Title */}
        <Skeleton className="mt-2 h-5 w-3/4" />
      </CardHeader>
      <CardContent className="pb-2">
        {/* Content preview (2 lines) */}
        <Skeleton className="h-4 w-full" />
        <Skeleton className="mt-1 h-4 w-2/3" />
      </CardContent>
      {/* Stats footer: likes · comments · views */}
      <CardFooter className="gap-4">
        <Skeleton className="h-3 w-10" />
        <Skeleton className="h-3 w-10" />
        <Skeleton className="h-3 w-10" />
      </CardFooter>
    </Card>
  );
}
