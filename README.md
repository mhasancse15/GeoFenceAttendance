# Geo-Fenced Attendance System
A modern Android application for location-aware employee attendance tracking using Jetpack Compose and Google Play Services.

## 📋 Features

- **GPS-Based Location Tracking**: Real-time location updates using Google Play Services Fused Location Provider
- **Office Geofencing**: 50-meter radius validation for office presence detection
- **Time Window Validation**: Attendance marking only available during 09:00 AM - 10:30 AM
- **Real-time Distance Indicator**: Animated visual feedback showing distance from office location
- **Local Data Persistence**: Office coordinates saved locally using AndroidX DataStore
- **Material Design 3 UI**: Modern Jetpack Compose implementation with professional styling

## 🏗️ Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/geoattendance/
│   │   │   ├── MainActivity.kt                 # App entry point with permission handling
│   │   │   ├── data/
│   │   │   │   ├── LocationRepository.kt       # GPS location provider (Fused Location)
│   │   │   │   └── OfficeLocationStore.kt      # Local persistence (DataStore)
│   │   │   ├── viewmodel/
│   │   │   │   ├── AttendanceViewModel.kt      # UI state management
│   │   │   │   └── AttendanceViewModelFactory.kt # ViewModel factory
│   │   │   ├── ui/
│   │   │   │   └── AttendanceScreen.kt         # Main Compose UI with all components
│   │   │   └── util/
│   │   │       ├── DistanceUtils.kt            # Geofencing calculations
│   │   │       └── TimeWindow.kt               # Check-in time window logic
│   │   ├── res/
│   │   │   ├── drawable/                       # Drawable resources
│   │   │   ├── mipmap-*/                       # App icons for all densities
│   │   │   ├── values/                         # Color, string, dimension resources
│   │   │   └── AndroidManifest.xml
│   │   └── AndroidManifest.xml
│   └── test/                                    # Unit tests (future)
├── build.gradle.kts                            # Dependencies & build config
└── .gitignore                                  # Git exclusions
```

## 🔧 Tech Stack

### Core Android
- **Kotlin 1.9+** - Modern Android development language
- **Jetpack Compose** - Declarative UI framework
- **Android Material 3** - Latest Material Design system
- **Android API 26+** - Android 8.0 and above

### Architecture Components
- **ViewModel** - Lifecycle-aware state management
- **DataStore** - Modern data persistence (replacing SharedPreferences)
- **StateFlow** - Reactive state streams with Kotlin Coroutines

### Location & Permissions
- **Google Play Services Location 21.3.0** - Fused Location Provider API
- **Kotlin Coroutines Play Services** - Async extensions for location tasks

### Build & Gradle
- **Gradle 9.3** - Latest build system
- **AGP (Android Gradle Plugin)** - Latest version
- **Java/Kotlin Compilation Target**: 17

## 📱 Specifications

| Property | Value |
|---|---|
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 (Android 14) |
| Compile SDK | 34 |
| Java Version | 17 |
| Kotlin Version | 1.9+ |

## 🚀 Quick Start

### Prerequisites
- Android Studio Electric Eel or newer
- JDK 17+
- Android SDK 34 (API Level 34)

### Build Commands

**Debug Build**
```bash
./gradlew assembleDebug
```

**Release Build**
```bash
./gradlew assembleRelease
```

**Run on Device**
```bash
./gradlew installDebug
adb shell am start -n com.example.geoattendance/.MainActivity
```

**Clean Build**
```bash
./gradlew clean build
```

## 📦 Dependencies

### Core
```gradle
androidx.core:core-ktx:1.13.1
androidx.activity:activity-compose:1.9.1
```

### UI & Compose
```gradle
androidx.compose.bom:2024.06.00
androidx.compose.material3:material3
androidx.compose.material:material-icons-extended
androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4
```

### Location Services
```gradle
com.google.android.gms:play-services-location:21.3.0
org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0
```

### Data Persistence
```gradle
androidx.datastore:datastore-preferences:1.1.1
```

## 📊 Architecture Overview

### MVVM Architecture
```
UI Layer (Compose)
    ↓
ViewModel (State Management)
    ↓
Data Layer (Repositories)
    ↓
Repository Pattern (Repositories)
```

### Data Flow
```
User Action (Button Click)
    ↓
ViewModel.setOfficeLocation()
    ↓
LocationRepository.getCurrentLocation()
    ↓
OfficeLocationStore.save()
    ↓
UI State Update → Recompose
```

## 🎯 User Workflow

1. **Permission Request** → REQUEST location permission on launch
2. **Set Office Location** → 
   - Tap "Set Office Location" button
   - Fetch current GPS coordinates via Fused Location Provider
   - Save to local DataStore persistence
3. **Real-time Tracking** →
   - Observe location updates every 3 seconds
   - Calculate distance to office location
   - Update distance ring indicator
4. **Range Validation** →
   - Check if within 50-meter radius
   - Display "IN RANGE" / "OUT OF RANGE" status
5. **Time Window Check** →
   - Validate current time is between 09:00 AM - 10:30 AM
   - Enable/disable "Mark Attendance" button accordingly
6. **Mark Attendance** →
   - Single tap to submit (only when conditions met)
   - Visual confirmation of successful submission

## 🔒 Permissions

Required in `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
```

Runtime permission request handled in `MainActivity.kt`

## 🧮 Core Algorithms

### Geofencing Logic
- **Distance Calculation**: Uses `Location.distanceBetween()` (Vincenty method)
- **Radius**: 50 meters (configurable in `DistanceUtils.kt`)
- **Accuracy**: High-accuracy GPS mode

### Time Window Logic
- **Check-in Window**: 09:00 AM - 10:30 AM
- **Validation**: Compares `LocalTime.now()` against window bounds
- **Timezone**: Device local timezone

### Location Tracking
- **Update Interval**: 3 seconds (optimized for real-time feedback)
- **Priority**: HIGH_ACCURACY (requires battery, ensures precision)
- **Provider**: Fused Location Provider (combines GPS, Wi-Fi, cellular)

## 📋 State Management

### AttendanceUiState
```kotlin
data class AttendanceUiState(
    val officeLocation: GeoPoint?,           // Saved office location
    val currentLocation: GeoPoint?,          // Real-time user location
    val distanceMeters: Float?,              // Calculated distance
    val isWithinRange: Boolean,              // 50m geofence check
    val isSettingOffice: Boolean,            // Loading state for location set
    val isTimeWindowOpen: Boolean,           // 09:00-10:30 validation
    val attendanceMarked: Boolean,           // Submission state
    val error: String?                       // Error messages
)
```

