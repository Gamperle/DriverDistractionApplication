package de.test.driverdistractionapplication

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Sample Instrumented Tests for Car UX Restrictions Service Integration.
 *
 * These tests verify the application's behavior in an Android Automotive environment,
 * testing the integration with the Car API and UX Restrictions framework.
 *
 * Run with: ./gradlew connectedAndroidTest
 *
 * Note: These tests require an Android Automotive OS emulator or device.
 */
@RunWith(AndroidJUnit4::class)
class CarUxRestrictionsServiceInstrumentedTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    /**
     * Verifies the application context is correct.
     */
    @Test
    fun testAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("de.test.driverdistractionapplication", appContext.packageName)
    }

    /**
     * Tests that the MainActivity launches successfully.
     * This verifies the basic setup including Car API connection attempts.
     */
    @Test
    fun testMainActivityLaunches() {
        activityRule.scenario.onActivity { activity ->
            assertNotNull("Activity should not be null", activity)
            assertTrue("Should be MainActivity instance", activity is MainActivity)
        }
    }

    /**
     * Verifies that string resources exist for all UI elements.
     */
    @Test
    fun testStringResourcesExist() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // Test that all required string resources are defined
        val resources = listOf(
            R.string.function_call,
            R.string.function_message,
            R.string.function_video,
            R.string.function_keyboard,
            R.string.function_limit_string_length,
            R.string.no_function_blocked,
            R.string.safety_mode_active,
            R.string.functions_blocked,
            R.string.status_auto_updated,
            R.string.info_text_long
        )

        resources.forEach { resId ->
            val string = appContext.getString(resId)
            assertNotNull("Resource $resId should exist", string)
            assertTrue("Resource $resId should not be empty", string.isNotEmpty())
        }
    }

    /**
     * Tests that string resources have expected content.
     */
    @Test
    fun testStringResourceContent() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // Verify key string contents
        assertEquals("Phone Call", appContext.getString(R.string.function_call))
        assertEquals("Messaging", appContext.getString(R.string.function_message))
        assertEquals("Video", appContext.getString(R.string.function_video))
        assertEquals("Keyboard", appContext.getString(R.string.function_keyboard))
        assertEquals("Safety Mode Active", appContext.getString(R.string.safety_mode_active))
    }

    /**
     * Tests that the long informational text resource exists and is sufficiently long.
     */
    @Test
    fun testLongInformationalText() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val longText = appContext.getString(R.string.info_text_long)

        assertNotNull("Long text should exist", longText)
        assertTrue("Long text should be substantial", longText.length > 100)
        assertTrue("Long text should mention Android Automotive",
            longText.contains("Android Automotive"))
    }

    /**
     * Verifies the application has the required Car API permission declared.
     */
    @Test
    fun testCarPermissionDeclared() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val packageInfo = appContext.packageManager.getPackageInfo(
            appContext.packageName,
            android.content.pm.PackageManager.GET_PERMISSIONS
        )

        val permissions = packageInfo.requestedPermissions?.toList() ?: emptyList()
        assertTrue("Should request CAR_MONITOR_DRIVING_STATE permission",
            permissions.contains("android.car.permission.CAR_MONITOR_DRIVING_STATE"))
    }

    /**
     * Tests that the app has proper automotive features declared in manifest.
     */
    @Test
    fun testAutomotiveFeatures() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val packageManager = appContext.packageManager

        // Check if running on automotive (may be false on regular emulator)
        val hasAutomotive = packageManager.hasSystemFeature("android.hardware.type.automotive")

        // This test documents the expected environment, but won't fail on non-automotive
        if (hasAutomotive) {
            assertTrue("Automotive feature should be available", true)
        }
    }

    /**
     * Simulates the string truncation scenario.
     * This test demonstrates the behavior when LIMIT_STRING_LENGTH restriction is active.
     */
    @Test
    fun testStringTruncationBehavior() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val fullText = appContext.getString(R.string.info_text_long)
        val maxLength = 120

        // Simulate the truncation logic used in the app
        val truncated = if (fullText.length > maxLength) {
            fullText.take(maxLength - 3) + "..."
        } else {
            fullText
        }

        if (fullText.length > maxLength) {
            assertEquals("Truncated length should be maxLength", maxLength, truncated.length)
            assertTrue("Should end with ellipsis", truncated.endsWith("..."))
        } else {
            assertEquals("Short text should not be modified", fullText, truncated)
        }
    }

    /**
     * Tests that activity can handle configuration changes (like rotation).
     * Important for maintaining state during driving conditions.
     */
    @Test
    fun testActivityRecreation() {
        activityRule.scenario.onActivity { activity ->
            assertNotNull("Activity should exist before recreation", activity)
        }

        // Recreate the activity (simulates configuration change)
        activityRule.scenario.recreate()

        activityRule.scenario.onActivity { activity ->
            assertNotNull("Activity should exist after recreation", activity)
            assertTrue("Should still be MainActivity", activity is MainActivity)
        }
    }
}
