package com.anoncommunity.infra.rdb.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "processed_events")
class ProcessedEventEntity(
    @Id
    @Column(name = "event_id", length = 36)
    val eventId: String = "",

    @Column(name = "event_type", nullable = false, length = 100)
    val eventType: String = "",

    @Column(name = "processed_at", nullable = false)
    val processedAt: Instant = Instant.now(),
)
