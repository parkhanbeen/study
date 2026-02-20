package com.anoncommunity.infra.opensearch.util

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ChosungExtractorSpec : DescribeSpec({

    describe("ChosungExtractor") {

        context("extractChosung") {
            it("should extract chosung from Korean syllables") {
                ChosungExtractor.extractChosung("무신사") shouldBe "ㅁㅅㅅ"
            }

            it("should extract chosung for compound title") {
                ChosungExtractor.extractChosung("운동 추천") shouldBe "ㅇㄷㅊㅊ"
            }

            it("should ignore non-Korean characters (spaces, English, numbers)") {
                ChosungExtractor.extractChosung("Hello 무신사 2025") shouldBe "ㅁㅅㅅ"
            }

            it("should return empty string for non-Korean text") {
                ChosungExtractor.extractChosung("Hello World") shouldBe ""
            }

            it("should return empty string for empty input") {
                ChosungExtractor.extractChosung("") shouldBe ""
            }

            it("should extract all basic consonants correctly") {
                ChosungExtractor.extractChosung("가나다라마바사아자차카타파하") shouldBe "ㄱㄴㄷㄹㅁㅂㅅㅇㅈㅊㅋㅌㅍㅎ"
            }

            it("should handle double consonant initial (ggiyeok)") {
                ChosungExtractor.extractChosung("까") shouldBe "ㄲ"
            }
        }
    }
})
