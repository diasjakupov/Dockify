# Dockify — Claude Code Project Guide

Compose Multiplatform health tracking app (Android + iOS). Integrates with Health Connect and HealthKit, syncs to a backend server.

---

## Commands

```bash
# Android
./gradlew :composeApp:assembleDebug          # Build debug APK
./gradlew :composeApp:installDebug           # Build + install on connected device
./gradlew :composeApp:testDebugUnitTest      # Run unit tests

# iOS
open iosApp/iosApp.xcodeproj                 # Open in Xcode, then build from there

# Lint / checks
./gradlew :composeApp:lint
```

---

## Architecture

**Clean Architecture + MVI** — strict layer separation, unidirectional data flow.

```
Screen → onAction() → ViewModel.handleAction() → updateState() / emitEffect()
```

Layers (inner cannot depend on outer):
```
presentation  →  domain  ←  data
```

### Feature structure (every feature must follow this):
```
features/{feature}/
  data/
    datasource/     # interfaces + implementations
    dto/            # API/storage data models
    mapper/         # DTO → domain model converters
    repository/     # repository implementations
  di/               # Koin modules (data, domain, presentation split)
  domain/
    model/          # domain models (pure Kotlin, no framework deps)
    repository/     # repository interfaces
    usecase/        # one use case per class
  presentation/
    di/             # Koin presentation module
    {screen}/       # screen composable + UiState + UiAction + UiEffect
    components/     # screen-specific composables
```

---

## Backend Reference

