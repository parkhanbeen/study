package com.anoncommunity.contract.event

import java.time.Instant
import java.util.UUID

abstract class DomainEvent(
    open val aggregateId: String,
    val eventId: String = UUID.randomUUID().toString(),
    val occurredAt: Instant = Instant.now(),
    val schemaVersion: Int = 1,
)
