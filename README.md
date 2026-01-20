# Driver Distraction Application

An Android Automotive OS application demonstrating the implementation and handling of Car UX Restrictions to ensure driver safety while driving.

## Overview

This application showcases how to build a distraction-optimized Android Automotive app that responds to vehicle UX restrictions in real-time. The app automatically adapts its UI and functionality based on the current driving state, blocking potentially distracting features when the vehicle is in motion.

## Features

- **Real-time UX Restriction Monitoring**: Automatically detects and responds to Car UX Restrictions
- **Multiple Restriction Types Support**: Handles various restriction flags simultaneously:
  - `UX_RESTRICTIONS_NO_DIALPAD` - Phone call blocking
  - `UX_RESTRICTIONS_NO_FILTERING` - Messaging blocking
  - `UX_RESTRICTIONS_NO_VIDEO` - Video playback blocking
  - `UX_RESTRICTIONS_NO_KEYBOARD` - Keyboard input blocking
  - `UX_RESTRICTIONS_LIMIT_STRING_LENGTH` - Text length limitation
- **Dynamic UI Updates**: Visual feedback showing which functions are currently restricted
- **String Length Truncation**: Automatically shortens long text when string length restrictions are active
- **Distraction Optimized**: Fully compliant with Android Automotive distraction optimization requirements

## Architecture

### Components

- **MainActivity**: Main Activity using Jetpack Compose for UI
- **AppFunction Enum**: Represents different app functions that can be restricted
- **RestrictionState**: Data class holding blocked functions information
- **CarUxRestrictionsManager**: Android Automotive system service for monitoring restrictions

### Key Technologies

- **Kotlin**: Primary programming language
- **Jetpack Compose**: Modern UI toolkit
- **Android Automotive OS**: Target platform
- **Car API**: Android Automotive's Car UX Restrictions API

## Project Structure

```
DriverDistractionApplication/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/de/test/driverdistractionapplication/
│   │   │   │   └── MainActivity.kt
│   │   │   ├── res/
│   │   │   │   └── values/
│   │   │   │       └── strings.xml
│   │   │   └── AndroidManifest.xml
│   │   └── androidTest/
│   └── build.gradle.kts
├── gradle/
└── README.md
```

## Requirements

### Hardware
- Android Automotive OS device or emulator
- Vehicle hardware with UX restriction support (or emulator configuration)

### Software
- Android Studio Hedgehog (2023.1.1) or later
- Kotlin 1.9.0 or later
- Android Gradle Plugin 8.2.0 or later
- Target SDK: 34
- Minimum SDK: 28

## Setup and Installation

### 1. Clone the Repository

```bash
git clone <repository-url>
cd DriverDistractionApplication
```

### 2. Open in Android Studio

- Launch Android Studio
- Select "Open an Existing Project"
- Navigate to the project directory
- Click "OK"

### 3. Configure Automotive Emulator (if testing on emulator)

1. Open AVD Manager in Android Studio
2. Create a new Virtual Device
3. Select "Automotive" category
4. Choose a system image with API level 28 or higher
5. Configure UX restrictions in emulator settings

### 4. Build and Run

```bash
./gradlew assembleDebug
```

Or use Android Studio's Run button (Shift + F10)

## Permissions

The application requires the following permission:

```xml
<uses-permission android:name="android.car.permission.CAR_MONITOR_DRIVING_STATE" />
```

This permission allows the app to monitor the vehicle's driving state and receive UX restriction updates.

## Usage

### Running the Application

1. Install the app on an Android Automotive device or emulator
2. Launch the application
3. The app will automatically connect to the Car API
4. Drive or simulate driving state changes
5. Observe how the UI updates to reflect current restrictions

### Testing Restrictions

On Android Automotive emulator:
1. Use the emulator's extended controls
2. Navigate to "Car Data" or "Driving Status"
3. Toggle between "Parked" and "Driving" states
4. Observe the app's response to restriction changes

## How It Works

### 1. Car API Connection

The app establishes a connection to the Car API on startup:

```kotlin
Car.createCar(this, null, Car.CAR_WAIT_TIMEOUT_WAIT_FOREVER) { car, ready ->
    // Get CarUxRestrictionsManager
    val manager = car.getCarManager(Car.CAR_UX_RESTRICTION_SERVICE)
    // Register listener for restriction changes
}
```

### 2. Restriction Monitoring

The app registers a listener to receive UX restriction updates:

```kotlin
private val uxListener = CarUxRestrictionsManager.OnUxRestrictionsChangedListener { restrictions ->
    // Update blocked state based on current restrictions
    blockedState.value = mapRestrictionsToBlockedFunctions(restrictions)
}
```

### 3. UI Adaptation

The Compose UI reactively updates based on the blocked state:

- Shows which functions are currently blocked
- Truncates long text when string length is limited
- Provides visual feedback about safety mode status

## Internationalization

All user-facing strings are externalized in `res/values/strings.xml` for easy localization:

```xml
<string name="safety_mode_active">Safety Mode Active</string>
<string name="functions_blocked">The following functions are currently blocked:</string>
```

To add a new language:
1. Create a new values folder (e.g., `values-de` for German)
2. Copy `strings.xml` to the new folder
3. Translate all string values

## Best Practices Implemented

- ✅ **Distraction Optimization**: App declares `distractionOptimized="true"` in manifest
- ✅ **Reactive UI**: Uses Compose state for automatic UI updates
- ✅ **Resource Externalization**: All strings in resource files
- ✅ **Permission Handling**: Graceful fallback if Car API is unavailable
- ✅ **Lifecycle Management**: Proper cleanup in `onDestroy()`
- ✅ **Non-exclusive Restrictions**: Supports multiple simultaneous restrictions

## Troubleshooting

### App crashes on startup
- Ensure you're running on Android Automotive OS (not regular Android)
- Check that the required permission is granted
- Verify the automotive feature is available

### Restrictions not updating
- Check Car API connection status
- Verify driving state changes in emulator controls
- Review logcat for Car API errors

### UI not responding
- Ensure Compose dependencies are properly configured
- Check that state updates are happening on main thread
- Verify listener is properly registered

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Resources

- [Android Automotive Developer Documentation](https://developer.android.com/training/cars)
- [Car UX Restrictions Guide](https://developer.android.com/training/cars/testing#test-ux-restrictions)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Android Automotive Design Guidelines](https://developer.android.com/training/cars/design)

## Acknowledgments

- Android Automotive OS team for the Car API
- Jetpack Compose team for the modern UI toolkit
- Android developer community for best practices and examples

## Contact

For questions or support, please open an issue in the repository.

---

**Note**: This is a demonstration application intended for educational purposes and as a reference implementation for Android Automotive UX restrictions handling.
