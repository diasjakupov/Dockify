# Medical Documents — Mobile Client Design Spec

**Date:** 2026-03-10
**Platform:** Kotlin Multiplatform (Android + iOS)
**Phase:** 1 — Upload & Storage

---

## Overview

Allow users to upload medical documents (PDFs and images) from their device via camera or file picker. Documents are stored on the backend server and listed in a dedicated second tab.

---

## Decisions

| Decision | Choice |
|----------|--------|
| Nav placement | New bottom tab, position 2: Health → **Documents** → Nearby |
| File types | PDF + images |
| File source | Camera + file picker (both) |
| Document metadata | Free-form title only (no category for now) |
| Storage abstraction | None on mobile — calls backend REST API directly |

---

## Feature Structure

```
features/documents/
  data/
    datasource/
      DocumentRemoteDataSource.kt       # interface
      DocumentRemoteDataSourceImpl.kt   # Ktor multipart upload, list, delete
    dto/
      DocumentDto.kt
    mapper/
      DocumentMapper.kt
    repository/
      DocumentRepositoryImpl.kt
  di/
    DocumentsDataModule.kt
    DocumentsDomainModule.kt
    DocumentsPresentationModule.kt
  domain/
    model/
      Document.kt
      FileType.kt                       # enum: PDF, IMAGE
    repository/
      DocumentRepository.kt
    usecase/
      GetDocumentsUseCase.kt
      UploadDocumentUseCase.kt
      DeleteDocumentUseCase.kt
  platform/
    FilePicker.kt                       # expect class (commonMain)
  presentation/
    documents/
      DocumentsScreen.kt
      DocumentsViewModel.kt
      DocumentsUiState.kt
      DocumentsUiAction.kt
      DocumentsUiEffect.kt
    components/
      DocumentCard.kt
      UploadBottomSheet.kt
```

---

## Domain Models

```kotlin
// domain/model/FileType.kt
enum class FileType { PDF, IMAGE }

// domain/model/Document.kt
data class Document(
    val id: Int,
    val title: String,
    val fileUrl: String,
    val fileType: FileType,
    val fileSize: Long,
    val createdAt: LocalDateTime        // kotlinx-datetime
)

// domain/repository/DocumentRepository.kt
interface DocumentRepository {
    suspend fun getDocuments(): Resource<List<Document>, DataError>
    suspend fun uploadDocument(title: String, file: FilePickResult): Resource<Document, DataError>
    suspend fun deleteDocument(id: Int): EmptyResult<DataError>
}
```

---

## Platform File Picking (expect/actual)

```kotlin
// commonMain — platform/FilePicker.kt
data class FilePickResult(
    val bytes: ByteArray,
    val fileName: String,
    val mimeType: String
)

expect class FilePicker {
    suspend fun pickFile(): FilePickResult?
    suspend fun takePhoto(): FilePickResult?
}
```

- **Android actual:** Activity Result API — `GetContent` contract (gallery/files) + `TakePicture` contract (camera). Injected with `ComponentActivity` via Koin `androidContext()`.
- **iOS actual:** `PHPickerViewController` (gallery) + `UIImagePickerController` (camera). Wrapped in `suspendCancellableCoroutine`. No injected context needed.

> **Risk:** Android `ActivityResultLauncher` must be registered during `onCreate`. Evaluate composable-level launcher (`rememberLauncherForActivityResult`) vs Koin singleton approach before implementing Step 5. See Architectural Risks below.

> **Risk:** `ByteArray` in `FilePickResult` loads entire file into memory. For large files, consider holding a URI/path reference and reading bytes only at upload time.

---

## Data Layer

### DocumentDto

```kotlin
@Serializable
data class DocumentDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("file_url") val fileUrl: String,
    @SerialName("file_type") val fileType: String,   // "pdf" | "image"
    @SerialName("file_size") val fileSize: Long,
    @SerialName("created_at") val createdAt: String  // RFC 3339 string
)
```

### DocumentMapper

