package com.cleanesia.core.geo

import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** Satu test per kasus cellBounds di fixture. Pembandingan eksak (toleransi 0.0). */
@RunWith(Parameterized::class)
class CellBoundsFixtureTest(
    private val cellId: String,
    private val expected: CellBounds,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases(): List<Array<Any>> =
            GeoFixture.root["cellBounds"]!!.jsonArray.map {
                val c = it.jsonObject
                arrayOf(
                    c["cellId"]!!.jsonPrimitive.content,
                    CellBounds(
                        latLo = c["latLo"]!!.jsonPrimitive.double,
                        latHi = c["latHi"]!!.jsonPrimitive.double,
                        lngLo = c["lngLo"]!!.jsonPrimitive.double,
                        lngHi = c["lngHi"]!!.jsonPrimitive.double,
                    ),
                )
            }
    }

    @Test
    fun matchesFixture() {
        val actual = cellBounds(cellId)
        assertEquals("latLo", expected.latLo, actual.latLo, 0.0)
        assertEquals("latHi", expected.latHi, actual.latHi, 0.0)
        assertEquals("lngLo", expected.lngLo, actual.lngLo, 0.0)
        assertEquals("lngHi", expected.lngHi, actual.lngHi, 0.0)
    }
}
