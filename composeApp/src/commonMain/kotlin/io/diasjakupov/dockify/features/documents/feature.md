# Documents Feature

## Overview
Allows authenticated users to upload, view, download, and delete documents stored on the backend.

## Routes
- Documents tab in main bottom navigation — no dedicated route, embedded in main scaffold

## Use Cases
- `GetDocumentsUseCase` — fetches all documents for the current user
- `UploadDocumentUseCase` — uploads a file (max 10MB); validates size before sending
- `DeleteDocumentUseCase` — deletes a document by ID
- `DownloadDocumentUseCase` — downloads raw bytes for a document by ID

## Data Sources
- Remote: `DocumentRemoteDataSourceImpl` — GET /api/v1/documents, POST /api/v1/documents/upload, DELETE /api/v1/documents/:id, GET /api/v1/documents/:id/download

## DI Modules
- `DocumentDataModule`, `DocumentDomainModule`, `DocumentPresentationModule`

## Key Behaviors / Gotchas
- **File picker:** Supports camera, gallery, and file picker (pdf, docx, xlsx, txt, png, jpg, jpeg). Max file size 10MB enforced in `UploadDocumentUseCase`.
- **Swipe-to-delete:** `DocumentItem` uses `AnchoredDraggableState` (Compose Foundation, `@ExperimentalFoundationApi`) with three anchors:
  - `Idle` (0f) — default state
  - `Revealed` (-80dp) — partial swipe settles here, shows red trash icon zone
  - `Dismissed` (-fullWidth) — full swipe triggers `SwipeDeleteDocument` action → direct delete, no confirmation dialog
  - Partial swipe → tap trash icon → `RequestDeleteDocument` → confirmation dialog (existing flow)
  - `swipeDeleteDispatched` flag prevents double-fire of `onSwipeDelete()` on recomposition
- **Long-press delete:** Long press on a `DocumentItem` shows an animated trash icon inside the row → tap it → `RequestDeleteDocument` → confirmation dialog
- The `anchoredDraggable` modifier in CMP 1.10.0-beta01 does not expose `snapAnimationSpec`/`decayAnimationSpec` externally; snap uses framework defaults.
