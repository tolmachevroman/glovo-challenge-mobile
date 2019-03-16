package com.glovo.test.data.models

import com.squareup.moshi.Json

data class City(
    val code: String,
    val name: String,
    @field:Json(name = "country_code")
    val countryCode: String,
    val currency: String?,
    val enabled: Boolean?,
    val busy: Boolean?,
    @field:Json(name = "time_zone")
    val timeZone: String?,
    @field:Json(name = "language_code")
    val languageCode: String?,
    @field:Json(name = "working_area")
    val workingArea: List<String>
)