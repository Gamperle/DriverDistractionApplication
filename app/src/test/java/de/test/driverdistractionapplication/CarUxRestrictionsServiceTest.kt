package de.test.driverdistractionapplication

import android.car.drivingstate.CarUxRestrictions
import org.junit.Test
import org.junit.Assert.*

/**
 * Sample Unit Tests for Car UX Restrictions Service.
 *
 * These tests demonstrate how to verify the Car UX Restrictions logic,
 * focusing on bitwise flag operations and string truncation functionality
 * that powers the distraction optimization features.
 *
 * Run with: ./gradlew test
 */
class CarUxRestrictionsServiceTest {

    /**
     * Verifies that CarUxRestrictions flag constants have the expected values.
     * These are bitwise flags used to indicate different types of UX restrictions.
     */
    @Test
    fun testRestrictionFlagConstants() {
        assertEquals("NO_DIALPAD flag value", 1, CarUxRestrictions.UX_RESTRICTIONS_NO_DIALPAD)
        assertEquals("NO_FILTERING flag value", 2, CarUxRestrictions.UX_RESTRICTIONS_NO_FILTERING)
        assertEquals("LIMIT_STRING_LENGTH flag value", 4, CarUxRestrictions.UX_RESTRICTIONS_LIMIT_STRING_LENGTH)
        assertEquals("NO_KEYBOARD flag value", 8, CarUxRestrictions.UX_RESTRICTIONS_NO_KEYBOARD)
        assertEquals("NO_VIDEO flag value", 16, CarUxRestrictions.UX_RESTRICTIONS_NO_VIDEO)
    }

    /**
     * Tests detection of a single restriction flag using bitwise AND operation.
     * Simulates how the app checks if a specific restriction is active.
     */
    @Test
    fun testSingleFlagDetection() {
        val activeRestrictions = CarUxRestrictions.UX_RESTRICTIONS_NO_DIALPAD
        
        assertTrue("Should detect NO_DIALPAD",
            (activeRestrictions and CarUxRestrictions.UX_RESTRICTIONS_NO_DIALPAD) != 0)
        assertFalse("Should not detect NO_FILTERING",
            (activeRestrictions and CarUxRestrictions.UX_RESTRICTIONS_NO_FILTERING) != 0)
    }

    /**
     * Tests detection of multiple simultaneous restriction flags.
     * This is a key feature - restrictions are not mutually exclusive.
     */
    @Test
    fun testMultipleFlagsDetection() {
        // Combine multiple flags using bitwise OR
        val activeRestrictions = 
            CarUxRestrictions.UX_RESTRICTIONS_NO_DIALPAD or 
            CarUxRestrictions.UX_RESTRICTIONS_NO_VIDEO or
            CarUxRestrictions.UX_RESTRICTIONS_LIMIT_STRING_LENGTH
        
        // Verify each flag is detected
        assertTrue("Should detect NO_DIALPAD",
            (activeRestrictions and CarUxRestrictions.UX_RESTRICTIONS_NO_DIALPAD) != 0)
        assertTrue("Should detect NO_VIDEO",
            (activeRestrictions and CarUxRestrictions.UX_RESTRICTIONS_NO_VIDEO) != 0)
        assertTrue("Should detect LIMIT_STRING_LENGTH",
            (activeRestrictions and CarUxRestrictions.UX_RESTRICTIONS_LIMIT_STRING_LENGTH) != 0)
        
        // Verify non-set flags are not detected
        assertFalse("Should not detect NO_KEYBOARD",
            (activeRestrictions and CarUxRestrictions.UX_RESTRICTIONS_NO_KEYBOARD) != 0)
        assertFalse("Should not detect NO_FILTERING",
            (activeRestrictions and CarUxRestrictions.UX_RESTRICTIONS_NO_FILTERING) != 0)
    }

    /**
     * Tests that all restriction flags can be combined simultaneously.
     */
    @Test
    fun testAllFlagsCombined() {
        val allFlags = CarUxRestrictions.UX_RESTRICTIONS_NO_DIALPAD or
                CarUxRestrictions.UX_RESTRICTIONS_NO_FILTERING or
                CarUxRestrictions.UX_RESTRICTIONS_LIMIT_STRING_LENGTH or
                CarUxRestrictions.UX_RESTRICTIONS_NO_KEYBOARD or
                CarUxRestrictions.UX_RESTRICTIONS_NO_VIDEO
        
        // All flags should be detectable
        assertTrue((allFlags and CarUxRestrictions.UX_RESTRICTIONS_NO_DIALPAD) != 0)
        assertTrue((allFlags and CarUxRestrictions.UX_RESTRICTIONS_NO_FILTERING) != 0)
        assertTrue((allFlags and CarUxRestrictions.UX_RESTRICTIONS_LIMIT_STRING_LENGTH) != 0)
        assertTrue((allFlags and CarUxRestrictions.UX_RESTRICTIONS_NO_KEYBOARD) != 0)
        assertTrue((allFlags and CarUxRestrictions.UX_RESTRICTIONS_NO_VIDEO) != 0)
    }

