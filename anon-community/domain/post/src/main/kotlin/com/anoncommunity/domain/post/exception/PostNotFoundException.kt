package com.anoncommunity.domain.post.exception

import com.anoncommunity.contract.model.PostId

class PostNotFoundException(
    val postId: PostId,
) : RuntimeException("Post not found: ${postId.value}")
