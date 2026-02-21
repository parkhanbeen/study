package com.anoncommunity.contract.model

@JvmInline
value class PostId(val value: Long) {
    companion object {
        val UNSAVED = PostId(0L)
    }
}

@JvmInline
value class MemberId(val value: Long) {
    companion object {
        val UNSAVED = MemberId(0L)
    }
}

@JvmInline
value class CommentId(val value: Long) {
    companion object {
        val UNSAVED = CommentId(0L)
    }
}

@JvmInline
value class PostLikeId(val value: Long) {
    companion object {
        val UNSAVED = PostLikeId(0L)
    }
}

// ProfileId — contract layer (same as PostId/MemberId)
// ⚠️ No init { require(value > 0) } — UNSAVED sentinel pattern
@JvmInline
value class ProfileId(val value: Long) {
    companion object {
        val UNSAVED = ProfileId(0L)
    }
}

@JvmInline
value class ProfileFollowId(val value: Long) {
    companion object {
        val UNSAVED = ProfileFollowId(0L)
    }
}
