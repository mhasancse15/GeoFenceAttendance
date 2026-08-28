# Project Structure Guide

## Directory Hierarchy

```
GeoFenceAttendance/                          # Root project directory
│
├── .gitignore                               # Git exclusions
├── gradle.properties                        # Gradle configuration (AndroidX, JVM args)
├── settings.gradle.kts                      # Root project settings
├── build.gradle.kts                         # Root build configuration
│
├── README.md                                # Project overview & quick start
├── ARCHITECTURE.md                          # Detailed architecture documentation
│
├── gradle/                                  # Gradle wrapper files
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
│
├── app/                                     # Main application module
│   ├── build.gradle.kts                     # App-level build configuration
│   │                                        # (Dependencies, plugins, Android config)
│   │
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml          # App manifest (permissions, activities)
│       │   │
│       │   ├── java/com/example/geoattendance/
│       │   │   ├── MainActivity.kt          # Entry point (permission handling)
│       │   │   │
│       │   │   ├── data/                    # Repository Layer
│       │   │   │   ├── LocationRepository.kt        # Location provider (GPS)
│       │   │   │   └── OfficeLocationStore.kt       # Local persistence
│       │   │   │
│       │   │   ├── viewmodel/               # State Management Layer
│       │   │   │   ├── AttendanceViewModel.kt       # UI state & actions
│       │   │   │   └── AttendanceViewModelFactory.kt # ViewModel instantiation
│       │   │   │
│       │   │   ├── ui/                      # Presentation Layer
│       │   │   │   └── AttendanceScreen.kt  # Compose UI components
│       │   │   │
│       │   │   └── util/                    # Utility Functions
│       │   │       ├── DistanceUtils.kt     # Geofencing calculations
│       │   │       └── TimeWindow.kt        # Time validation logic
│       │   │
│       │   └── res/                         # Resources
│       │       ├── drawable/                # Vector drawables
│       │       │   ├── ic_launcher_background.xml
│       │       │   └── ic_launcher_foreground.xml
│       │       │
│       │       ├── mipmap-anydpi-v33/       # Adaptive icons (Android 13+)
│       │       │   └── ic_launcher.xml
│       │       │
│       │       ├── mipmap-{hdpi,mdpi,...}/  # Icons for all screen densities
│       │       │   ├── mipmap-hdpi/
│       │       │   ├── mipmap-mdpi/
│       │       │   ├── mipmap-xhdpi/
│       │       │   ├── mipmap-xxhdpi/
│       │       │   └── mipmap-xxxhdpi/
│       │       │
│       │       └── values/                  # Strings, colors, themes
│       │           └── themes.xml           # Material 3 theme
│       │
│       └── test/                            # Unit tests (future)
│           └── java/...
│
└── .gradle/                                 # Build cache (auto-generated)
    └── ...
```

## File Organization Rationale

### Root Level Files

| File | Purpose |
|------|---------|
| `gradle.properties` | Gradle daemon settings, AndroidX enablement, JVM memory |
| `settings.gradle.kts` | Module includes (app module declaration) |
| `build.gradle.kts` | Root build configuration (shared plugins) |
| `README.md` | High-level overview, quick start, features |
| `ARCHITECTURE.md` | Detailed architecture, data flow, design patterns |
| `.gitignore` | Git exclusions (build artifacts, IDE files) |

### App Module Structure

#### `build.gradle.kts`
Declares:
- Compilation SDK and target SDK versions
- Kotlin compiler extension version for Compose
- Java/Kotlin compilation targets (17)
- All dependencies (Compose BOM, location services, etc.)
- Build features (Compose enabled)
- Build types (debug, release)

#### Source Code Organization

**Package: `com.example.geoattendance`**

```
com.example.geoattendance/
├── MainActivity.kt              # Application entry point
├── data/                        # Repository Layer
│   ├── LocationRepository.kt    # Fused Location Provider wrapper
│   └── OfficeLocationStore.kt   # DataStore-backed persistence
├── viewmodel/                   # ViewModel Layer (State Management)
│   ├── AttendanceViewModel.kt   # Main state holder & actions
│   └── AttendanceViewModelFactory.kt # ViewModel factory for DI
├── ui/                          # UI Layer (Presentation)
│   └── AttendanceScreen.kt      # All Compose composables
└── util/                        # Utility Layer (Business Logic)
    ├── DistanceUtils.kt         # GeoPoint, distance calculation
    └── TimeWindow.kt            # Time window validation
```

### Resource Organization

#### Drawables
- **`drawable/`** - Vector graphics (XML)
  - `ic_launcher_foreground.xml` - App icon foreground (location indicator)
  - `ic_launcher_background.xml` - App icon background

#### Mipmaps (Icons for Different Densities)
Each directory contains `ic_launcher.xml` for that density:

| Directory | DPI | Scale |
|-----------|-----|-------|
| `mipmap-mdpi` | 160 | 1.0x |
| `mipmap-hdpi` | 240 | 1.5x |
| `mipmap-xhdpi` | 320 | 2.0x |
| `mipmap-xxhdpi` | 480 | 3.0x |
| `mipmap-xxxhdpi` | 640 | 4.0x |
| `mipmap-anydpi-v33` | Adaptive | Android 13+ |

#### Values
- **`values/themes.xml`** - Material 3 theme definition

