package com.anoncommunity.contract.event

interface ProcessedEventPort {
    /** Returns true if the event was newly marked as processed, false if already processed. */
    fun markProcessed(eventId: String, eventType: String): Boolean
}
