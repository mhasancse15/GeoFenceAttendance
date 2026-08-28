# Architecture & Design

## Overview

Geo-Fenced Attendance System follows **MVVM Architecture** with **Reactive Programming** using Kotlin Coroutines and Flow.

```
┌─────────────────────────────────────────────────────────┐
│                   UI Layer (Compose)                     │
│                  AttendanceScreen.kt                     │
│  (OfficeContextCard, DistanceIndicator, MarkButton)     │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────┴─────────────────────────────────┐
│              ViewModel Layer (State Mgmt)                │
│              AttendanceViewModel.kt                      │
│  (State: AttendanceUiState, Actions: onClick methods)   │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────┴─────────────────────────────────┐
│            Repository Layer (Data Access)                │
│  ┌──────────────────────┐  ┌──────────────────────┐     │
│  │ LocationRepository   │  │ OfficeLocationStore  │     │
│  │ (Fused Location API) │  │ (DataStore)          │     │
│  └──────────────────────┘  └──────────────────────┘     │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────┴─────────────────────────────────┐
│            Data Sources (External APIs)                  │
│  ┌──────────────────────┐  ┌──────────────────────┐     │
│  │ GPS/Location Service │  │ Local Preferences   │     │
│  │ (Google Play Svc)    │  │ (DataStore)         │     │
│  └──────────────────────┘  └──────────────────────┘     │
└─────────────────────────────────────────────────────────┘
```

## Layer Responsibilities

### UI Layer (Presentation)
**File**: `ui/AttendanceScreen.kt`

Responsibilities:
- Display UI components (Compose)
- Handle user interactions (clicks, navigation)
- Observe and react to ViewModel state changes
- Display real-time updates (distance, status)

Composables:
- `AttendanceScreen()` - Main container with Scaffold
- `OfficeContextCard()` - Office location setup section
- `MapTile()` - Location display placeholder
- `DistanceIndicator()` - Animated distance ring with status
- `MarkAttendanceButton()` - Attendance submission button
- `ErrorMessage()` - Error feedback display

### ViewModel Layer (State Management)
**File**: `viewmodel/AttendanceViewModel.kt`

Responsibilities:
- Manage UI state (AttendanceUiState)
- Handle business logic for state transitions
- Connect repositories and expose state to UI
- Handle user actions and update state

State:
```kotlin
data class AttendanceUiState(
    val officeLocation: GeoPoint? = null,          // Persisted office location
    val currentLocation: GeoPoint? = null,         // Real-time user location
    val distanceMeters: Float? = null,             // Calculated distance
    val isWithinRange: Boolean = false,            // Geofence validation
    val isSettingOffice: Boolean = false,          // Location fetch loading state
    val isTimeWindowOpen: Boolean = false,         // Time window validation
    val attendanceMarked: Boolean = false,         // Submission confirmation
    val error: String? = null                      // Error messages
)
```

Key Methods:
- `setOfficeLocation()` - Fetch and save office location
- `markAttendance()` - Submit attendance (gated by validation)

### Repository Layer (Data Access)
**Files**: `data/LocationRepository.kt`, `data/OfficeLocationStore.kt`

#### LocationRepository
Responsibilities:
- Interface with Fused Location Provider API
- Provide GPS location updates
- Handle location permissions
- Expose location as reactive Flow

Methods:
- `getCurrentLocation(): GeoPoint?` - One-shot high-accuracy location fetch
- `observeLocation(intervalMillis): Flow<GeoPoint>` - Continuous location stream

#### OfficeLocationStore
Responsibilities:
- Persist office location locally
- Expose persisted location as reactive Flow
- Handle DataStore lifecycle

Methods:
- `officeLocation: Flow<GeoPoint?>` - Observe saved location
- `save(point: GeoPoint)` - Persist location to DataStore
- `clear()` - Remove persisted location

### Utility Layer (Business Logic)
**Files**: `util/DistanceUtils.kt`, `util/TimeWindow.kt`

#### DistanceUtils
- `data class GeoPoint` - Immutable coordinate pair
- `const OFFICE_RADIUS_METERS = 50f` - Geofence radius
- `fun distanceBetween(from, to): Float` - Calculate distance using Vincenty formula
- `fun Float.isWithinOfficeRadius(): Boolean` - Extension for range check

#### TimeWindow
- `data class CheckInWindow` - Time window definition
- `fun isOpenNow(): Boolean` - Check if current time is within window
- `fun label(): String` - Human-readable time range display

## Data Flow

### Setting Office Location
```
User Clicks "Set Office Location"
  ↓
ViewModel.setOfficeLocation()
  ↓
LocationRepository.getCurrentLocation()
  ↓
Fused Location Provider (GPS)
  ↓
GeoPoint returned
  ↓
OfficeLocationStore.save(GeoPoint)
  ↓
DataStore persists
  ↓
ViewModel state updated
  ↓
UI recomposed with new location
```

