package com.glovo.test.data.models

import com.squareup.moshi.Json

data class City(
    val code: String,
    val name: String,
    @field:Json(name = "country_code")
    val countryCode: String,
    val currency: String? = null,
    val enabled: Boolean? = null,
    val busy: Boolean? = null,
    @field:Json(name = "time_zone")
    val timeZone: String? = null,
    @field:Json(name = "language_code")
    val languageCode: String? = null,
    @field:Json(name = "working_area")
    val workingArea: List<String>
)