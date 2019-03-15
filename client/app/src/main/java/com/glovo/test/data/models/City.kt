package com.glovo.test.data.models

data class City(
    val code: String,
    val name: String,
    val countryCode: String,
    val currency: String?,
    val enabled: Boolean?,
    val busy: Boolean?,
    val timeZone: String?,
    val languageCode: String?,
    val workingArea: List<String>
)