## Package Responsibilities

### `data` - Data Access Layer
Responsibilities:
- Abstract external data sources
- Provide reactive data streams (Flow)
- Handle data persistence and retrieval

Classes:
- **LocationRepository** - GPS location provider
  - Dependencies: Fused Location Provider, Coroutines
  - Exposes: `getCurrentLocation()`, `observeLocation()`
  
- **OfficeLocationStore** - Persistent office location
  - Dependencies: DataStore
  - Exposes: `officeLocation` Flow, `save()`, `clear()`

### `viewmodel` - State Management Layer
Responsibilities:
- Manage UI state lifecycle
- Coordinate repositories
- Expose observable state to UI
- Handle user actions

Classes:
- **AttendanceViewModel** - Main state holder
  - State: `AttendanceUiState`
  - Actions: `setOfficeLocation()`, `markAttendance()`
  - Init: Combines repository flows, calculates derived state
  
- **AttendanceViewModelFactory** - ViewModel instantiation
  - Creates ViewModel with context-dependent repositories

### `ui` - Presentation Layer
Responsibilities:
- Display UI using Compose
- React to state changes
- Handle user interactions
- Provide visual feedback

Composables:
- `AttendanceScreen()` - Main container
- `OfficeContextCard()` - Office location setup
- `DistanceIndicator()` - Distance ring + status
- `MarkAttendanceButton()` - Attendance submission
- `ErrorMessage()` - Error display

### `util` - Utility Layer
Responsibilities:
- Core business logic
- Calculations and validations
- Constants and helpers

Classes:
- **DistanceUtils**
  - `GeoPoint` data class
  - Distance calculations
  - Range checking
  
- **TimeWindow**
  - Time validation
  - User-friendly label generation

## Build Artifacts

After building, artifacts are generated in:

```
app/build/
├── outputs/
│   ├── apk/
│   │   ├── debug/
│   │   │   └── app-debug.apk              # Debug APK (installable)
│   │   └── release/
│   │       └── app-release.apk            # Release APK
│   ├── bundle/
│   │   └── release/
│   │       └── app-release.aab            # App Bundle (Play Store)
│   └── mapping/
│       └── release/                       # ProGuard/R8 mapping
│
├── intermediates/                         # Compiled intermediate files
└── reports/
    └── lint-results-*.html               # Lint analysis reports
```

## Dependencies Structure

```
app/build.gradle.kts
│
├── Platform Dependencies
│   └── androidx.compose:compose-bom:2024.06.00
│
├── AndroidX Libraries
│   ├── androidx.core:core-ktx
│   ├── androidx.activity:activity-compose
│   ├── androidx.lifecycle:lifecycle-*
│   └── androidx.datastore:datastore-preferences
│
├── Compose UI
│   ├── androidx.compose.ui:ui
│   ├── androidx.compose.material3:material3
│   └── androidx.compose.material:material-icons-extended
│
├── Google Play Services
│   └── com.google.android.gms:play-services-location
│
└── Kotlin
    └── org.jetbrains.kotlinx:kotlinx-coroutines-play-services
```

## Naming Conventions

### Files
- **Kotlin**: `PascalCase.kt` (e.g., `MainActivity.kt`, `LocationRepository.kt`)
- **XML Resources**: `snake_case.xml` (e.g., `ic_launcher.xml`)
- **Android Manifest**: `AndroidManifest.xml`

### Packages
- Reverse domain convention: `com.example.geoattendance`
- Feature-based sub-packages: `data`, `ui`, `viewmodel`, `util`

### Classes
- **Activities**: `*Activity` (e.g., `MainActivity`)
- **ViewModels**: `*ViewModel` (e.g., `AttendanceViewModel`)
- **Repositories**: `*Repository` (e.g., `LocationRepository`)
- **Data Store**: `*Store` (e.g., `OfficeLocationStore`)
- **Composables**: `*Screen`, `*Card`, `*Indicator` (e.g., `AttendanceScreen`)
- **Utilities**: Module-based (e.g., `DistanceUtils`)

### Variables
- **UI State**: `uiState` (StateFlow)
- **Private State**: `_uiState` (MutableStateFlow)
- **Flows**: `observable*` or noun (e.g., `officeLocation`)

## Configuration Files

### `gradle.properties`
```properties
android.useAndroidX=true                    # Use AndroidX libraries
android.enableJetifier=true                 # Jetifier compatibility
org.gradle.jvmargs=-Xmx2048m               # JVM heap size
org.gradle.daemon.performance.disable-logging=true
```

### `build.gradle.kts` (App Level)
- Compilation & Target SDK
- Kotlin Compiler Extension Version
- Build Features (Compose)
- Dependencies management

### `AndroidManifest.xml`
- App package name
- Activities
- Permissions (location)
- Application attributes (icon, theme)

## Best Practices Implemented

✅ **Separation of Concerns** - Clear layer boundaries
✅ **Reactive Programming** - Flow-based state management
✅ **MVVM Architecture** - ViewModel + State
✅ **Dependency Injection** - Factory pattern for ViewModel
✅ **Modern Android** - Jetpack Compose, Coroutines
✅ **Resource Organization** - Density-specific mipmaps
✅ **Code Organization** - Package-based structure
✅ **Documentation** - README + ARCHITECTURE guides

---

**Last Updated**: August 28, 2026
