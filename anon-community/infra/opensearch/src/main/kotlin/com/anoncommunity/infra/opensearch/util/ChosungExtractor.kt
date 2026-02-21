package com.anoncommunity.infra.opensearch.util

/**
 * Extracts Korean initial consonants (초성) from text.
 * Non-Korean characters (spaces, English, numbers, punctuation) are ignored.
 *
 * Example: "무신사" → "ㅁㅅㅅ"
 * Example: "Hello 무신사" → "ㅁㅅㅅ"
 */
object ChosungExtractor {

    private val CHOSUNG = charArrayOf(
        'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ',
        'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ',
    )
    private const val HANGUL_BASE = 0xAC00  // '가'
    private const val HANGUL_END = 0xD7A3   // '힣'
    private const val JUNGSEONG_COUNT = 21
    private const val JONGSEONG_COUNT = 28

    fun extractChosung(text: String): String =
        text
            .filter { it.code in HANGUL_BASE..HANGUL_END }
            .map { ch -> CHOSUNG[(ch.code - HANGUL_BASE) / (JUNGSEONG_COUNT * JONGSEONG_COUNT)] }
            .joinToString("")
}
