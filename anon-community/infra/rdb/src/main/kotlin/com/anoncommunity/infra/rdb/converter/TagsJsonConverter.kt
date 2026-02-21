package com.anoncommunity.infra.rdb.converter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class TagsJsonConverter : AttributeConverter<List<String>, String?> {

    override fun convertToDatabaseColumn(tags: List<String>?): String? {
        if (tags.isNullOrEmpty()) return null
        return objectMapper.writeValueAsString(tags)
    }

    override fun convertToEntityAttribute(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return objectMapper.readValue(json, object : TypeReference<List<String>>() {})
    }

    private companion object {
        val objectMapper = jacksonObjectMapper()
    }
}
