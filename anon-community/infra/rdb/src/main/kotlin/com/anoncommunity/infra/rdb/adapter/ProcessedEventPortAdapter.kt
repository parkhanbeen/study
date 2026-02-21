package com.anoncommunity.infra.rdb.adapter

import com.anoncommunity.contract.event.ProcessedEventPort
import com.anoncommunity.infra.rdb.repository.ProcessedEventJpaRepository
import org.springframework.stereotype.Component

@Component
class ProcessedEventPortAdapter(
    private val processedEventRepository: ProcessedEventJpaRepository,
) : ProcessedEventPort {

    override fun markProcessed(eventId: String, eventType: String): Boolean {
        return processedEventRepository.insertIgnore(eventId, eventType) > 0
    }
}
