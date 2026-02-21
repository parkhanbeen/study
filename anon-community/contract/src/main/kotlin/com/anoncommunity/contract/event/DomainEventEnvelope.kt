package com.anoncommunity.contract.event

data class DomainEventEnvelope(
    val events: List<DomainEvent>,
) {
    constructor(vararg events: DomainEvent) : this(events.toList())
}
