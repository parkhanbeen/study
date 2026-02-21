package com.anoncommunity.app.usecase.search

import com.anoncommunity.domain.post.port.PostSearchPort
import org.springframework.stereotype.Service

@Service
class GetHotTagsUseCase(
    private val postSearchPort: PostSearchPort,
) {
    operator fun invoke(limit: Int = 10): List<String> {
        return postSearchPort.getHotTags(limit)
    }
}