    /**
     * Tests that no flags are detected when restrictions are zero.
     */
    @Test
    fun testNoFlagsActive() {
        val noFlags = 0
        
        assertFalse((noFlags and CarUxRestrictions.UX_RESTRICTIONS_NO_DIALPAD) != 0)
        assertFalse((noFlags and CarUxRestrictions.UX_RESTRICTIONS_NO_FILTERING) != 0)
        assertFalse((noFlags and CarUxRestrictions.UX_RESTRICTIONS_LIMIT_STRING_LENGTH) != 0)
        assertFalse((noFlags and CarUxRestrictions.UX_RESTRICTIONS_NO_KEYBOARD) != 0)
        assertFalse((noFlags and CarUxRestrictions.UX_RESTRICTIONS_NO_VIDEO) != 0)
    }

    /**
     * Tests string truncation logic for UX_RESTRICTIONS_LIMIT_STRING_LENGTH.
     * When this restriction is active, long text must be truncated to prevent distraction.
     */
    @Test
    fun testStringTruncation_ExceedsLimit() {
        val longText = "This is a very long informational text that exceeds the maximum allowed length"
        val maxLength = 50
        
        // Simulate truncation logic
        val truncated = if (longText.length > maxLength) {
            longText.take(maxLength - 3) + "..."
        } else {
            longText
        }
        
        assertEquals("Length should be exactly maxLength", maxLength, truncated.length)
        assertTrue("Should end with ellipsis", truncated.endsWith("..."))
        assertTrue("Should start with original text", longText.startsWith(truncated.take(47)))
    }

    /**
     * Tests that short text is not truncated.
     */
    @Test
    fun testStringTruncation_WithinLimit() {
        val shortText = "Short text"
        val maxLength = 50
        
        val result = if (shortText.length > maxLength) {
            shortText.take(maxLength - 3) + "..."
        } else {
            shortText
        }
        
        assertEquals("Short text should remain unchanged", shortText, result)
    }

    /**
     * Tests edge case: text exactly at the limit should not be truncated.
     */
    @Test
    fun testStringTruncation_ExactLimit() {
        val text = "X".repeat(50)
        val maxLength = 50
        
        val result = if (text.length > maxLength) {
            text.take(maxLength - 3) + "..."
        } else {
            text
        }
        
        assertEquals("Text at exact limit should not be truncated", text, result)
    }

    /**
     * Tests edge case: text one character over the limit.
     */
    @Test
    fun testStringTruncation_OnePastLimit() {
        val text = "X".repeat(51)
        val maxLength = 50
        
        val result = if (text.length > maxLength) {
            text.take(maxLength - 3) + "..."
        } else {
            text
        }
        
        assertEquals("Should truncate to maxLength", maxLength, result.length)
        assertTrue("Should have ellipsis", result.endsWith("..."))
    }

    /**
     * Verifies that restriction flags are proper bitmasks (powers of 2).
     * This is a requirement for bitwise operations to work correctly.
     */
    @Test
    fun testFlagsArePowersOfTwo() {
        val flags = listOf(
            CarUxRestrictions.UX_RESTRICTIONS_NO_DIALPAD,
            CarUxRestrictions.UX_RESTRICTIONS_NO_FILTERING,
            CarUxRestrictions.UX_RESTRICTIONS_LIMIT_STRING_LENGTH,
            CarUxRestrictions.UX_RESTRICTIONS_NO_KEYBOARD,
            CarUxRestrictions.UX_RESTRICTIONS_NO_VIDEO
        )
        
        flags.forEach { flag ->
            // A number is a power of 2 if: n > 0 and (n & (n-1)) == 0
            assertTrue("Flag $flag should be power of 2",
                flag > 0 && (flag and (flag - 1)) == 0)
        }
    }

    /**
     * Tests the default max string length used in the application.
     */
    @Test
    fun testDefaultMaxStringLength() {
        val defaultMaxLength = 120
        
        assertTrue("Default should be positive", defaultMaxLength > 0)
        assertTrue("Default should be reasonable for driving mode", defaultMaxLength >= 100)
        assertTrue("Default should not be too restrictive", defaultMaxLength <= 200)
    }
}
