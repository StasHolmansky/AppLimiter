package com.stas.applimiter.ui.theme

data class AppLanguage(
    val code: String,
    val label: String,
)

val SUPPORTED_LANGUAGES = listOf(
    AppLanguage("en", "English"),
    AppLanguage("ru", "Русский"),
    AppLanguage("es", "Español"),
    AppLanguage("pt-BR", "Português (Brasil)"),
    AppLanguage("fr", "Français"),
    AppLanguage("de", "Deutsch"),
)

fun resolveLanguage(deviceLocale: String): String {
    val normalized = deviceLocale.replace('_', '-')
    val codes = SUPPORTED_LANGUAGES.map { it.code }
    if (normalized in codes) return normalized
    val base = normalized.substringBefore('-')
    return codes.find { it == base || it.startsWith("$base-") } ?: "en"
}
