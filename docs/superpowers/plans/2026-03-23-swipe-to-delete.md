# Swipe-to-Delete Documents Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an iOS-style swipe-left gesture to `DocumentItem` so a partial swipe reveals a red delete zone and a full swipe deletes the document directly.

**Architecture:** `AnchoredDraggableState` (Compose Foundation) drives a three-anchor drag on the horizontal axis (Idle → Revealed → Dismissed). The foreground `Surface` slides left while a static red background is revealed. A new `SwipeDeleteDocument` action bypasses the existing confirmation dialog and calls `deleteDocument` directly.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform 1.10.0-beta01, `androidx.compose.foundation.AnchoredDraggableState` (`@ExperimentalFoundationApi`), MVI with `BaseViewModel`.

---

## File Map

| File | Action | Change |
|------|--------|--------|
| `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/documents/presentation/documents/DocumentsAction.kt` | Modify | Add `SwipeDeleteDocument(val id: String)` |
| `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/documents/presentation/documents/DocumentsViewModel.kt` | Modify | Handle `SwipeDeleteDocument → deleteDocument(id)` |
| `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/documents/presentation/components/DocumentItem.kt` | Modify | Add swipe gesture, restructure layout |
| `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/documents/presentation/documents/DocumentsScreen.kt` | Modify | Pass `onSwipeDelete`, add `Modifier.animateItem()` |

---

## Task 1: Add `SwipeDeleteDocument` to the action sealed interface

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/documents/presentation/documents/DocumentsAction.kt`

- [ ] **Step 1: Add the new action**

Open `DocumentsAction.kt`. Add this entry at the end of the sealed interface (after `FilePickerDismissed`):

```kotlin
/** Full swipe-to-delete — skips confirmation dialog, deletes immediately */
data class SwipeDeleteDocument(val id: String) : DocumentsAction
```

The file now has 14 entries in the sealed interface. Do not remove or change any existing entries.

- [ ] **Step 2: Build to confirm no compile errors**

```bash
cd /Users/diasjakupov/Desktop/Dockify/Dockify
./gradlew :composeApp:compileKotlinAndroid 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`. If you see an error about `when` expression not being exhaustive, proceed to Task 2 immediately (the `DocumentsViewModel.handleAction` needs the new branch).

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/documents/presentation/documents/DocumentsAction.kt
git commit -m "feat(documents): add SwipeDeleteDocument action"
```

---

## Task 2: Handle `SwipeDeleteDocument` in `DocumentsViewModel`

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/documents/presentation/documents/DocumentsViewModel.kt`

- [ ] **Step 1: Add handler branch**

In `DocumentsViewModel.handleAction`, the `when` block covers all existing actions. Add this branch directly after the `CancelDeleteDocument` branch (line ~47):

```kotlin
is DocumentsAction.SwipeDeleteDocument -> deleteDocument(action.id)
```

The `deleteDocument(id)` private function already:
- Removes the document from `state.documents` on success
- Shows a snackbar on error via `emitEffect(DocumentsEffect.ShowSnackbar(message))`

No additional error handling is needed in the new branch.

- [ ] **Step 2: Build to confirm exhaustive `when` is satisfied**

```bash
./gradlew :composeApp:compileKotlinAndroid 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Run existing unit tests to confirm nothing broke**

```bash
./gradlew :composeApp:testDebugUnitTest 2>&1 | tail -30
```

Expected: all tests pass (same count as before).

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/documents/presentation/documents/DocumentsViewModel.kt
git commit -m "feat(documents): handle SwipeDeleteDocument in ViewModel"
```

---

## Task 3: Restructure `DocumentItem` with `AnchoredDraggable`

This is the largest change. Read the existing file carefully before editing — the existing row content (left accent stripe, badge, text column, animated delete icon) stays unchanged inside the `Surface`.

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/documents/presentation/components/DocumentItem.kt`

- [ ] **Step 1: Add required imports**

Add these imports at the top of `DocumentItem.kt` (merge with existing import block):

```kotlin
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.AnchoredDraggableState
import androidx.compose.foundation.DraggableAnchors
import androidx.compose.foundation.anchoredDraggable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
```

> **API note:** `AnchoredDraggableState` lives in `androidx.compose.foundation` and is `@ExperimentalFoundationApi` — the file already carries `@OptIn(ExperimentalFoundationApi::class)` so no new annotation is needed. If any import causes "unresolved reference", check the exact package path in your IDE's autocomplete against the bundled CMP 1.10.0-beta01 Foundation.

- [ ] **Step 2: Add the `SwipeAnchor` enum at the bottom of the file**

