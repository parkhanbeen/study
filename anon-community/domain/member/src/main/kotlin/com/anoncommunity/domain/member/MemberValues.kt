package com.anoncommunity.domain.member

@JvmInline
value class MemberNickname(val value: String) {
    init {
        require(value.matches(PATTERN)) {
            "Nickname must be 2-20 characters and contain only letters, numbers, CJK characters, dots, underscores, or hyphens"
        }
    }

    companion object {
        // Supports: Latin, CJK (Korean, Japanese, Chinese), dots, underscores, hyphens
        private val PATTERN = Regex("^[\\p{L}\\p{N}._-]{2,20}$")
        const val MIN_LENGTH = 2
        const val MAX_LENGTH = 20
    }
}

@JvmInline
value class Bio(val value: String) {
    init {
        require(value.length <= MAX_LENGTH) { "Bio must not exceed $MAX_LENGTH characters" }
    }

    companion object {
        const val MAX_LENGTH = 200
    }
}

@JvmInline
value class PreferredLanguage(val value: String) {
    init {
        require(value in SUPPORTED_LANGUAGES) {
            "Unsupported language: $value. Supported: $SUPPORTED_LANGUAGES"
        }
    }

    companion object {
        val SUPPORTED_LANGUAGES = setOf("ko", "en", "ja", "zh", "zh-CN", "zh-TW")
    }
}
