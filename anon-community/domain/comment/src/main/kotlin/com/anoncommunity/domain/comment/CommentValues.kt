package com.anoncommunity.domain.comment

@JvmInline
value class CommentContent(val value: String) {
    init {
        require(value.isNotBlank()) { "Comment content must not be blank" }
        require(value.length <= MAX_LENGTH) { "Comment content must not exceed $MAX_LENGTH characters" }
    }

    companion object {
        const val MAX_LENGTH = 1_000
    }
}

enum class CommentStatus {
    ACTIVE,
    DELETED,
}
