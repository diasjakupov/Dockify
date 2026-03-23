# Swipe-to-Delete on Documents Tab

**Date:** 2026-03-23
**Status:** Approved

---

## Overview

Add an iOS-style swipe-left gesture to `DocumentItem` in the Documents tab. A partial swipe settles and reveals a red delete zone with a trash icon; continuing to swipe fully across the row deletes the document immediately without a confirmation dialog.

---

## Gesture & Anchors

`DocumentItem` uses `AnchoredDraggableState` from **`androidx.compose.foundation`** (not Material 3). The opt-in annotation is `@ExperimentalFoundationApi`, which `DocumentItem.kt` already carries.

Three anchors on the horizontal axis:

| Anchor | Offset | Description |
|--------|--------|-------------|
| `Idle` | `0f` | Default — no swipe visible |
| `Revealed` | `-80.dp.toPx()` | Settles here on partial swipe; shows trash icon |
| `Dismissed` | `-fullItemWidth` | Full swipe across the entire row |

**State construction:**

```kotlin
private enum class SwipeAnchor { Idle, Revealed, Dismissed }

val state = remember {
    AnchoredDraggableState(
        initialValue = SwipeAnchor.Idle,
        positionalThreshold = { totalDistance -> totalDistance * 0.5f },
        velocityThreshold = { with(density) { 125.dp.toPx() } }
    )
}
```

> Note: `snapAnimationSpec` and `decayAnimationSpec` may be constructor params or modifier params depending on the exact Foundation version bundled with CMP 1.10.0-beta01. The implementer should verify the actual `AnchoredDraggableState` signature and pass `spring(stiffness = Spring.StiffnessMediumLow)` and `exponentialDecay()` wherever the API expects them.

**Anchor assignment** happens inside `Modifier.onSizeChanged` on the foreground layer, after the pixel width is known:

```kotlin
.onSizeChanged { size ->
    val revealedOffset = -with(density) { 80.dp.toPx() }
    val dismissedOffset = -size.width.toFloat()
    state.updateAnchors(
        DraggableAnchors {
            SwipeAnchor.Idle at 0f
            SwipeAnchor.Revealed at revealedOffset
            SwipeAnchor.Dismissed at dismissedOffset
        }
    )
}
```

**Enabled guard:** The `anchoredDraggable` modifier receives the same `enabled` value as `combinedClickable`:

```kotlin
.anchoredDraggable(
    state = state,
    orientation = Orientation.Horizontal,
    enabled = enabled
)
```

---

## Visual Design

The item is restructured as follows:

```
Box(
    modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))   // ← clips both layers to card shape
) {
    // 1. Background layer (static, aligned to end)
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
            Icon(Icons.Filled.Delete, tint = Color.White, ...)
        }
    }

    // 2. Foreground layer (slides left)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { ... }           // ← registers anchors
            .offset { IntOffset(state.requireOffset().roundToInt(), 0) }
            .anchoredDraggable(state, Orientation.Horizontal, enabled)
            .combinedClickable(
                enabled = enabled,
                onClick = { ... },           // ← see Interaction section
                onLongClick = { showDelete = true }
            ),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) { /* existing row content unchanged */ }
}
```

The outer `Box` is clipped to `RoundedCornerShape(12.dp)` so the red zone never bleeds outside the card's rounded corners.

The `combinedClickable` moves from the old `Surface` modifier position to the same `Surface` modifier chain (it was already on `Surface`; the refactor just adds the new modifiers in the correct order: `onSizeChanged` → `offset` → `anchoredDraggable` → `combinedClickable`).

---

## Interaction Behaviour

### Conflict with long-press `showDelete` state

When the user starts dragging, `showDelete` must be collapsed to prevent the long-press trash icon and the swipe background from being visible simultaneously.

**Concrete mechanism:** Wrap the foreground `Surface` in a `pointerInput(Unit)` modifier placed before `anchoredDraggable`. Inside, use `detectHorizontalDragGestures` with an `onDragStart` callback that sets `showDelete = false`, then forward the drag events to the `AnchoredDraggableState` manually — or, simpler, use `Modifier.draggable` instead of `detectHorizontalDragGestures`. The simplest correct approach:

Add a `LaunchedEffect(state.currentValue)`:
```kotlin
LaunchedEffect(state.currentValue) {
    if (state.currentValue != SwipeAnchor.Idle) {
        showDelete = false
    }
}
```
This fires whenever the anchor changes (including mid-drag when value updates), ensuring `showDelete` collapses as soon as any swipe movement is registered.

### Revealed anchor

