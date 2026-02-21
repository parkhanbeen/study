package com.anoncommunity.app.api.config

/**
 * Holds per-request language/country context propagated from the API Gateway.
 * Thread-local scoped, cleared after each request by LanguageContextFilter.
 */
object LanguageContext {

    private val languageHolder = ThreadLocal<String>()
    private val countryHolder = ThreadLocal<String>()

    fun setLanguage(language: String) = languageHolder.set(language)
    fun setCountry(country: String) = countryHolder.set(country)

    fun getLanguage(): String = languageHolder.get() ?: DEFAULT_LANGUAGE
    fun getCountry(): String = countryHolder.get() ?: DEFAULT_COUNTRY

    fun clear() {
        languageHolder.remove()
        countryHolder.remove()
    }

    private const val DEFAULT_LANGUAGE = "ko"
    private const val DEFAULT_COUNTRY = "KR"
}
