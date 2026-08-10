package com.os.cvCamera.features

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the resolution picker labels. Runs on the host JVM.
 */
class SizeLabelTest {
    @Test
    fun aspectRatio_reducesToSimplestForm() {
        assertEquals("16:9", SizeLabel.aspectRatio(1920, 1080))
        assertEquals("4:3", SizeLabel.aspectRatio(4000, 3000))
        assertEquals("1:1", SizeLabel.aspectRatio(2992, 2992))
        assertEquals("4:3", SizeLabel.aspectRatio(4080, 3060))
    }

    @Test
    fun aspectRatio_handlesRatiosThatDoNotReduceNeatly() {
        assertEquals("292:135", SizeLabel.aspectRatio(2336, 1080))
    }

    @Test
    fun megaPixels_roundsToOneDecimal() {
        assertEquals("2.1 MP", SizeLabel.megaPixels(1920, 1080))
        assertEquals("12.0 MP", SizeLabel.megaPixels(4000, 3000))
        assertEquals("0.7 MP", SizeLabel.megaPixels(960, 720))
        assertEquals("12.5 MP", SizeLabel.megaPixels(4080, 3060))
    }

    @Test
    fun describe_containsDimensionsRatioAndMegaPixels() {
        val label = SizeLabel.describe(1920, 1080)
        assertTrue(label.contains("1920"))
        assertTrue(label.contains("1080"))
        assertTrue(label.contains("16:9"))
        assertTrue(label.contains("2.1 MP"))
    }

    @Test
    fun greatestCommonDivisor_neverReturnsZero() {
        // A zero dimension must not cause a division by zero in aspectRatio.
        assertEquals(1, SizeLabel.greatestCommonDivisor(0, 0))
        assertEquals(5, SizeLabel.greatestCommonDivisor(0, 5))
    }

    @Test
    fun aspectRatio_survivesAZeroDimension() {
        assertEquals("0:1", SizeLabel.aspectRatio(0, 1))
    }
}