The foreground settles at `-80dp`, exposing the red trash zone.

**Tapping the trash icon** in the background layer:
```kotlin
IconButton(onClick = {
    scope.launch { state.animateTo(SwipeAnchor.Idle) }
    onDelete() // → RequestDeleteDocument(id) → shows confirmation dialog
})
```
The swipe resets to `Idle` as part of the click handler directly — no `LaunchedEffect` needed.

**Tapping anywhere on the foreground** while `Revealed`:
```kotlin
onClick = {
    when {
        state.currentValue == SwipeAnchor.Revealed ->
            scope.launch { state.animateTo(SwipeAnchor.Idle) }
        showDelete -> showDelete = false
        else -> onOpen()
    }
}
```

### Dismissed anchor

The foreground has swiped fully off screen.

- No confirmation dialog — the full swipe is intentional.
- `LaunchedEffect(state.settledValue)` fires once the animation fully completes:
  ```kotlin
  LaunchedEffect(state.settledValue) {
      if (state.settledValue == SwipeAnchor.Dismissed) {
          onSwipeDelete() // → SwipeDeleteDocument(id) → ViewModel calls deleteDocument(id)
      }
  }
  ```
- The ViewModel removes the document from `state.documents` on success. The `LazyColumn` uses `key = { it.id }`, so removal is targeted. The `Modifier.animateItem()` on the item in `DocumentsScreen` provides the built-in collapse/fade exit — no manual height animation needed.
- **Double-fire safety:** `settledValue` stays `Dismissed` until the composable is removed from the list (ViewModel success path). The `LaunchedEffect` key does not change between recompositions while `settledValue` remains `Dismissed`, so it fires exactly once. If the delete fails, the item stays in the list at the `Dismissed` visual position — the spec does not define recovery UX for this case (a snackbar is shown by existing error handling in `deleteDocument()`). Future work can add a "snap back on error" behavior.

---

## State Changes

### `DocumentsAction.kt`
Add:
```kotlin
/** Full swipe-to-delete — skips confirmation dialog */
data class SwipeDeleteDocument(val id: String) : DocumentsAction
```
The existing `RequestDeleteDocument` / `ConfirmDeleteDocument` / `CancelDeleteDocument` flow is **unchanged**. The new action is an independent path.

### `DocumentsViewModel.kt`
Add one branch to `handleAction`:
```kotlin
is DocumentsAction.SwipeDeleteDocument -> deleteDocument(action.id)
```
`deleteDocument(id)` already handles success (removes from list) and error (shows snackbar). No additional error handling needed in the new branch.

### `DocumentItem.kt`
- Add `onSwipeDelete: () -> Unit` parameter.
- Add private `enum class SwipeAnchor { Idle, Revealed, Dismissed }`.
- Restructure layout: outer clipped `Box` → static background → sliding foreground `Surface`.
- `AnchoredDraggableState<SwipeAnchor>` in `remember`, anchors via `onSizeChanged`.
- `LaunchedEffect(state.settledValue)` handles `Dismissed` → `onSwipeDelete()`.
- `LaunchedEffect(state.currentValue)` collapses `showDelete` when not `Idle`.
- `onClick` updated to handle `Revealed` → snap back.

### `DocumentsScreen.kt`
- Pass `onSwipeDelete` to `DocumentItem`.
- Add `Modifier.animateItem()` to the `DocumentItem` call in `items` block (requires `@OptIn(ExperimentalFoundationApi::class)` on `DocumentsScreen` or its containing function — add if not already present).

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

---

## Files Changed

| File | Change |
|------|--------|
| `features/documents/presentation/components/DocumentItem.kt` | Add `AnchoredDraggable`, clipped outer `Box`, background layer, offset+drag on foreground, `onSwipeDelete` param, `showDelete` conflict resolution |
| `features/documents/presentation/documents/DocumentsAction.kt` | Add `SwipeDeleteDocument(val id: String)` |
| `features/documents/presentation/documents/DocumentsViewModel.kt` | Handle `SwipeDeleteDocument` → `deleteDocument(id)` |
| `features/documents/presentation/documents/DocumentsScreen.kt` | Pass `onSwipeDelete`, add `Modifier.animateItem()`, add `@OptIn(ExperimentalFoundationApi::class)` if needed |

---

## Out of Scope

- No changes to the data layer, repository, or use cases — `DeleteDocumentUseCase` is already wired.
- No changes to the long-press delete flow or confirmation dialog.
- No haptic feedback (can be added later).
- No undo/snackbar after swipe-delete (can be added later).
- No "snap back on delete error" recovery UX (can be added later).
