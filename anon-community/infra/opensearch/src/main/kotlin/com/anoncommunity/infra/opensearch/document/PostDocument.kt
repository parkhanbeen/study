package com.anoncommunity.infra.opensearch.document

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
data class PostDocument(
    val id: Long,
    val authorProfileId: Long,
    val authorNickname: String = "",
    val title: String,
    val content: String,
    val category: String = "DAILY",
    val tags: List<String>,
    val status: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val commentCount: Int = 0,
    val likeCount: Int = 0,
    val viewCount: Int = 0,
    val titleChosung: String = "",
    val contentChosung: String = "",
)
