package de.test.driverdistractionapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Car API
import android.car.Car
import android.car.drivingstate.CarUxRestrictions
import android.car.drivingstate.CarUxRestrictionsManager

class MainActivity : ComponentActivity() {
    private var car: Car? = null
    private var uxManager: CarUxRestrictionsManager? = null

    // Compose-bound state that is updated through Car listener
    private val blockedState = mutableStateOf<Set<AppFunction>>(emptySet())

    private val uxListener = CarUxRestrictionsManager.OnUxRestrictionsChangedListener { restrictions ->
        blockedState.value = mapRestrictionsToBlockedFunctions(restrictions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Try to establish connection to Car API (if available)
        tryConnectCar()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DistractionOptimizedScreen(blocked = blockedState)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleanup
        uxManager?.unregisterListener()
        uxManager = null
        car?.disconnect()
        car = null
    }

    private fun tryConnectCar() {
        runCatching {
            Car.createCar(this, null, Car.CAR_WAIT_TIMEOUT_WAIT_FOREVER) { c, ready ->
                if (ready) {
                    car = c
                    val manager = c.getCarManager(Car.CAR_UX_RESTRICTION_SERVICE) as? CarUxRestrictionsManager
                    uxManager = manager
                    if (manager != null) {
                        // Set initial state
                        val currentRestrictions = manager.currentCarUxRestrictions
                        blockedState.value = mapRestrictionsToBlockedFunctions(currentRestrictions)
                        // Register listener
                        manager.registerListener(uxListener)
                    }
                }
            }
        }.onFailure {
            // No Automotive framework available or no permission -> Fallback: no blocking
            blockedState.value = emptySet()
        }
    }

    private fun mapRestrictionsToBlockedFunctions(restrictions: CarUxRestrictions?): Set<AppFunction> {
        if (restrictions == null) return emptySet()
        val flags = restrictions.activeRestrictions
        val baseRestricted = restrictions.isRequiresDistractionOptimization

        // If no distraction optimization required, no blocking
        if (!baseRestricted) return emptySet()

        // Collect all blocked functions based on active flags
        val blockedFunctions = mutableSetOf<AppFunction>()

        if ((flags and CarUxRestrictions.UX_RESTRICTIONS_NO_DIALPAD) != 0) {
            blockedFunctions.add(AppFunction.CALL)
        }
        if ((flags and CarUxRestrictions.UX_RESTRICTIONS_NO_FILTERING) != 0) {
            blockedFunctions.add(AppFunction.MESSAGE)
        }
        if ((flags and CarUxRestrictions.UX_RESTRICTIONS_NO_VIDEO) != 0) {
            blockedFunctions.add(AppFunction.VIDEO)
        }
        if ((flags and CarUxRestrictions.UX_RESTRICTIONS_NO_KEYBOARD) != 0) {
            blockedFunctions.add(AppFunction.KEYBOARD)
        }
        if ((flags and CarUxRestrictions.UX_RESTRICTIONS_LIMIT_STRING_LENGTH) != 0) {
            blockedFunctions.add(AppFunction.LIMIT_STRING_LENGTH)
        }

        return blockedFunctions
    }
}

private enum class AppFunction(val labelResId: Int) {
    CALL(R.string.function_call),
    MESSAGE(R.string.function_message),
    VIDEO(R.string.function_video),
    KEYBOARD(R.string.function_keyboard),
    LIMIT_STRING_LENGTH(R.string.function_limit_string_length);
}

@Composable
private fun DistractionOptimizedScreen(blocked: State<Set<AppFunction>>) {
    val currentBlocked by blocked

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (currentBlocked.isEmpty()) {
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.no_function_blocked),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.safety_mode_active),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Text(
                text = androidx.compose.ui.res.stringResource(R.string.functions_blocked),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            currentBlocked.forEach { function ->
                Text(
                    text = "• ${androidx.compose.ui.res.stringResource(function.labelResId)}",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // No manual buttons anymore – the app reacts exclusively to Car API
        Text(
            text = androidx.compose.ui.res.stringResource(R.string.status_auto_updated),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Long text with automatic truncation based on UX_RESTRICTIONS_LIMIT_STRING_LENGTH
        val fullText = androidx.compose.ui.res.stringResource(R.string.info_text_long)

        val displayText = if (currentBlocked.contains(AppFunction.LIMIT_STRING_LENGTH)) {
            // When string length is limited, truncate to a reasonable length
            val maxLength = 120 // Default reasonable limit for driving mode
            if (fullText.length > maxLength) {
                fullText.take(maxLength - 3) + "..."
            } else {
                fullText
            }
        } else {
            fullText
        }

        Text(
            text = displayText,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DistractionOptimizedScreenPreview() {
    MaterialTheme {
        // Preview simulates no Car status
        val previewState = remember { mutableStateOf<Set<AppFunction>>(emptySet()) }
        DistractionOptimizedScreen(blocked = previewState)
    }
}