Add this private enum *after* the existing private helpers (`FileTypeInfo`, `fileTypeInfo`, `toReadableSize`):

```kotlin
private enum class SwipeAnchor { Idle, Revealed, Dismissed }
```

- [ ] **Step 3: Add `onSwipeDelete` parameter to `DocumentItem`**

Change the function signature from:
```kotlin
fun DocumentItem(
    document: Document,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
)
```
to:
```kotlin
fun DocumentItem(
    document: Document,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onSwipeDelete: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
)
```

- [ ] **Step 4: Set up `AnchoredDraggableState` inside the composable**

Add these lines at the top of the `DocumentItem` function body, after `var showDelete by remember { mutableStateOf(false) }` and `val typeInfo = fileTypeInfo(document.contentType)`:

```kotlin
val density = LocalDensity.current
val scope = rememberCoroutineScope()

val swipeState = remember {
    AnchoredDraggableState(
        initialValue = SwipeAnchor.Idle,
        positionalThreshold = { totalDistance -> totalDistance * 0.5f },
        velocityThreshold = { with(density) { 125.dp.toPx() } },
        snapAnimationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        decayAnimationSpec = exponentialDecay()
    )
}
```

> **If the above constructor does not compile** (animation specs not accepted in constructor): remove `snapAnimationSpec` and `decayAnimationSpec` from the constructor and pass them to the `anchoredDraggable` modifier call in Step 6 instead: `.anchoredDraggable(swipeState, Orientation.Horizontal, enabled, snapAnimationSpec = spring(...), decayAnimationSpec = exponentialDecay())`.

- [ ] **Step 5: Add `LaunchedEffect` blocks**

Add two `LaunchedEffect` blocks after the `swipeState` declaration:

```kotlin
// Collapse long-press delete icon when swiping begins
LaunchedEffect(swipeState.currentValue) {
    if (swipeState.currentValue != SwipeAnchor.Idle) {
        showDelete = false
    }
}

// Trigger direct delete after full swipe settles
LaunchedEffect(swipeState.settledValue) {
    if (swipeState.settledValue == SwipeAnchor.Dismissed) {
        onSwipeDelete()
    }
}
```

- [ ] **Step 6: Restructure the layout**

Replace the existing top-level `Surface(modifier = modifier ...)` with the following structure. The content *inside* the `Surface` row (accent stripe, badge, column, animated delete icon, spacer) is **not changed** — only the outer wrapping changes:

```kotlin
Box(
    modifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
) {
    // ── Background layer (static, always behind foreground) ──────────────
    Box(
        modifier = Modifier.matchParentSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .width(80.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.error),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = {
                    scope.launch { swipeState.animateTo(SwipeAnchor.Idle) }
                    onDelete()
                },
                enabled = enabled
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    // ── Foreground layer (slides left) ───────────────────────────────────
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { size ->
                val revealedPx = -with(density) { 80.dp.toPx() }
                val dismissedPx = -size.width.toFloat()
                swipeState.updateAnchors(
                    DraggableAnchors {
                        SwipeAnchor.Idle at 0f
                        SwipeAnchor.Revealed at revealedPx
                        SwipeAnchor.Dismissed at dismissedPx
                    }
                )
            }
            .offset { IntOffset(swipeState.requireOffset().roundToInt(), 0) }
            .anchoredDraggable(
                state = swipeState,
                orientation = Orientation.Horizontal,
                enabled = enabled
            )
            .combinedClickable(
                enabled = enabled,
                onClick = {
                    when {
                        swipeState.currentValue == SwipeAnchor.Revealed ->
                            scope.launch { swipeState.animateTo(SwipeAnchor.Idle) }
                        showDelete -> showDelete = false
                        else -> onOpen()
                    }
                },
                onLongClick = { showDelete = true }
            ),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        // ── Existing row content — DO NOT CHANGE ─────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left accent stripe
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(typeInfo.accentColor)
            )
            Spacer(Modifier.width(12.dp))
            // File type badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(typeInfo.accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = typeInfo.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = typeInfo.accentColor
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 14.dp)
            ) {
                Text(
                    text = document.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "${document.fileSize.toReadableSize()} · ${document.uploadedAt.take(10)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AnimatedVisibility(
                visible = showDelete,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 0.8f)
            ) {
                IconButton(
                    onClick = { showDelete = false; onDelete() },
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            if (!showDelete) Spacer(Modifier.width(8.dp))
        }
        // ── End existing row content ──────────────────────────────────────
    }
}
```

- [ ] **Step 7: Build**

