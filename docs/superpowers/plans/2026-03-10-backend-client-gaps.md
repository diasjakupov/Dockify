# Backend–Client Gap Fixes Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix all five identified gaps between the backend API and the Kotlin Multiplatform client: two critical runtime bugs, one missing Koin registration, missing health metric types, and the unimplemented Profile screen.

**Architecture:** Each fix is isolated to its own feature slice (data → domain → presentation). The health metric type changes add a `backendKey` field to `HealthMetricType` so the exact string sent to the backend is decoupled from the Kotlin enum name. The Profile screen follows the established MVI + Koin pattern.

**Tech Stack:** Kotlin Multiplatform · Compose Multiplatform · Ktor 3 · Koin 4 · Navigation 3 · DataStore · MVI (BaseViewModel)

---

## Chunk 1: Critical Bugs + Koin Fix

### Task 1: Fix hospital endpoint path

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/location/data/datasource/LocationRemoteDataSourceImpl.kt:35`

The client calls `/api/v1/location/hospitals` but the backend serves `POST /api/v1/hospitals/nearest`.

- [ ] **Step 1: Fix the path**

In `LocationRemoteDataSourceImpl.kt`, change line 35 from:
```kotlin
httpClient.post("$baseUrl/api/v1/location/hospitals") {
```
to:
```kotlin
httpClient.post("$baseUrl/api/v1/hospitals/nearest") {
```

- [ ] **Step 2: Build to confirm no compilation errors**

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :composeApp:compileKotlinAndroid --quiet
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/location/data/datasource/LocationRemoteDataSourceImpl.kt
git commit -m "fix: correct hospital endpoint path to /api/v1/hospitals/nearest"
```

---

### Task 2: Fix recommendation endpoint — add `user_id` query parameter

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/recommendation/data/datasource/RecommendationRemoteDataSource.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/recommendation/data/datasource/RecommendationRemoteDataSourceImpl.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/recommendation/domain/repository/RecommendationRepository.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/recommendation/data/repository/RecommendationRepositoryImpl.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/recommendation/domain/usecase/GetRecommendationUseCase.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/health/presentation/HealthViewModel.kt`

The backend requires `GET /api/v1/recommendation?user_id=123`. The `user_id` parameter must be threaded through the entire call chain.

- [ ] **Step 1: Update `RecommendationRemoteDataSource` interface**

Add `userId: String` parameter:
```kotlin
interface RecommendationRemoteDataSource {
    suspend fun getRecommendation(userId: String): Resource<RecommendationResponseDto, DataError>
}
```

- [ ] **Step 2: Update `RecommendationRemoteDataSourceImpl`**

Add `parameter` call to attach the query param:
```kotlin
import io.ktor.client.request.parameter

override suspend fun getRecommendation(userId: String): Resource<RecommendationResponseDto, DataError> {
    return safeApiCall {
        httpClient.get("$baseUrl/api/v1/recommendation") {
            parameter("user_id", userId)
        }
    }
}
```

- [ ] **Step 3: Update `RecommendationRepository` interface**

```kotlin
interface RecommendationRepository {
    suspend fun getRecommendation(userId: String): Resource<Recommendation, DataError>
    fun observeCachedRecommendation(): Flow<Recommendation?>
    suspend fun clearCachedRecommendation()
}
```

- [ ] **Step 4: Update `RecommendationRepositoryImpl`**

Pass `userId` through to the remote data source. The existing impl uses `observeCachedRecommendation().first()` for the fallback — preserve that pattern:
```kotlin
override suspend fun getRecommendation(userId: String): Resource<Recommendation, DataError> {
    return when (val result = remoteDataSource.getRecommendation(userId)) {
        is Resource.Success -> {
            val recommendation = result.data.toDomain()
            localDataSource.cacheRecommendation(result.data)
            Resource.Success(recommendation)
        }
        is Resource.Error -> {
            val cached = localDataSource.observeCachedRecommendation().first()
            if (cached != null) Resource.Success(cached.toDomain())
            else Resource.Error(result.error)
        }
    }
}
```
(Keep the rest of the impl unchanged — only the `getRecommendation` signature and body change.)

- [ ] **Step 5: Update `GetRecommendationUseCase`**

```kotlin
class GetRecommendationUseCase(
    private val recommendationRepository: RecommendationRepository
) {
    suspend operator fun invoke(userId: String): Resource<Recommendation, DataError> {
        if (userId.isBlank()) return Resource.Error(DataError.Auth.UNAUTHORIZED)
        return recommendationRepository.getRecommendation(userId)
    }
}
```

- [ ] **Step 6: Update `HealthViewModel` — pass current user ID**

In `HealthViewModel`, find `loadRecommendation()`. Get the userId from `getCurrentUserUseCase` and pass it:
```kotlin
private fun loadRecommendation() {
    launch {
        val userId = getCurrentUserId() ?: return@launch
        when (val result = getRecommendationUseCase(userId)) {
            is Resource.Success -> updateState { copy(recommendation = result.data, isRecommendationLoading = false) }
            is Resource.Error -> updateState { copy(isRecommendationLoading = false) }
        }
    }
}
```
`getCurrentUserId()` is already a private helper in `HealthViewModel` — reuse it.

- [ ] **Step 7: Build to confirm no compilation errors**

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :composeApp:compileKotlinAndroid --quiet
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/recommendation/
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/health/presentation/HealthViewModel.kt
git commit -m "fix: pass user_id query param to recommendation endpoint"
```

---

### Task 3: Register `GetHealthMetricsUseCase` in Koin

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/health/di/HealthModule.kt`

`GetHealthMetricsUseCase` is defined but never registered — Koin will throw a `NoBeanDefFoundException` if it's injected anywhere.

- [ ] **Step 1: Add import and factory to `HealthModule.kt`**

Add at the top of the imports:
```kotlin
import io.diasjakupov.dockify.features.health.domain.usecase.GetHealthMetricsUseCase
```

Add inside the `healthModule` block, after the existing use cases:
```kotlin
factory {
    GetHealthMetricsUseCase(
        healthRepository = get()
    )
}
```

- [ ] **Step 2: Build**

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :composeApp:compileKotlinAndroid --quiet
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/health/di/HealthModule.kt
git commit -m "fix: register GetHealthMetricsUseCase in Koin healthModule"
```

---

## Chunk 2: Health Metric Types

### Task 4: Add missing `HealthMetricType` values and fix backend key mapping

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/health/domain/model/HealthMetricType.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/health/data/mapper/HealthMetricMapper.kt` (wherever the mapper converts `HealthMetric` → `HealthMetricDto`)

**Background:** The backend's ML recommendation model uses exact string keys (e.g. `CALORIES`, `HEIGHT_M`, `MAX_BPM`, `resting_bpm`). The client's enum currently uses Kotlin names (`CALORIES_BURNED`, `HEIGHT`) which don't match. Adding a `backendKey` field decouples the two without renaming existing enum entries (which would break stored data).

- [ ] **Step 1: Add `backendKey` field and all missing types to `HealthMetricType.kt`**

Replace the entire file content:
```kotlin
package io.diasjakupov.dockify.features.health.domain.model

/**
 * Health metric types supported by the platform and backend.
 *
 * [backendKey] is the exact string sent to the backend API.
 * It may differ from the Kotlin enum name (e.g. CALORIES_BURNED → "CALORIES").
 */
enum class HealthMetricType(
    val displayName: String,
    val defaultUnit: String,
    val backendKey: String        // every entry must supply this explicitly; `name` is not accessible as a default in enum constructors
) {
    // ── Existing platform-read metrics ──────────────────────────────────
    STEPS("Steps", "steps", "STEPS"),
    HEART_RATE("Heart Rate", "bpm", "HEART_RATE"),
    BLOOD_PRESSURE_SYSTOLIC("Systolic BP", "mmHg", "BLOOD_PRESSURE_SYSTOLIC"),
    BLOOD_PRESSURE_DIASTOLIC("Diastolic BP", "mmHg", "BLOOD_PRESSURE_DIASTOLIC"),
    BLOOD_OXYGEN("Blood Oxygen", "%", "BLOOD_OXYGEN"),
    SLEEP_DURATION("Sleep Duration", "hours", "SLEEP_DURATION"),
    CALORIES_BURNED("Calories Burned", "kcal", "CALORIES"),          // backend key: CALORIES
    DISTANCE("Distance", "km", "DISTANCE"),
    WEIGHT("Weight", "kg", "WEIGHT"),
    HEIGHT("Height", "cm", "HEIGHT_M"),                              // backend key: HEIGHT_M
    BODY_TEMPERATURE("Body Temperature", "°C", "BODY_TEMPERATURE"),
    RESPIRATORY_RATE("Respiratory Rate", "breaths/min", "RESPIRATORY_RATE"),

    // ── Missing types required by backend ML model ───────────────────────
    AGE("Age", "years", "AGE"),
    BMI("BMI", "kg/m²", "BMI"),
    FAT_PERCENTAGE("Body Fat", "%", "FAT_PERCENTAGE"),
    MAX_BPM("Max Heart Rate", "bpm", "MAX_BPM"),
    AVG_BPM("Avg Heart Rate", "bpm", "AVG_BPM"),
    RESTING_BPM("Resting Heart Rate", "bpm", "resting_bpm"),        // backend key: lowercase
    SESSION_DURATION_HOURS("Session Duration", "hours", "SESSION_DURATION_HOURS"),
    WORKOUT_FREQUENCY("Workout Frequency", "days/week", "WORKOUT_FREQUENCY"),
    DAILY_CALORIES("Daily Calorie Intake", "kcal", "DAILY_CALORIES"),
    WATER_INTAKE_LITERS("Water Intake", "L", "WATER_INTAKE_LITERS"),
    SLEEP_EFFICIENCY("Sleep Efficiency", "%", "SLEEP_EFFICIENCY"),
    TIME_IN_BED_HOURS("Time in Bed", "hours", "TIME_IN_BED_HOURS"),
    MOVEMENTS_PER_HOUR("Movements/Hour", "count", "MOVEMENTS_PER_HOUR"),
    SNORE_TIME("Snore Time", "min", "SNORE_TIME"),
    DAY_OF_WEEK("Day of Week", "", "DAY_OF_WEEK"),
    HOUR_STARTED("Hour Started", "", "HOUR_STARTED"),
    NOTE_COFFEE("Coffee", "flag", "NOTE_COFFEE"),
    NOTE_TEA("Tea", "flag", "NOTE_TEA"),
    NOTE_WORKOUT("Workout Note", "flag", "NOTE_WORKOUT"),
    NOTE_STRESS("Stress Note", "flag", "NOTE_STRESS"),
    NOTE_ATE_LATE("Ate Late", "flag", "NOTE_ATE_LATE");

    companion object {
        fun fromString(value: String): HealthMetricType? =
            entries.find {
                it.name.equals(value, ignoreCase = true) ||
                it.displayName.equals(value, ignoreCase = true) ||
                it.backendKey.equals(value, ignoreCase = true)
            }
    }
}
```

- [ ] **Step 2: Update the health metric mapper to use `backendKey`**

Find the mapper that converts `HealthMetric` → `HealthMetricDto` (likely `HealthMetricMapper.kt`).
Change the `metric_type` field from `metric.type.name` to `metric.type.backendKey`:

```kotlin
// Before
HealthMetricDto(
    metricType = metric.type.name,   // was sending "CALORIES_BURNED", "HEIGHT"
    metricValue = metric.value.toString()
)

// After
HealthMetricDto(
    metricType = metric.type.backendKey,  // now sends "CALORIES", "HEIGHT_M"
    metricValue = metric.value.toString()
)
```

For the reverse mapper (DTO → domain): the existing code already calls `HealthMetricType.fromString(dto.metricType)`. After Step 1 adds `backendKey` to the enum and extends `fromString` to also match `backendKey`, this direction automatically works correctly. No separate change is needed beyond verifying the call site uses `fromString` (not `valueOf`).

- [ ] **Step 3: Build**

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :composeApp:compileKotlinAndroid --quiet
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/health/domain/model/HealthMetricType.kt
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/health/data/mapper/
git commit -m "feat: add missing HealthMetricType values and fix backend key mapping"
```

---

## Chunk 3: Profile Screen

### Task 5: Create Profile domain types and ViewModel

**Files:**
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/auth/presentation/profile/ProfileState.kt`
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/auth/presentation/profile/ProfileViewModel.kt`

The Profile screen displays the authenticated user's data already stored in DataStore. It reads via `GetCurrentUserUseCase` which exists and is already registered in Koin.

- [ ] **Step 1: Create `ProfileState.kt`**

```kotlin
package io.diasjakupov.dockify.features.auth.presentation.profile

import io.diasjakupov.dockify.features.auth.domain.model.User
import io.diasjakupov.dockify.ui.base.LoadingState
import io.diasjakupov.dockify.ui.base.UiAction
import io.diasjakupov.dockify.ui.base.UiEffect
import io.diasjakupov.dockify.ui.base.UiState
import io.diasjakupov.dockify.ui.base.WithError
import io.diasjakupov.dockify.ui.base.WithLoading

data class ProfileState(
    val user: User? = null,
    override val loadingState: LoadingState = LoadingState.IDLE,
    override val error: String? = null
) : UiState, WithLoading, WithError

sealed interface ProfileAction : UiAction {
    data object LoadProfile : ProfileAction
    data object Logout : ProfileAction
}

sealed interface ProfileEffect : UiEffect {
    data object NavigateToLogin : ProfileEffect
}
```

- [ ] **Step 2: Create `ProfileViewModel.kt`**

```kotlin
package io.diasjakupov.dockify.features.auth.presentation.profile

import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.auth.domain.usecase.GetCurrentUserUseCase
import io.diasjakupov.dockify.features.auth.domain.usecase.LogoutUseCase
import io.diasjakupov.dockify.ui.base.BaseViewModel
import io.diasjakupov.dockify.ui.base.LoadingState

class ProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : BaseViewModel<ProfileState, ProfileAction, ProfileEffect>(ProfileState()) {

    init {
        onAction(ProfileAction.LoadProfile)
    }

    override fun handleAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.LoadProfile -> loadProfile()
            is ProfileAction.Logout -> logout()
        }
    }

    private fun loadProfile() {
        updateState { copy(loadingState = LoadingState.LOADING) }
        launch {
            when (val result = getCurrentUserUseCase()) {
                is Resource.Success -> updateState {
                    copy(user = result.data, loadingState = LoadingState.IDLE)
                }
                is Resource.Error -> updateState {
                    copy(error = "Failed to load profile", loadingState = LoadingState.IDLE)
                }
            }
        }
    }

    private fun logout() {
        launch {
            // Best-effort: navigate to login regardless of result.
            // There is no meaningful recovery if logout fails locally.
            logoutUseCase()
            emitEffect(ProfileEffect.NavigateToLogin)
        }
    }
}
```

- [ ] **Step 3: Build**

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :composeApp:compileKotlinAndroid --quiet
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/auth/presentation/profile/
git commit -m "feat: add ProfileState, ProfileAction, ProfileEffect, ProfileViewModel"
```

---

### Task 6: Create ProfileScreen composable

**Files:**
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/auth/presentation/profile/ProfileScreen.kt`

- [ ] **Step 1: Create `ProfileScreen.kt`**

```kotlin
package io.diasjakupov.dockify.features.auth.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.diasjakupov.dockify.ui.components.common.DockifyScaffold
import io.diasjakupov.dockify.ui.components.common.TopBarConfig
import io.diasjakupov.dockify.ui.theme.DockifyTextStyles
import io.diasjakupov.dockify.ui.theme.NotionColors
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProfileEffect.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    DockifyScaffold(
        topBarConfig = TopBarConfig.Custom {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NotionColors.TextPrimary
                    )
                }
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NotionColors.TextPrimary
                )
                IconButton(onClick = { viewModel.onAction(ProfileAction.Logout) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        tint = NotionColors.TextSecondary
                    )
                }
            }
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NotionColors.Accent)
                }
            }
            state.user != null -> {
                ProfileContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileState,
    modifier: Modifier = Modifier
) {
    val user = state.user ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Avatar + name
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(NotionColors.SurfaceSecondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = NotionColors.TextSecondary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${user.firstName} ${user.lastName}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = NotionColors.TextPrimary
                )
                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NotionColors.TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Info card
        Text(
            text = "ACCOUNT",
            style = DockifyTextStyles.sectionHeader,
            color = NotionColors.TextTertiary
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, NotionColors.Divider, RoundedCornerShape(12.dp))
        ) {
            ProfileRow(label = "Email", value = user.email)
            Divider(color = NotionColors.Divider, thickness = 1.dp)
            ProfileRow(label = "Username", value = user.username)
        }
    }
}

@Composable
private fun ProfileRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = NotionColors.TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = NotionColors.TextPrimary
        )
    }
}
```

- [ ] **Step 2: Build**

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :composeApp:compileKotlinAndroid --quiet
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/auth/presentation/profile/ProfileScreen.kt
git commit -m "feat: add ProfileScreen composable"
```

---

### Task 7: Register ProfileViewModel in Koin and wire into App.kt

**Files:**
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/auth/presentation/profile/ProfilePresentationModule.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/di/AppModule.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/App.kt`

- [ ] **Step 1: Create `ProfilePresentationModule.kt`**

```kotlin
package io.diasjakupov.dockify.features.auth.presentation.profile

import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val profilePresentationModule = module {
    viewModelOf(::ProfileViewModel)
}
```

- [ ] **Step 2: Register in `AppModule.kt`**

`AppModule.kt` exposes a `fun appModules(): List<Module>` returning a `listOf(...)`. Add the import and append to the list:
```kotlin
import io.diasjakupov.dockify.features.auth.presentation.profile.profilePresentationModule

fun appModules(): List<Module> = listOf(
    // ... existing modules ...,
    profilePresentationModule
)
```

- [ ] **Step 3: Replace `PlaceholderScreen` for `ProfileRoute` in `App.kt`**

In `App.kt`, find the `ProfileRoute` entry:
```kotlin
// Before
is ProfileRoute -> NavEntry(key) {
    PlaceholderScreen(
        title = "Profile",
        onBack = { navigator.navigateBack() }
    )
}
```

Replace with:
```kotlin
// After
import io.diasjakupov.dockify.features.auth.presentation.profile.ProfileScreen

is ProfileRoute -> NavEntry(key) {
    ProfileScreen(
        onNavigateBack = { navigator.navigateBack() },
        onNavigateToLogin = { navigator.navigateToLogin() }
    )
}
```

- [ ] **Step 4: Ensure `navigateToLogin()` exists in `AppNavigator`**

Open `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/ui/navigation/AppNavigator.kt`.
Verify a method navigates to `LoginRoute` (clearing the back stack). If it doesn't exist, add:
```kotlin
fun navigateToLogin() {
    backStack.clear()
    backStack.add(LoginRoute)
}
```

- [ ] **Step 5: Full build**

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :composeApp:assembleDebug --quiet
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/auth/presentation/profile/ProfilePresentationModule.kt
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/di/AppModule.kt
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/App.kt
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/ui/navigation/AppNavigator.kt
git commit -m "feat: wire ProfileScreen into Koin and navigation"
```

---

## Summary of all changed files

| File | Change |
|------|--------|
| `features/location/data/datasource/LocationRemoteDataSourceImpl.kt` | Fix hospital path |
| `features/recommendation/data/datasource/RecommendationRemoteDataSource.kt` | Add `userId` param |
| `features/recommendation/data/datasource/RecommendationRemoteDataSourceImpl.kt` | Add `user_id` query param |
| `features/recommendation/domain/repository/RecommendationRepository.kt` | Add `userId` param |
| `features/recommendation/data/repository/RecommendationRepositoryImpl.kt` | Pass `userId` through |
| `features/recommendation/domain/usecase/GetRecommendationUseCase.kt` | Accept + validate `userId` |
| `features/health/presentation/HealthViewModel.kt` | Pass userId to recommendation use case |
| `features/health/di/HealthModule.kt` | Register `GetHealthMetricsUseCase` |
| `features/health/domain/model/HealthMetricType.kt` | Add `backendKey`, add 19 missing types |
| `features/health/data/mapper/HealthMetricMapper.kt` | Use `backendKey` in DTO conversion |
| `features/auth/presentation/profile/ProfileState.kt` | **NEW** — MVI state + actions + effects |
| `features/auth/presentation/profile/ProfileViewModel.kt` | **NEW** — loads user, handles logout |
| `features/auth/presentation/profile/ProfileScreen.kt` | **NEW** — UI composable |
| `features/auth/presentation/profile/ProfilePresentationModule.kt` | **NEW** — Koin module |
| `di/AppModule.kt` | Include `profilePresentationModule` |
| `App.kt` | Replace `PlaceholderScreen` for `ProfileRoute` |
| `ui/navigation/AppNavigator.kt` | Verify/add `navigateToLogin()` |
