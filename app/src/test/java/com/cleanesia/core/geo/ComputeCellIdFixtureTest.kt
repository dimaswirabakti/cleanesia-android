package com.cleanesia.core.geo

import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** Satu test per kasus computeCellId di fixture. */
@RunWith(Parameterized::class)
class ComputeCellIdFixtureTest(
    @Suppress("unused") private val label: String,
    private val lat: Double,
    private val lng: Double,
    private val expected: String,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases(): List<Array<Any>> =
            GeoFixture.root["computeCellId"]!!.jsonArray.map {
                val c = it.jsonObject
                arrayOf(
                    "(${c["lat"]}, ${c["lng"]}) -> ${c["cellId"]!!.jsonPrimitive.content}",
                    c["lat"]!!.jsonPrimitive.double,
                    c["lng"]!!.jsonPrimitive.double,
                    c["cellId"]!!.jsonPrimitive.content,
                )
            }
    }

    @Test
    fun matchesFixture() {
        assertEquals(expected, computeCellId(lat, lng))
    }
}