### Real-time Distance Tracking
```
LocationRepository.observeLocation() starts
  ↓
Emits GeoPoint every 3 seconds
  ↓
ViewModel combines office + current location
  ↓
Calculate distance: distanceBetween(office, current)
  ↓
Check range: distance.isWithinOfficeRadius()
  ↓
Update AttendanceUiState
  ↓
Compose recomposes:
  - Distance ring (animated)
  - Status badge (IN/OUT of range)
  - Button enabled state
```

### Marking Attendance
```
User Clicks "Mark Attendance" (must satisfy all conditions)
  ↓
Check:
  ✓ isWithinRange == true
  ✓ isTimeWindowOpen == true
  ✓ officeLocation != null
  ✓ attendanceMarked == false
  ↓
ViewModel.markAttendance()
  ↓
Set attendanceMarked = true
  ↓
UI shows "✓ Attendance Marked"
  ↓
[TODO: API call to backend]
```

## State Computed Properties

```kotlin
val canMarkAttendance: Boolean
  get() = isWithinRange 
       && isTimeWindowOpen 
       && !attendanceMarked 
       && officeLocation != null

val windowLabel: String
  get() = DEFAULT_ATTENDANCE_WINDOW.label()
  // Output: "Available 09:00 AM - 10:30 AM"
```

## Reactive Streams

### Observable Chains

**Location Updates**
```kotlin
// Combines office location and current location streams
combine(
    officeLocationStore.officeLocation,    // Flow<GeoPoint?>
    locationRepository.observeLocation()   // Flow<GeoPoint>
) { office, current -> office to current }
  .collect { (office, current) ->
      // Calculate distance and update state
  }
```

**State Propagation**
```kotlin
MutableStateFlow(AttendanceUiState())
  ↓
StateFlow.asStateFlow() // Read-only
  ↓
UI: state.collectAsState()
  ↓
Compose recomposition
```

## Permission Handling

**Runtime Permission Request** (MainActivity.kt)
```kotlin
LocationPermissionGate(
    onGranted = { AttendanceScreen() },
    onDenied = { RequestPermissionButton() }
)
```

**Manifest Declaration**
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

**Repository Assumption**
- LocationRepository assumes permission is already granted
- Enforced via @SuppressLint("MissingPermission") annotations

## Error Handling

Error scenarios handled:
1. **No GPS Fix** - Message: "Couldn't get a GPS fix. Try again outdoors."
2. **Location Permission Denied** - Fallback UI with retry button
3. **DataStore Read/Write Failures** - Caught in ViewModel try-catch
4. **Location Timeout** - Handled by Fused Location API timeouts

Error messages displayed in `ErrorMessage()` composable with red styling.

## Configuration Constants

Located in `util/DistanceUtils.kt` and `util/TimeWindow.kt`:

```kotlin
const val OFFICE_RADIUS_METERS = 50f          // Geofence boundary

val DEFAULT_ATTENDANCE_WINDOW = CheckInWindow(
    start = LocalTime.of(9, 0),               // 09:00 AM
    end = LocalTime.of(10, 30)                // 10:30 AM
)

// LocationRepository constants
private const val LOCATION_UPDATE_INTERVAL = 3_000L  // 3 seconds
private const val LOCATION_FASTEST_INTERVAL = 1_500L // 1.5 seconds
private const val PRIORITY = Priority.PRIORITY_HIGH_ACCURACY
```

## Testing Considerations

### Unit Tests (Future)
- `LocationRepositoryTest` - Mock Fused Location Provider
- `OfficeLocationStoreTest` - Mock DataStore
- `AttendanceViewModelTest` - Verify state transitions
- `DistanceUtilsTest` - Verify distance calculations
- `TimeWindowTest` - Verify time validation logic

### Integration Tests (Future)
- Full flow: Permission → Location → Attendance → Submission
- Error recovery: Retry mechanisms
- State persistence: Survive process death

### Manual Testing
1. Test outdoor with clear GPS signal (accuracy required)
2. Test with office location at different distances (0m, 40m, 50m, 100m)
3. Test at time boundaries (08:59, 09:00, 10:30, 10:31)
4. Test permission denial and recovery

## Performance Considerations

1. **Location Updates**: 3-second interval balances real-time feedback with battery
2. **Recomposition Optimization**: State changes only trigger affected composables
3. **DataStore**: Non-blocking, coroutine-based persistence
4. **Memory**: ViewModel retained across configuration changes (rotation-safe)

## Security Considerations

1. **Permissions**: Runtime permission request (Android 6.0+)
2. **Data Privacy**: Office location stored locally only (no cloud initially)
3. **No API Keys**: No sensitive credentials in code
4. **Location Accuracy**: High-accuracy mode requires device capability

---

**Last Updated**: August 28, 2026  
**Architect**: Development Team
