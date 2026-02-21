package com.anoncommunity.domain.post

import com.anoncommunity.domain.post.port.CursorSearchQuery
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class CursorSearchQuerySpec : DescribeSpec({

    describe("CursorSearchQuery") {

        context("default values") {
            it("should default sortBy to createdAt") {
                val query = CursorSearchQuery(keyword = "test", size = 10)
                query.sortBy shouldBe "createdAt"
            }

            it("should default dateRange to null") {
                val query = CursorSearchQuery(keyword = "test", size = 10)
                query.dateRange shouldBe null
            }
        }

        context("custom sortBy values") {
            it("should accept likeCount as sortBy") {
                val query = CursorSearchQuery(keyword = "test", size = 10, sortBy = "likeCount")
                query.sortBy shouldBe "likeCount"
            }

            it("should accept commentCount as sortBy") {
                val query = CursorSearchQuery(keyword = "test", size = 10, sortBy = "commentCount")
                query.sortBy shouldBe "commentCount"
            }

            it("should accept viewCount as sortBy") {
                val query = CursorSearchQuery(keyword = "test", size = 10, sortBy = "viewCount")
                query.sortBy shouldBe "viewCount"
            }
        }

        context("dateRange values") {
            it("should accept today as dateRange") {
                val query = CursorSearchQuery(keyword = "test", size = 10, dateRange = "today")
                query.dateRange shouldBe "today"
            }

            it("should accept this_week as dateRange") {
                val query = CursorSearchQuery(keyword = "test", size = 10, dateRange = "this_week")
                query.dateRange shouldBe "this_week"
            }

            it("should accept this_month as dateRange") {
                val query = CursorSearchQuery(keyword = "test", size = 10, dateRange = "this_month")
                query.dateRange shouldBe "this_month"
            }

            it("should allow null dateRange") {
                val query = CursorSearchQuery(keyword = "test", size = 10, dateRange = null)
                query.dateRange shouldBe null
            }
        }
    }
})
