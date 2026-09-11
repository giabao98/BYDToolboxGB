package com.byd.toolkit.gb.data.model

enum class AppLanguage(val code: String, val label: String, val flag: String) {
    VIETNAMESE("vi", "Tiếng Việt", "🇻🇳"),
    ENGLISH("en", "English", "🇺🇸"),
    CHINESE("zh", "中文", "🇨🇳");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: VIETNAMESE
        }
    }
}
