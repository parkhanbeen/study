package com.anoncommunity.contract

import com.anoncommunity.contract.event.DomainEvent

abstract class AggregateRoot {
    private val domainEvents = mutableListOf<DomainEvent>()

    protected fun recordDomainEvent(event: DomainEvent) {
        domainEvents.add(event)
    }

    fun pullDomainEvents(): List<DomainEvent> = domainEvents.toList().also {
        domainEvents.clear()
    }
}
