# Stride — Android Studio starter project

Stride is a native Android starter app for personal outdoor jogging.

## Included in this first project drop
- Native Kotlin + Jetpack Compose dashboard
- Start/finish run controls
- GPS location updates using Google Play services Location
- Foreground location service so an active run can continue with the screen locked
- Distance estimate with basic GPS accuracy / jump filtering
- Elapsed time and average pace display
- Health Connect permissions declared in the manifest and a privacy rationale screen

## Important current limitations
This is a **starter project**, not a finished, tested APK.
- Workout history, route map display, step sensor counting, calories, and actual Health Connect read/write synchronization are not implemented yet.
- The Health Connect button currently opens the Health Connect app if installed; it does not yet complete the permission or sync flow.
- The current run summary is not persisted after the service stops.
- GPS distance can vary with signal quality. Test outdoors and compare with Samsung Health before relying on it.
- Heart rate requires a compatible external sensor or a source such as a Galaxy Watch; a phone alone generally cannot measure it.

## Build with Android Studio
1. Install the latest stable Android Studio.
2. Extract the `Stride` folder.
3. Open Android Studio → Open → select the extracted `Stride` folder.
4. Allow Gradle sync. Install Android SDK Platform 35 if prompted.
5. Connect your Samsung phone with USB debugging enabled, or use Android Studio's device pairing.
6. Press Run to install the debug app.

To create an APK:
- Build → Build Bundle(s) / APK(s) → Build APK(s)
- Android Studio will show the generated APK location.

## Phone permissions
When prompted, allow precise location and notifications. Start each run while the app is visible. Android requires a location foreground service for sustained background GPS tracking. Do not grant "all the time" location access for this design.

## Health Connect / Samsung Health
Samsung Health can share supported data through Health Connect when the phone's Samsung Health and Health Connect versions support the data type and the user enables the connection. The next development milestone is implementing Health Connect permission requests and inserting an ExerciseSessionRecord, DistanceRecord, and route after each run, then reading steps/heart-rate data where available.

## Technical references
- Health Connect workouts: https://developer.android.com/health-and-fitness/health-connect/experiences/workouts
- Health Connect exercise routes: https://developer.android.com/health-and-fitness/health-connect/features/exercise-routes
- Health Connect setup: https://developer.android.com/health-and-fitness/health-connect/get-started
- Location foreground service: https://developer.android.com/about/versions/14/changes/fgs-types-required
