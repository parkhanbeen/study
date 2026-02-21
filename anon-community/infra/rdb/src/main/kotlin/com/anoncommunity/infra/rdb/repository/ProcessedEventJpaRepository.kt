package com.anoncommunity.infra.rdb.repository

import com.anoncommunity.infra.rdb.entity.ProcessedEventEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ProcessedEventJpaRepository : JpaRepository<ProcessedEventEntity, String> {

    @Modifying
    @Query(
        nativeQuery = true,
        value = "INSERT IGNORE INTO processed_events (event_id, event_type, processed_at) VALUES (:eventId, :eventType, NOW())",
    )
    fun insertIgnore(@Param("eventId") eventId: String, @Param("eventType") eventType: String): Int
}
