# PantryPal

PantryPal is an offline-first Android pantry manager created by Group 12. It helps users track ingredients, monitor expiry dates, reduce food waste, and discover recipes that use food already available at home.

The app stores its data locally, does not require a Supabase account, and does not use a recipe API or API key.

## Features

- Add, edit, view, consume, and discard pantry ingredients.
- Track quantities, categories, prices, photos, and expiry dates.
- Dashboard summaries for fresh, near-expiry, and expired items.
- Usage, waste, and estimated cost statistics.
- Daily expiry notifications with a user-controlled on/off setting.
- Offline recipe suggestions ranked against non-expired pantry ingredients.
- Bundled recipe images, ingredient lists, and cooking instructions.
- Persistent System, Light, and Dark theme options.
- Local profile and kitchen-name support.
- Fully local operation with no internet permission.

## Technology

- Kotlin and Java
- Android SDK 34, minimum SDK 26
- AndroidX AppCompat, RecyclerView, CardView, and View Binding
- Material Components
- Kotlin Coroutines
- WorkManager for scheduled expiry checks
- Gson for local JSON serialization
- Glide for bundled and locally selected images
- MPAndroidChart for dashboard statistics

## Local Data Storage

Inventory and profile data are serialized as JSON and saved in Android `SharedPreferences`. The main storage components are:

- `LocalStorage` — initializes and provides application-level preferences.
- `IngredientRepository` — manages active and historical pantry items.
- `ProfileRepository` — manages local user profiles.

Uninstalling the app or clearing its application data removes the locally stored information. No pantry or profile data is uploaded to an external service.

## Offline Recipe Suggestions

Recipes are defined in `LocalRecipeRepository.kt`. The repository:

1. Excludes expired pantry items from matching.
2. Normalizes pantry and recipe ingredient names.
3. Counts the ingredients available for each recipe.
4. Sorts recipes by the number of pantry matches, missing ingredients, and cooking time.
5. Returns starter recipe ideas even when the pantry is empty.

Because the catalogue, images, and instructions are bundled with the APK, opening the recipe menu does not consume an API quota and continues to work offline.

To expand the catalogue, add another `LocalRecipe` entry in:

```text
app/src/main/java/com/example/group12_asgmt2/LocalRecipeRepository.kt
```

## Expiry Notifications

`ExpiryNotificationManager` schedules a daily background check through WorkManager. `ExpiryNotificationWorker` creates a consolidated notification for expired items and items expiring soon.

- Notifications can be enabled or disabled from Settings.
- Android 13 and newer request notification permission at runtime.
- Disabling alerts cancels scheduled work and removes the current expiry notification.
- Each ingredient can also retain its own alert preference.

## Themes and Settings

Open Settings using the gear icon on the dashboard. Users can select:

- Use device setting
- Light theme
- Dark theme

The selected mode is saved locally and restored when the application starts. Settings also contains the expiry-alert switch and About Us information.

## Project Structure

```text
app/src/main/
├── AndroidManifest.xml
├── java/com/example/group12_asgmt2/
│   ├── DashboardActivity.kt
│   ├── InventoryActivity.kt
│   ├── AddEditIngredientActivity.kt
│   ├── RecipeActivity.java
│   ├── RecipeDetailActivity.java
│   ├── StatisticsActivity.kt
│   ├── SettingsActivity.kt
│   ├── LocalStorage.kt
│   ├── IngredientRepository.kt
│   ├── ProfileRepository.kt
│   ├── LocalRecipeRepository.kt
│   ├── ExpiryNotificationManager.kt
│   ├── ExpiryNotificationWorker.kt
│   └── ThemePreferences.kt
└── res/
    ├── drawable/       Bundled images, icons, and backgrounds
    ├── layout/         Activity and list-item layouts
    ├── values/         Light palette, strings, and styles
    └── values-night/   Dark palette and styles
```

## Requirements

- Android Studio with Android SDK 34 installed
- JDK 17 or newer
- An emulator or Android device running Android 8.0 (API 26) or newer

No Supabase configuration, API key, or network service is required.

## Build and Run

1. Clone or open the project in Android Studio.
2. Allow Gradle to synchronize dependencies.
3. Select an emulator or connected Android device.
4. Run the `app` configuration.

To build from a terminal on Windows:

```powershell
.\gradlew.bat assembleDebug
```

On macOS or Linux:

```bash
./gradlew assembleDebug
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Validation

Run the local unit tests, Android lint, and debug build with:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

The current implementation has been verified on an Android emulator for:

- Application launch and navigation
- Local pantry persistence
- Light and dark themes
- Notification preference persistence
- Expiry notification delivery
- Offline recipe listing and recipe details

## Privacy

PantryPal keeps pantry, profile, theme, and notification preferences on the device. The application manifest does not request internet access.

## Authors

Created by Group 12.