```bash
./gradlew :composeApp:compileKotlinAndroid 2>&1 | tail -30
```

Expected: `BUILD SUCCESSFUL`. Common errors and fixes:
- "Unresolved: AnchoredDraggableState" → wrong import package; check IDE autocomplete for correct `androidx.compose.foundation` path
- "None of the following candidates…" on constructor → remove `snapAnimationSpec`/`decayAnimationSpec` from constructor (see Step 4 note)
- "Unresolved: DraggableAnchors" → same package as `AnchoredDraggableState`; add import

- [ ] **Step 8: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/documents/presentation/components/DocumentItem.kt
git commit -m "feat(documents): add swipe-to-delete gesture to DocumentItem"
```

---

## Task 4: Wire `onSwipeDelete` and `animateItem` in `DocumentsScreen`

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/documents/presentation/documents/DocumentsScreen.kt`

- [ ] **Step 1: Add `@OptIn` annotation if needed**

Check whether `DocumentsScreen.kt` already has `@OptIn(ExperimentalFoundationApi::class)`. If not, add it to the `DocumentsScreen` composable function:

```kotlin
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DocumentsScreen() {
```

Also add this import if not present:
```kotlin
import androidx.compose.foundation.ExperimentalFoundationApi
```

- [ ] **Step 2: Update the `items` block**

Find this block (around line 189–196):

```kotlin
items(state.documents, key = { it.id }) { document ->
    DocumentItem(
        document = document,
        onOpen = { viewModel.onAction(DocumentsAction.OpenDocument(document)) },
        onDelete = { viewModel.onAction(DocumentsAction.RequestDeleteDocument(document.id)) },
        enabled = !state.isUploading
    )
}
```

Replace with:

```kotlin
items(state.documents, key = { it.id }) { document ->
    DocumentItem(
        document = document,
        onOpen = { viewModel.onAction(DocumentsAction.OpenDocument(document)) },
        onDelete = { viewModel.onAction(DocumentsAction.RequestDeleteDocument(document.id)) },
        onSwipeDelete = { viewModel.onAction(DocumentsAction.SwipeDeleteDocument(document.id)) },
        enabled = !state.isUploading,
        modifier = Modifier.animateItem()
    )
}
```

- [ ] **Step 3: Build**

```bash
./gradlew :composeApp:compileKotlinAndroid 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Run all unit tests**

```bash
./gradlew :composeApp:testDebugUnitTest 2>&1 | tail -30
```

Expected: all existing tests still pass.

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/documents/presentation/documents/DocumentsScreen.kt
git commit -m "feat(documents): wire onSwipeDelete and animateItem in DocumentsScreen"
```

---

## Task 5: Manual verification checklist

Install on a device or emulator and verify:

- [ ] **Partial swipe left** → row slides left, red zone with white trash icon becomes visible, row settles at ~80dp revealed
- [ ] **Tap trash icon in revealed zone** → row snaps back to Idle, confirmation dialog appears → tap Delete → document removed from list
- [ ] **Tap anywhere on foreground while revealed** → row snaps back to Idle, no delete triggered
- [ ] **Full swipe left (fast or slow)** → row slides fully off, document disappears from list with collapse animation, no dialog shown
- [ ] **Long press** → existing trash icon appears inside row → tap it → confirmation dialog (unchanged behavior)
- [ ] **Long press then swipe** → trash icon collapses as soon as swipe starts
- [ ] **Swipe while uploading** (`isUploading = true`) → swipe gesture should be disabled (no movement)
- [ ] **Swipe on iOS** → same behavior (test in Xcode simulator)

```bash
# Install on connected Android device
./gradlew :composeApp:installDebug
```

---

## Task 6: Update feature documentation

**Files:**
- Modify or create: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/documents/feature.md`

- [ ] **Step 1: Update or create `feature.md`**

Add a "Key Behaviors" entry:

```markdown
## Key Behaviors / Gotchas
- **Swipe-to-delete:** `DocumentItem` uses `AnchoredDraggableState` (Foundation) with three anchors:
  `Idle` → `Revealed` (-80dp, shows red delete zone) → `Dismissed` (full width, triggers `SwipeDeleteDocument`).
  Full swipe bypasses confirmation dialog. Partial swipe tap uses existing `RequestDeleteDocument` dialog flow.
- `SwipeDeleteDocument` action calls `deleteDocument(id)` directly in the ViewModel — no `pendingDeleteId` involved.
```

- [ ] **Step 2: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/documents/feature.md
git commit -m "docs(documents): document swipe-to-delete behavior"
```