The backend source lives at `dockify-backend/` (cloned from https://github.com/askaroe/dockify-backend).

**This directory is READ-ONLY. Never modify any file inside `dockify-backend/`.** It exists only as a reference for API contracts, endpoints, and data models. To restore read-only permissions if lost: `chmod -R a-w dockify-backend/`.

---

## Key Files

| File | Purpose |
|------|---------|
| `@composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/App.kt` | Root composable, NavDisplay setup |
| `@composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/di/AppModule.kt` | All Koin modules wired together |
| `@composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/ui/base/BaseViewModel.kt` | MVI ViewModel base class |
| `@composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/core/domain/Resource.kt` | Result type for all operations |
| `@composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/ui/navigation/Routes.kt` | All nav routes + SavedStateConfiguration |
| `@composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/ui/navigation/AppNavigator.kt` | Centralized navigator class |
| `@gradle/libs.versions.toml` | All dependency versions |

### Shared UI infrastructure:
- `@composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/ui/theme/` — theme, colors, typography, dimensions
- `@composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/ui/components/common/` — shared composables

---

## Technology Rules

### Navigation — Jetpack Navigation 3
> Full rules: `@.claude/rules/navigation3.md`

- **ONLY** use `navigation3-ui` + `lifecycle-viewmodel-navigation3`. No Jetpack Navigation 2.
- All routes must be `@Serializable data object/class` implementing `NavKey`.
- Always use `rememberNavBackStack(navSavedStateConfig, InitialRoute)` — never without config.
- All routes must be registered in `navSavedStateConfig` serializers module.
- Navigate only via `AppNavigator` (centralized). Never manipulate `backStack` directly in composables.
- Access navigator via `LocalAppNavigator.current` in screens.

### Dependency Injection — Koin 4
- Use `koin-compose-viewmodel` for ViewModels in composables.
- Split each feature into separate modules: `{feature}DataModule`, `{feature}DomainModule`, `{feature}PresentationModule`.
- Register all feature modules in `@composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/di/AppModule.kt`.
- No service locator pattern (`get()` calls) outside of Koin module definitions.

### Networking — Ktor 3
- All HTTP calls go through `SafeApiCall` wrapper.
- Configure client via `HttpClientFactory` — do not create `HttpClient` instances manually.
- Use `kotlinx-serialization` for all JSON. No Gson or Moshi.
- Inspektify is active in debug builds — network traffic is visible in the inspector.

### State Management — MVI
- Every screen ViewModel extends `BaseViewModel<S: UiState, A: UiAction, E: UiEffect>`.
- UI state: immutable `data class` implementing `UiState`.
- User interactions: sealed interface implementing `UiAction` — call `viewModel.onAction(action)`.
- One-time events (navigation, toasts): sealed interface implementing `UiEffect`, collected via `viewModel.effect`.
- Never expose `MutableStateFlow` or `MutableSharedFlow` from ViewModel.

### Result Handling — Resource<D, E>
- All repository and use case return types use `Resource<Data, DataError>`.
- Use `.onSuccess {}`, `.onError {}`, `.map {}` extension functions — no manual `when` unwrapping at data/domain layers.
- `EmptyResult<E>` = `Resource<Unit, E>` for operations with no return value.

### Storage — DataStore
- All persistent storage via `DataStoreFactory`. No SharedPreferences.
- Token / session data only via the auth data layer (`AuthLocalDataSource`).

### Coroutines & Flow
- All async work in `viewModelScope` via `BaseViewModel.launch {}` or `collectFlow {}`.
- Expose `StateFlow` for state, `Flow` via `Channel` for effects.
- No `GlobalScope` usage.

---

## Code Rules

- **Platform-specific code** goes in `androidMain` or `iosMain`, not in `commonMain`.
- **Domain models** must be pure Kotlin — no Android/iOS imports.
- **DTOs** live only in `data/dto/` — never pass DTOs to the domain or presentation layer.
- **Mappers** are the only classes allowed to convert between DTO ↔ domain model.
- **Use cases** are single-responsibility — one public operator `fun invoke()` per use case.
- **Composables** must not contain business logic — delegate everything to the ViewModel via actions.
- **No hardcoded strings** in composables — use string resources or constants.
- **No type parameters** on `rememberNavBackStack` — type is inferred.

---

## Platform Notes

| Platform | Min SDK | Notes |
|----------|---------|-------|
| Android | 26 | Health Connect requires Android 9+ (API 28) for full feature set |
| iOS | — | HealthKit permissions declared in `iosApp/` Info.plist |

- Health and location features require runtime permissions — handled in `features/{health,location}/permission/`.
- Platform entry points: `MainActivity.kt` (Android), `MainViewController.kt` (iOS).

---

## Workflow Rules

### Development Workflow — Explore → Plan → Implement → Simplify

Every non-trivial task must follow this pipeline in order:

1. **Explore** — Launch several `claude-haiku` agents in parallel to research the codebase (find relevant files, understand existing patterns, check dependencies). Do not write code yet.
2. **Plan** — Use the `claude-opus` model to produce a step-by-step implementation plan based on exploration findings. The plan must reference specific file paths and follow project architecture rules.
3. **Implement** — Execute the plan using the `claude-sonnet` model. Follow the plan; do not deviate without re-planning.
4. **Simplify** — Run the `code-simplifier` agent on all changed files. Fix any issues it surfaces before considering the task done.

Skip steps only for single-line fixes or config-only changes.

---

### Documentation — Keep Feature Docs in Sync
After any change to a feature (new files, modified behavior, new routes, DI changes, etc.):

1. Locate or create `features/{feature}/feature.md`.
2. Update it to reflect the current state — routes, use cases, data sources, DI modules, key behaviors.
3. Add or update an `@filepath` reference to it in `CLAUDE.md` under **Key Files** if it isn't listed yet.

**Feature doc template** (`features/{feature}/feature.md`):
```markdown
# {Feature} Feature

## Overview
One-sentence description of what this feature does.

## Routes
- `{Route}` — description

## Use Cases
- `{UseCase}` — what it does, inputs, outputs

## Data Sources
- Remote: `{RemoteDataSource}` — API endpoints used
- Local: `{LocalDataSource}` — what is persisted and where

## DI Modules
- `{DataModule}`, `{DomainModule}`, `{PresentationModule}`

## Key Behaviors / Gotchas
- Any non-obvious logic, edge cases, or platform differences
```

---

## Adding a New Feature

1. Create `features/{feature}/` with the full layer structure above.
2. Define domain models, repository interface, use cases in `domain/`.
3. Implement DTOs, mappers, data sources, repository in `data/`.
4. Add Koin modules (data + domain + presentation).
5. Register all modules in `AppModule.kt`.
6. Add route(s) to `Routes.kt` and register in `navSavedStateConfig`.
7. Add `NavEntry` branch in `App.kt` entry provider.
