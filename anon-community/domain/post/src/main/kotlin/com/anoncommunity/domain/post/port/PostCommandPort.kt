package com.anoncommunity.domain.post.port

import com.anoncommunity.contract.model.PostId
import com.anoncommunity.domain.post.Post

interface PostCommandPort {
    fun save(post: Post): Post
    fun delete(id: PostId)
}
