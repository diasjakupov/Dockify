# Swipe-to-Delete on Documents Tab

**Date:** 2026-03-23
**Status:** Approved

---

## Overview

Add an iOS-style swipe-left gesture to `DocumentItem` in the Documents tab. A partial swipe settles and reveals a red delete zone with a trash icon; continuing to swipe fully across the row deletes the document immediately without a confirmation dialog.

---

## Gesture & Anchors

`DocumentItem` uses `AnchoredDraggableState` (Material 3 experimental API) with three anchors on the horizontal axis:

| Anchor | Offset | Description |
|--------|--------|-------------|
| `Idle` | `0f` | Default — no swipe visible |
| `Revealed` | `-80.dp.toPx()` | Settles here on partial swipe; shows trash icon |
| `Dismissed` | `-fullWidth` | Full swipe across the entire row |

- **Positional threshold:** 50% of the distance between adjacent anchors
- **Velocity threshold:** 125 dp/s — a fast fling can jump from `Idle` directly to `Dismissed`
- **Snap animation:** `spring(stiffness = Spring.StiffnessMediumLow)`
- **Decay animation:** `exponentialDecay()`

The full item width is captured via `onSizeChanged` on the foreground layer and fed into `updateAnchors`.

---

## Visual Design

Each `DocumentItem` is wrapped in a `Box` with two layers:

1. **Background (static):** A `Box` filling the full width and height, containing a red `Container` aligned to the end. The red zone is always 80dp wide and spans the full height. A white trash icon is centered within it.

2. **Foreground (slides):** The existing `Surface` row. Its horizontal offset is driven by `state.requireOffset()` via `Modifier.offset { IntOffset(offset.roundToInt(), 0) }`. The `anchoredDraggable` modifier is attached here with `Orientation.Horizontal`.

The red background is always in the composition — it becomes visible as the foreground slides left.

---

## Interaction Behaviour

### Revealed anchor
- The foreground settles at `-80dp`, exposing the red trash zone.
- **Tapping the trash icon** → calls `onDelete()` → `RequestDeleteDocument(id)` → shows existing confirmation dialog (same as long-press flow). The swipe state resets to `Idle` via `LaunchedEffect` after the action fires.
- **Tapping anywhere else on the foreground** → resets to `Idle` (existing `onClick` handler already handles this: if `showDelete` is true it collapses — we extend this to also reset swipe state).

### Dismissed anchor
- The foreground has swiped fully off screen.
- No confirmation dialog. The swipe gesture is considered intentional.
- A `LaunchedEffect` watching `state.currentValue == Dismissed` triggers:
  1. Animate item height from natural size to `0dp` (collapse animation, ~200ms).
  2. Call `onSwipeDelete()` → dispatches `SwipeDeleteDocument(id)` to the ViewModel.
- The ViewModel handles `SwipeDeleteDocument` by calling `deleteDocument(id)` directly (no `pendingDeleteId` involved).

---

## State Changes

### `DocumentsAction.kt`
Add:
```kotlin
data class SwipeDeleteDocument(val id: String) : DocumentsAction
```

### `DocumentsViewModel.kt`
Add handler:
```kotlin
is DocumentsAction.SwipeDeleteDocument -> deleteDocument(action.id)
```

### `DocumentItem.kt`
- Add `onSwipeDelete: () -> Unit` parameter.
- Internal: `AnchoredDraggableState<SwipeAnchor>` where `SwipeAnchor` is a private `enum class { Idle, Revealed, Dismissed }`.
- Layout: `Box` → background layer + foreground layer with `anchoredDraggable`.
- `LaunchedEffect(state.currentValue)` handles post-settle side effects.

### `DocumentsScreen.kt`
Pass `onSwipeDelete`:
```kotlin
DocumentItem(
    document = document,
    onOpen = { viewModel.onAction(DocumentsAction.OpenDocument(document)) },
    onDelete = { viewModel.onAction(DocumentsAction.RequestDeleteDocument(document.id)) },
    onSwipeDelete = { viewModel.onAction(DocumentsAction.SwipeDeleteDocument(document.id)) },
    enabled = !state.isUploading
)
```

---

## Files Changed

| File | Change |
|------|--------|
| `features/documents/presentation/components/DocumentItem.kt` | Add `AnchoredDraggable`, background layer, offset on foreground, `onSwipeDelete` param |
| `features/documents/presentation/documents/DocumentsAction.kt` | Add `SwipeDeleteDocument(val id: String)` |
| `features/documents/presentation/documents/DocumentsViewModel.kt` | Handle `SwipeDeleteDocument` → call `deleteDocument(id)` |
| `features/documents/presentation/documents/DocumentsScreen.kt` | Pass `onSwipeDelete` callback to `DocumentItem` |

---

## Out of Scope

- No changes to the data layer, repository, or use cases — `DeleteDocumentUseCase` is already wired.
- No changes to the long-press delete flow or confirmation dialog.
- No haptic feedback (can be added later).
- No undo/snackbar after swipe-delete (can be added later).
