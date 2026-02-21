package com.anoncommunity.contract.event

interface DomainEventPublisher {
    fun publish(events: List<DomainEvent>)
}