- `fileType` string → `FileType` enum (lowercase comparison)
- `createdAt` → parse as `Instant` first, then convert to `LocalDateTime` (handles the `Z` timezone suffix from Go's `time.Time`)

### DocumentRemoteDataSource

- `GET /api/v1/documents` — list user's documents
- `POST /api/v1/documents` — multipart form: `title` + `file`
- `DELETE /api/v1/documents/:id` — delete

**Important:** The `HttpClientFactory` has a global `defaultRequest { contentType(ContentType.Application.Json) }` which will conflict with multipart uploads. This must be removed — Ktor's `ContentNegotiation` plugin already sets `application/json` on serialized bodies.

---

## Error Types

Add to `core/domain/DataError.kt`:

```kotlin
enum class Document : DataError {
    UPLOAD_FAILED,
    DELETE_FAILED,
    FILE_TOO_LARGE,
    UNSUPPORTED_FILE_TYPE,
    NOT_FOUND
}
```

---

## MVI Contract

```kotlin
data class DocumentsUiState(
    val documents: List<Document> = emptyList(),
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val error: String? = null,
    val showUploadSheet: Boolean = false
) : UiState

sealed interface DocumentsUiAction : UiAction {
    data object LoadDocuments : DocumentsUiAction
    data class UploadDocument(val title: String, val file: FilePickResult) : DocumentsUiAction
    data class DeleteDocument(val id: Int) : DocumentsUiAction
    data object ShowUploadSheet : DocumentsUiAction
    data object HideUploadSheet : DocumentsUiAction
}

sealed interface DocumentsUiEffect : UiEffect {
    data class ShowError(val message: String) : DocumentsUiEffect
    data object UploadSuccess : DocumentsUiEffect
}
```

---

## Navigation Changes

- Add `DocumentsRoute` (`@Serializable data object`) to `Routes.kt`
- Register in `navSavedStateConfig` polymorphic block
- Update `TopLevelDestination` enum: insert `DOCUMENTS` between `HEALTH` and `NEARBY`
- Update `BottomNavItem` enum in `MainScaffold.kt`: use `Icons.Filled.Description` (or `Icons.Filled.Article` if Description not available)
- Add `navigateToDocuments()` to `AppNavigator`
- Add `NavEntry` branch for `DocumentsRoute` in `App.kt` (wraps `DocumentsScreen` in `MainScaffoldScreen`)

---

## DI Modules

| Module | Contents |
|--------|----------|
| `DocumentsDataModule` (commonMain) | `DocumentRemoteDataSource`, `DocumentRepository` |
| `DocumentsDomainModule` (commonMain) | All three use cases (factory scope) |
| `DocumentsPresentationModule` (commonMain) | `DocumentsViewModel` via `viewModelOf` |
| `DocumentsAndroidModule` (androidMain) | `FilePicker(activity)` |
| `DocumentsIosModule` (iosMain) | `FilePicker()` |

Register common modules in `AppModule.kt`. Register platform modules in `DockifyApplication.kt` (Android) and `KoinHelper.kt` (iOS).

---

## Implementation Order

```
1.  DataError.Document enum
2.  FilePickResult + expect FilePicker (commonMain)
3.  Android actual FilePicker
4.  iOS actual FilePicker
5.  Document + FileType domain models
6.  DocumentRepository interface
7.  GetDocumentsUseCase, UploadDocumentUseCase, DeleteDocumentUseCase
8.  DocumentDto
9.  DocumentMapper
10. DocumentRemoteDataSource interface + impl
11. Fix HttpClientFactory Content-Type conflict
12. DocumentRepositoryImpl
13. DI modules (data, domain, presentation, android, ios)
14. Register modules in AppModule, Application, KoinHelper
15. MVI contracts (UiState, UiAction, UiEffect)
16. DocumentsViewModel
17. DocumentCard component
18. UploadBottomSheet component
19. DocumentsScreen
20. Navigation: Routes.kt, MainScaffold.kt, AppNavigator.kt, App.kt
21. feature.md + CLAUDE.md update
```

Steps 1, 2, 5 can be done in parallel. Steps 3 and 4 can be done in parallel.

---

## Architectural Risks

| Risk | Severity | Mitigation |
|------|----------|------------|
| Android `ActivityResultLauncher` lifecycle | High | Evaluate composable-level launcher before implementing `FilePicker` actual |
| `ByteArray` in memory for large files | Medium | Use URI reference in `FilePickResult`, read bytes only at upload time |
| `HttpClientFactory` Content-Type conflict breaks multipart | High | Remove global `contentType(ContentType.Application.Json)` from `defaultRequest` in both androidMain and iosMain |
| Go's RFC 3339 `Z` suffix not parsed by `LocalDateTime.parse()` | Medium | Use `Instant.parse().toLocalDateTime(TimeZone.currentSystemDefault())` in mapper |
| `Icons.Filled.Description` may not be in bundled icons | Low | Fall back to `Icons.Default.Folder` or check `material-icons-extended` dependency |
| Backend endpoints don't exist yet | Critical | Client will get 404s until backend is deployed; coordinate releases |
| Login response will change shape (JWT added) | High | Client must be updated to store JWT token and send `Authorization: Bearer` header |

---

## Out of Scope (Future Phases)

- AI analysis of document content
- Checkup reminders / notifications
- PDF export of aggregated medical data
- Document categorization / auto-classification
- Document detail / preview screen
- Offline support / local caching
