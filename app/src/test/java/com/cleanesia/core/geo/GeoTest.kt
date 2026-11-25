package com.cleanesia.core.geo

import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoTest {
    @Test
    fun cellSizeMatchesFixture() {
        assertEquals(GeoFixture.root["cellSizeDeg"]!!.jsonPrimitive.double, CELL_SIZE_DEG, 0.0)
    }

    @Test
    fun cellIsReplacedOnlyByNewerReport() {
        assertTrue(shouldReplaceCell(currentLatestAt = null, incomingCreatedAt = 1_000))
        assertTrue(shouldReplaceCell(currentLatestAt = 1_000, incomingCreatedAt = 2_000))
        assertFalse(shouldReplaceCell(currentLatestAt = 2_000, incomingCreatedAt = 1_000))
        assertFalse(shouldReplaceCell(currentLatestAt = 1_000, incomingCreatedAt = 1_000))
    }
}
