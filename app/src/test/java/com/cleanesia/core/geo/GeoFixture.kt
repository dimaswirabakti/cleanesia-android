package com.cleanesia.core.geo

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/** Fixture paritas geo yang sama dengan versi Flutter (shared/geo-fixtures.json). */
object GeoFixture {
    val root: JsonObject by lazy {
        val text = GeoFixture::class.java.classLoader!!.getResource("geo-fixtures.json")!!.readText()
        Json.parseToJsonElement(text).jsonObject
    }
}
