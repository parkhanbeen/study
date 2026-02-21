package com.anoncommunity.infra.opensearch.index

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class PostIndexMappingSpec : DescribeSpec({
    describe("PostIndexMapping") {
        it("should use posts-v5 as index name") {
            PostIndexMapping.INDEX_NAME shouldBe "posts-v5"
        }

        it("should include likeCount field with integer type in mappings") {
            PostIndexMapping.INDEX_SETTINGS shouldContain "\"likeCount\""
        }

        it("should include viewCount field with integer type in mappings") {
            PostIndexMapping.INDEX_SETTINGS shouldContain "\"viewCount\""
        }

        it("should retain commentCount field in mappings") {
            PostIndexMapping.INDEX_SETTINGS shouldContain "\"commentCount\""
        }
    }
})
