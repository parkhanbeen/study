package com.anoncommunity.domain.post

@JvmInline
value class PostTitle(val value: String) {
    init {
        require(value.isNotBlank()) { "Post title must not be blank" }
        require(value.length <= MAX_LENGTH) { "Post title must not exceed $MAX_LENGTH characters" }
    }

    companion object {
        const val MAX_LENGTH = 200
    }
}

@JvmInline
value class PostContent(val value: String) {
    init {
        require(value.isNotBlank()) { "Post content must not be blank" }
        require(value.length <= MAX_LENGTH) { "Post content must not exceed $MAX_LENGTH characters" }
    }

    companion object {
        const val MAX_LENGTH = 10_000
    }
}

@JvmInline
value class Tag(val value: String) {
    init {
        require(value.isNotBlank()) { "Tag must not be blank" }
        require(value.length <= MAX_LENGTH) { "Tag must not exceed $MAX_LENGTH characters" }
        require(!value.contains(' ')) { "Tag must not contain spaces" }
    }

    companion object {
        const val MAX_LENGTH = 30
    }
}
