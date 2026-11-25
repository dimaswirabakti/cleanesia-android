package com.cleanesia.core.geo

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Fixture paritas geo yang sama dengan versi Flutter. */
class GeoTest {

    private val fixture: JsonObject by lazy {
        val text = javaClass.classLoader!!.getResource("geo-fixtures.json")!!.readText()
        Json.parseToJsonElement(text).jsonObject
    }

    @Test
    fun cellSizeMatchesFixture() {
        assertEquals(fixture["cellSizeDeg"]!!.jsonPrimitive.double, CELL_SIZE_DEG, 0.0)
    }

    @Test
    fun computeCellIdMatchesFixture() {
        val cases = fixture["computeCellId"]!!.jsonArray
        assertTrue(cases.isNotEmpty())
        for (case in cases) {
            val c = case.jsonObject
            val lat = c["lat"]!!.jsonPrimitive.double
            val lng = c["lng"]!!.jsonPrimitive.double
            val expected = c["cellId"]!!.jsonPrimitive.content
            assertEquals("($lat, $lng)", expected, computeCellId(lat, lng))
        }
    }

    @Test
    fun cellBoundsMatchesFixture() {
        val cases = fixture["cellBounds"]!!.jsonArray
        assertTrue(cases.isNotEmpty())
        for (case in cases) {
            val c = case.jsonObject
            val cellId = c["cellId"]!!.jsonPrimitive.content
            val b = cellBounds(cellId)
            // Pembandingan eksak: hasil aritmetika double harus identik dengan Dart.
            assertEquals("$cellId latLo", c["latLo"]!!.jsonPrimitive.double, b.latLo, 0.0)
            assertEquals("$cellId latHi", c["latHi"]!!.jsonPrimitive.double, b.latHi, 0.0)
            assertEquals("$cellId lngLo", c["lngLo"]!!.jsonPrimitive.double, b.lngLo, 0.0)
            assertEquals("$cellId lngHi", c["lngHi"]!!.jsonPrimitive.double, b.lngHi, 0.0)
        }
    }

    @Test
    fun cellIsReplacedOnlyByNewerReport() {
        assertTrue(shouldReplaceCell(currentLatestAt = null, incomingCreatedAt = 1_000))
        assertTrue(shouldReplaceCell(currentLatestAt = 1_000, incomingCreatedAt = 2_000))
        assertFalse(shouldReplaceCell(currentLatestAt = 2_000, incomingCreatedAt = 1_000))
        assertFalse(shouldReplaceCell(currentLatestAt = 1_000, incomingCreatedAt = 1_000))
    }
}
