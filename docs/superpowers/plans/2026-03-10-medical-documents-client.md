# Medical Documents — Mobile Client Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a Documents tab to the Dockify app where users can upload medical files (PDFs + images) via camera or file picker to the backend server.

**Architecture:** Full Clean Architecture + MVI — domain layer (models, repository interface, use cases), data layer (DTO, mapper, Ktor multipart data source, repository impl), platform layer (expect/actual FilePicker), and presentation layer (MVI ViewModel + Compose screens). Documents becomes the 2nd bottom nav tab.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Ktor 3 (multipart), Koin 4, Navigation 3, kotlinx-datetime, kotlinx-serialization

**Spec:** `docs/superpowers/specs/client/2026-03-10-medical-documents-design.md`

**Prerequisite:** Backend implementation plan must be complete and backend deployed before the upload/list calls will succeed. The JWT token returned by login must be stored in DataStore and sent as `Authorization: Bearer <token>` in all API requests.

---

## File Map

| File | Action | Responsibility |
|------|--------|----------------|
| `composeApp/src/commonMain/.../core/domain/DataError.kt` | Modify | Add `Document` error enum |
| `composeApp/src/commonMain/.../features/documents/domain/model/FileType.kt` | Create | `FileType` enum (PDF, IMAGE) |
| `composeApp/src/commonMain/.../features/documents/domain/model/Document.kt` | Create | `Document` domain model |
| `composeApp/src/commonMain/.../features/documents/domain/repository/DocumentRepository.kt` | Create | Repository interface |
| `composeApp/src/commonMain/.../features/documents/domain/usecase/GetDocumentsUseCase.kt` | Create | Fetch list use case |
| `composeApp/src/commonMain/.../features/documents/domain/usecase/UploadDocumentUseCase.kt` | Create | Upload use case with title validation |
| `composeApp/src/commonMain/.../features/documents/domain/usecase/DeleteDocumentUseCase.kt` | Create | Delete use case |
| `composeApp/src/commonMain/.../features/documents/platform/FilePicker.kt` | Create | `FilePickResult` data class + `expect class FilePicker` |
| `composeApp/src/androidMain/.../features/documents/platform/FilePicker.kt` | Create | Android actual using Activity Result API |
| `composeApp/src/iosMain/.../features/documents/platform/FilePicker.kt` | Create | iOS actual using PHPickerViewController |
| `composeApp/src/commonMain/.../features/documents/data/dto/DocumentDto.kt` | Create | JSON DTO (kotlinx-serialization) |
| `composeApp/src/commonMain/.../features/documents/data/mapper/DocumentMapper.kt` | Create | DTO → domain model conversion |
| `composeApp/src/commonMain/.../features/documents/data/datasource/DocumentRemoteDataSource.kt` | Create | Remote data source interface |
| `composeApp/src/commonMain/.../features/documents/data/datasource/DocumentRemoteDataSourceImpl.kt` | Create | Ktor multipart + JSON calls |
| `composeApp/src/commonMain/.../features/documents/data/repository/DocumentRepositoryImpl.kt` | Create | Repository implementation |
| `composeApp/src/commonMain/.../features/documents/di/DocumentsDataModule.kt` | Create | Data layer Koin module |
| `composeApp/src/commonMain/.../features/documents/di/DocumentsDomainModule.kt` | Create | Domain layer Koin module |
| `composeApp/src/commonMain/.../features/documents/di/DocumentsPresentationModule.kt` | Create | Presentation layer Koin module |
| `composeApp/src/androidMain/.../features/documents/di/DocumentsAndroidModule.kt` | Create | Android FilePicker Koin module |
| `composeApp/src/iosMain/.../features/documents/di/DocumentsIosModule.kt` | Create | iOS FilePicker Koin module |
| `composeApp/src/commonMain/.../features/documents/presentation/documents/DocumentsUiState.kt` | Create | MVI state |
| `composeApp/src/commonMain/.../features/documents/presentation/documents/DocumentsUiAction.kt` | Create | MVI actions |
| `composeApp/src/commonMain/.../features/documents/presentation/documents/DocumentsUiEffect.kt` | Create | MVI effects |
| `composeApp/src/commonMain/.../features/documents/presentation/documents/DocumentsViewModel.kt` | Create | MVI ViewModel |
| `composeApp/src/commonMain/.../features/documents/presentation/components/DocumentCard.kt` | Create | Document list item composable |
| `composeApp/src/commonMain/.../features/documents/presentation/components/UploadBottomSheet.kt` | Create | Upload sheet composable |
| `composeApp/src/commonMain/.../features/documents/presentation/documents/DocumentsScreen.kt` | Create | Main documents screen |
| `composeApp/src/commonMain/.../ui/navigation/Routes.kt` | Modify | Add `DocumentsRoute` + serializer |
| `composeApp/src/commonMain/.../ui/navigation/MainScaffold.kt` | Modify | Add Documents tab (position 2) |
| `composeApp/src/commonMain/.../ui/navigation/AppNavigator.kt` | Modify | Add `navigateToDocuments()` |
| `composeApp/src/commonMain/.../App.kt` | Modify | Add `NavEntry` for `DocumentsRoute` |
| `composeApp/src/commonMain/.../di/AppModule.kt` | Modify | Register 3 new Koin modules |
| `composeApp/src/androidMain/.../DockifyApplication.kt` | Modify | Register Android Koin module |
| `composeApp/src/iosMain/.../di/KoinHelper.kt` | Modify | Register iOS Koin module |
| `composeApp/src/androidMain/.../core/network/HttpClientFactory.kt` | Modify | Remove conflicting global Content-Type header |
| `composeApp/src/iosMain/.../core/network/HttpClientFactory.kt` | Modify | Same fix for iOS |
| `composeApp/src/commonMain/.../features/documents/feature.md` | Create | Feature documentation |

> **Path shorthand:** `...` = `kotlin/io/diasjakupov/dockify`

---

## Chunk 1: Domain Layer

### Task 1: Error Types

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/core/domain/DataError.kt`

- [ ] **Step 1: Read DataError.kt**

Understand existing error structure before adding to it.

- [ ] **Step 2: Add Document error enum**

Find the `DataError` sealed interface and add:

```kotlin
enum class Document : DataError {
    UPLOAD_FAILED,
    DELETE_FAILED,
    FILE_TOO_LARGE,
    UNSUPPORTED_FILE_TYPE,
    NOT_FOUND
}
```

- [ ] **Step 3: Add error messages**

Find the `toUserMessage()` extension function (likely in `ResourceExtensions.kt` or similar) and add cases for all 5 new errors. Example:

```kotlin
is DataError.Document.UPLOAD_FAILED -> "Failed to upload document. Please try again."
is DataError.Document.DELETE_FAILED -> "Failed to delete document."
is DataError.Document.FILE_TOO_LARGE -> "File is too large. Maximum size is 10 MB."
is DataError.Document.UNSUPPORTED_FILE_TYPE -> "Only PDF and image files are supported."
is DataError.Document.NOT_FOUND -> "Document not found."
```

- [ ] **Step 4: Build (commonMain)**

```bash
./gradlew :composeApp:compileKotlinAndroid
```

Expected: compiles.

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/core/
git commit -m "feat: add Document error types"
```

---

### Task 2: Domain Models

**Files:**
- Create: `.../features/documents/domain/model/FileType.kt`
- Create: `.../features/documents/domain/model/Document.kt`
- Create: `.../features/documents/domain/repository/DocumentRepository.kt`

- [ ] **Step 1: Write unit test for Document model (verify it's pure Kotlin)**

```kotlin
// composeApp/src/commonTest/kotlin/io/diasjakupov/dockify/features/documents/domain/DocumentModelTest.kt
package io.diasjakupov.dockify.features.documents.domain

import io.diasjakupov.dockify.features.documents.domain.model.Document
import io.diasjakupov.dockify.features.documents.domain.model.FileType
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class DocumentModelTest {
    @Test
    fun `Document holds all fields`() {
        val doc = Document(
            id = 1,
            title = "Blood Test",
            fileUrl = "http://example.com/file.pdf",
            fileType = FileType.PDF,
            fileSize = 1024L,
            createdAt = LocalDateTime(2026, 3, 10, 12, 0)
        )
        assertEquals(FileType.PDF, doc.fileType)
        assertEquals("Blood Test", doc.title)
    }

    @Test
    fun `FileType has PDF and IMAGE`() {
        assertEquals(2, FileType.entries.size)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :composeApp:testDebugUnitTest --tests "*DocumentModelTest*"
```

Expected: compile error — classes don't exist yet.

- [ ] **Step 3: Create FileType**

```kotlin
// features/documents/domain/model/FileType.kt
package io.diasjakupov.dockify.features.documents.domain.model

enum class FileType { PDF, IMAGE }
```

- [ ] **Step 4: Create Document**

```kotlin
// features/documents/domain/model/Document.kt
package io.diasjakupov.dockify.features.documents.domain.model

import kotlinx.datetime.LocalDateTime

data class Document(
    val id: Int,
    val title: String,
    val fileUrl: String,
    val fileType: FileType,
    val fileSize: Long,
    val createdAt: LocalDateTime
)
```

- [ ] **Step 5: Run test**

```bash
./gradlew :composeApp:testDebugUnitTest --tests "*DocumentModelTest*"
```

Expected: PASS.

- [ ] **Step 6: Create DocumentRepository interface**

```kotlin
// features/documents/domain/repository/DocumentRepository.kt
package io.diasjakupov.dockify.features.documents.domain.repository

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.documents.domain.model.Document
import io.diasjakupov.dockify.features.documents.platform.FilePickResult

interface DocumentRepository {
    suspend fun getDocuments(): Resource<List<Document>, DataError>
    suspend fun uploadDocument(title: String, file: FilePickResult): Resource<Document, DataError>
    suspend fun deleteDocument(id: Int): EmptyResult<DataError>
}
```

- [ ] **Step 7: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/documents/domain/
git commit -m "feat: add Document domain models and repository interface"
```

---

### Task 3: Use Cases

**Files:**
- Create: `.../features/documents/domain/usecase/GetDocumentsUseCase.kt`
- Create: `.../features/documents/domain/usecase/UploadDocumentUseCase.kt`
- Create: `.../features/documents/domain/usecase/DeleteDocumentUseCase.kt`

- [ ] **Step 1: Write use case tests**

```kotlin
// composeApp/src/commonTest/kotlin/io/diasjakupov/dockify/features/documents/domain/UseCaseTest.kt
package io.diasjakupov.dockify.features.documents.domain

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.documents.domain.model.Document
import io.diasjakupov.dockify.features.documents.domain.model.FileType
import io.diasjakupov.dockify.features.documents.domain.repository.DocumentRepository
import io.diasjakupov.dockify.features.documents.domain.usecase.UploadDocumentUseCase
import io.diasjakupov.dockify.features.documents.platform.FilePickResult
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertIs

class UploadDocumentUseCaseTest {

    private val fakeDoc = Document(1, "Test", "/url", FileType.PDF, 100L, LocalDateTime(2026, 3, 10, 12, 0))

    private val successRepo = object : DocumentRepository {
        override suspend fun getDocuments() = Resource.Success(listOf(fakeDoc))
        override suspend fun uploadDocument(title: String, file: FilePickResult) = Resource.Success(fakeDoc)
        override suspend fun deleteDocument(id: Int) = Resource.Success(Unit)
    }

    @Test
    fun `upload with blank title returns error`() = kotlinx.coroutines.test.runTest {
        val useCase = UploadDocumentUseCase(successRepo)
        val result = useCase("", FilePickResult(ByteArray(0), "test.pdf", "application/pdf"))
        assertIs<Resource.Error<*, *>>(result)
    }

    @Test
    fun `upload with valid title delegates to repository`() = kotlinx.coroutines.test.runTest {
        val useCase = UploadDocumentUseCase(successRepo)
        val result = useCase("Blood Test", FilePickResult(ByteArray(10), "test.pdf", "application/pdf"))
        assertIs<Resource.Success<*, *>>(result)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :composeApp:testDebugUnitTest --tests "*UseCaseTest*"
```

Expected: compile error.

- [ ] **Step 3: Create GetDocumentsUseCase**

```kotlin
// features/documents/domain/usecase/GetDocumentsUseCase.kt
package io.diasjakupov.dockify.features.documents.domain.usecase

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.documents.domain.model.Document
import io.diasjakupov.dockify.features.documents.domain.repository.DocumentRepository

class GetDocumentsUseCase(private val repository: DocumentRepository) {
    suspend operator fun invoke(): Resource<List<Document>, DataError> =
        repository.getDocuments()
}
```

- [ ] **Step 4: Create UploadDocumentUseCase**

```kotlin
// features/documents/domain/usecase/UploadDocumentUseCase.kt
package io.diasjakupov.dockify.features.documents.domain.usecase

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.documents.domain.model.Document
import io.diasjakupov.dockify.features.documents.domain.repository.DocumentRepository
import io.diasjakupov.dockify.features.documents.platform.FilePickResult

class UploadDocumentUseCase(private val repository: DocumentRepository) {
    suspend operator fun invoke(title: String, file: FilePickResult): Resource<Document, DataError> {
        if (title.isBlank()) return Resource.Error(DataError.Document.UPLOAD_FAILED)
        return repository.uploadDocument(title, file)
    }
}
```

- [ ] **Step 5: Create DeleteDocumentUseCase**

```kotlin
// features/documents/domain/usecase/DeleteDocumentUseCase.kt
package io.diasjakupov.dockify.features.documents.domain.usecase

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.features.documents.domain.repository.DocumentRepository

class DeleteDocumentUseCase(private val repository: DocumentRepository) {
    suspend operator fun invoke(id: Int): EmptyResult<DataError> =
        repository.deleteDocument(id)
}
```

- [ ] **Step 6: Run tests**

```bash
./gradlew :composeApp:testDebugUnitTest --tests "*UseCaseTest*"
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/documents/domain/usecase/
git add composeApp/src/commonTest/
git commit -m "feat: add document use cases with tests"
```

---

## Chunk 2: Platform + Data Layer

### Task 4: FilePicker expect/actual

**Files:**
- Create: `.../features/documents/platform/FilePicker.kt` (commonMain)
- Create: `androidMain/.../features/documents/platform/FilePicker.kt`
- Create: `iosMain/.../features/documents/platform/FilePicker.kt`

> **Read first:** Check `composeApp/src/androidMain/kotlin/io/diasjakupov/dockify/features/health/` to understand how the existing Android permission handler accesses `ComponentActivity` via Koin. Mirror that exact pattern.

- [ ] **Step 1: Create FilePickResult and expect class (commonMain)**

```kotlin
// commonMain features/documents/platform/FilePicker.kt
package io.diasjakupov.dockify.features.documents.platform

data class FilePickResult(
    val bytes: ByteArray,
    val fileName: String,
    val mimeType: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FilePickResult) return false
        return bytes.contentEquals(other.bytes) && fileName == other.fileName && mimeType == other.mimeType
    }
    override fun hashCode(): Int = 31 * bytes.contentHashCode() + fileName.hashCode() + mimeType.hashCode()
}

expect class FilePicker {
    suspend fun pickFile(): FilePickResult?
    suspend fun takePhoto(): FilePickResult?
}
```

- [ ] **Step 2: Create Android actual**

```kotlin
// androidMain features/documents/platform/FilePicker.kt
package io.diasjakupov.dockify.features.documents.platform

import android.content.Context
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class FilePicker(private val context: Context) {

    actual suspend fun pickFile(): FilePickResult? = suspendCancellableCoroutine { cont ->
        val activity = context as? ComponentActivity ?: run {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }
        val launcher = activity.activityResultRegistry.register(
            "pick_file_${System.currentTimeMillis()}",
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri == null) { cont.resume(null); return@register }
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes() ?: run {
                cont.resume(null); return@register
            }
            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            val fileName = uri.lastPathSegment ?: "document"
            cont.resume(FilePickResult(bytes, fileName, mimeType))
        }
        launcher.launch("*/*")
    }

    actual suspend fun takePhoto(): FilePickResult? = suspendCancellableCoroutine { cont ->
        val activity = context as? ComponentActivity ?: run {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }
        // Create a temp URI for the photo
        val photoUri = createTempImageUri(context)
        val launcher = activity.activityResultRegistry.register(
            "take_photo_${System.currentTimeMillis()}",
            ActivityResultContracts.TakePicture()
        ) { success: Boolean ->
            if (!success) { cont.resume(null); return@register }
            val bytes = context.contentResolver.openInputStream(photoUri)?.readBytes() ?: run {
                cont.resume(null); return@register
            }
            cont.resume(FilePickResult(bytes, "photo_${System.currentTimeMillis()}.jpg", "image/jpeg"))
        }
        launcher.launch(photoUri)
    }
}

private fun createTempImageUri(context: Context): android.net.Uri {
    val file = java.io.File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
    return androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}
```

- [ ] **Step 3: Create iOS actual**

```kotlin
// iosMain features/documents/platform/FilePicker.kt
package io.diasjakupov.dockify.features.documents.platform

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.darwin.NSObject
import kotlin.coroutines.resume

actual class FilePicker {
    actual suspend fun pickFile(): FilePickResult? = suspendCancellableCoroutine { cont ->
        val picker = UIDocumentPickerViewController(
            forOpeningContentTypes = listOf(
                platform.UniformTypeIdentifiers.UTTypePDF,
                platform.UniformTypeIdentifiers.UTTypeImage
            )
        )
        val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
                val url = didPickDocumentsAtURLs.firstOrNull() as? platform.Foundation.NSURL
                val data = url?.let { platform.Foundation.NSData.dataWithContentsOfURL(it) }
                val bytes = data?.let { ByteArray(it.length.toInt()).also { arr -> it.getBytes(arr.refTo(0), it.length) } }
                cont.resume(bytes?.let { FilePickResult(it, url?.lastPathComponent ?: "document", "application/octet-stream") })
            }
            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                cont.resume(null)
            }
        }
        picker.delegate = delegate
        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(picker, animated = true, completion = null)
    }

    actual suspend fun takePhoto(): FilePickResult? = suspendCancellableCoroutine { cont ->
        val config = PHPickerConfiguration()
        config.selectionLimit = 1
        val picker = PHPickerViewController(configuration = config)
        val delegate = object : NSObject(), PHPickerViewControllerDelegateProtocol {
            override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
                picker.dismissViewControllerAnimated(true, completion = null)
                val result = (didFinishPicking.firstOrNull() as? PHPickerResult) ?: run { cont.resume(null); return }
                result.itemProvider.loadDataRepresentationForTypeIdentifier("public.image") { data, _ ->
                    val bytes = data?.let { ByteArray(it.length.toInt()).also { arr -> it.getBytes(arr.refTo(0), it.length) } }
                    cont.resume(bytes?.let { FilePickResult(it, "photo_${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}.jpg", "image/jpeg") })
                }
            }
        }
        picker.delegate = delegate
        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(picker, animated = true, completion = null)
    }
}
```

- [ ] **Step 4: Check / create FileProvider config (Android)**

The `takePhoto()` path uses `FileProvider` to create a URI for the camera. Check if `res/xml/file_provider_paths.xml` already exists in the Android app. If not:

Create `composeApp/src/androidMain/res/xml/file_provider_paths.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <cache-path name="camera_photos" path="." />
</paths>
```

And register in `AndroidManifest.xml` inside `<application>`:
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.provider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_provider_paths" />
</provider>
```

- [ ] **Step 5: Build**

```bash
./gradlew :composeApp:compileKotlinAndroid
```

Expected: compiles. Fix any import errors.

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/documents/platform/
git add composeApp/src/androidMain/kotlin/io/diasjakupov/dockify/features/documents/platform/
git add composeApp/src/iosMain/kotlin/io/diasjakupov/dockify/features/documents/platform/
git add composeApp/src/androidMain/res/
git commit -m "feat: add FilePicker expect/actual for Android and iOS"
```

---

### Task 5: Fix HttpClientFactory Content-Type Conflict

**Files:**
- Modify: `composeApp/src/androidMain/.../core/network/HttpClientFactory.kt`
- Modify: `composeApp/src/iosMain/.../core/network/HttpClientFactory.kt`

- [ ] **Step 1: Read both HttpClientFactory files**

Find the `defaultRequest { contentType(ContentType.Application.Json) }` block (or any global Content-Type header setting).

- [ ] **Step 2: Remove the global Content-Type from defaultRequest**

The `ContentNegotiation` plugin with `json()` already sets `application/json` on serialized request bodies. The global `defaultRequest` content-type header overrides the boundary on multipart requests, breaking file uploads.

Remove the `contentType(ContentType.Application.Json)` call from `defaultRequest {}` in both `androidMain` and `iosMain` `HttpClientFactory.kt` files.

- [ ] **Step 3: Build**

```bash
./gradlew :composeApp:compileKotlinAndroid
```

Verify existing JSON calls still work (the `ContentNegotiation` plugin handles them automatically).

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/androidMain/kotlin/io/diasjakupov/dockify/core/
git add composeApp/src/iosMain/kotlin/io/diasjakupov/dockify/core/
git commit -m "fix: remove global Content-Type header to allow multipart uploads"
```

---

### Task 6: Data Layer — DTO, Mapper, DataSource, Repository

**Files:**
- Create: `.../features/documents/data/dto/DocumentDto.kt`
- Create: `.../features/documents/data/mapper/DocumentMapper.kt`
- Create: `.../features/documents/data/datasource/DocumentRemoteDataSource.kt`
- Create: `.../features/documents/data/datasource/DocumentRemoteDataSourceImpl.kt`
- Create: `.../features/documents/data/repository/DocumentRepositoryImpl.kt`

- [ ] **Step 1: Write mapper test**

```kotlin
// commonTest .../features/documents/data/DocumentMapperTest.kt
package io.diasjakupov.dockify.features.documents.data

import io.diasjakupov.dockify.features.documents.data.dto.DocumentDto
import io.diasjakupov.dockify.features.documents.data.mapper.DocumentMapper.toDomain
import io.diasjakupov.dockify.features.documents.domain.model.FileType
import kotlin.test.Test
import kotlin.test.assertEquals

class DocumentMapperTest {
    @Test
    fun `maps pdf file_type to FileType PDF`() {
        val dto = DocumentDto(1, "Test", "/url", "pdf", 100L, "2026-03-10T12:00:00Z")
        val domain = dto.toDomain()
        assertEquals(FileType.PDF, domain.fileType)
        assertEquals("Test", domain.title)
    }

    @Test
    fun `maps image file_type to FileType IMAGE`() {
        val dto = DocumentDto(1, "Xray", "/url", "image", 200L, "2026-03-10T12:00:00Z")
        val domain = dto.toDomain()
        assertEquals(FileType.IMAGE, domain.fileType)
    }

    @Test
    fun `unknown file_type defaults to IMAGE`() {
        val dto = DocumentDto(1, "Other", "/url", "unknown", 50L, "2026-03-10T12:00:00Z")
        val domain = dto.toDomain()
        assertEquals(FileType.IMAGE, domain.fileType)
    }
}
```

- [ ] **Step 2: Run to verify failure**

```bash
./gradlew :composeApp:testDebugUnitTest --tests "*DocumentMapperTest*"
```

Expected: compile error.

- [ ] **Step 3: Create DocumentDto**

```kotlin
// data/dto/DocumentDto.kt
package io.diasjakupov.dockify.features.documents.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DocumentDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("file_url") val fileUrl: String,
    @SerialName("file_type") val fileType: String,
    @SerialName("file_size") val fileSize: Long,
    @SerialName("created_at") val createdAt: String   // RFC 3339 e.g. "2026-03-10T12:00:00Z"
)
```

- [ ] **Step 4: Create DocumentMapper**

```kotlin
// data/mapper/DocumentMapper.kt
package io.diasjakupov.dockify.features.documents.data.mapper

import io.diasjakupov.dockify.features.documents.data.dto.DocumentDto
import io.diasjakupov.dockify.features.documents.domain.model.Document
import io.diasjakupov.dockify.features.documents.domain.model.FileType
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object DocumentMapper {
    fun DocumentDto.toDomain(): Document = Document(
        id = id,
        title = title,
        fileUrl = fileUrl,
        fileType = if (fileType.lowercase() == "pdf") FileType.PDF else FileType.IMAGE,
        fileSize = fileSize,
        createdAt = Instant.parse(createdAt).toLocalDateTime(TimeZone.currentSystemDefault())
    )

    fun List<DocumentDto>.toDomainList(): List<Document> = map { it.toDomain() }
}
```

- [ ] **Step 5: Run mapper tests**

```bash
./gradlew :composeApp:testDebugUnitTest --tests "*DocumentMapperTest*"
```

Expected: all 3 tests PASS.

- [ ] **Step 6: Verify safeApiCallEmpty exists**

Search for `safeApiCallEmpty` in `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/core/network/`:

```bash
grep -r "safeApiCallEmpty" composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/core/
```

If it doesn't exist, add it to the same file as `safeApiCall`. It should call the API and return `EmptyResult<DataError>` (i.e., `Resource<Unit, DataError>`). Look at how `safeApiCall` is implemented and create a variant that returns `Resource.Success(Unit)` on a 2xx response.

- [ ] **Step 7: Create DocumentRemoteDataSource interface**

```kotlin
// data/datasource/DocumentRemoteDataSource.kt
package io.diasjakupov.dockify.features.documents.data.datasource

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.documents.data.dto.DocumentDto

interface DocumentRemoteDataSource {
    suspend fun getDocuments(): Resource<List<DocumentDto>, DataError>
    suspend fun uploadDocument(title: String, fileBytes: ByteArray, fileName: String, mimeType: String): Resource<DocumentDto, DataError>
    suspend fun deleteDocument(id: Int): EmptyResult<DataError>
}
```

- [ ] **Step 7: Create DocumentRemoteDataSourceImpl**

> **Read first:** Read an existing `RemoteDataSourceImpl` in the health feature to understand how `safeApiCall` is used and how `httpClient` + `baseUrl` are injected.

```kotlin
// data/datasource/DocumentRemoteDataSourceImpl.kt
package io.diasjakupov.dockify.features.documents.data.datasource

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.core.network.safeApiCall
import io.diasjakupov.dockify.core.network.safeApiCallEmpty
import io.diasjakupov.dockify.features.documents.data.dto.DocumentDto
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.append
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData

class DocumentRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String
) : DocumentRemoteDataSource {

    override suspend fun getDocuments(): Resource<List<DocumentDto>, DataError> =
        safeApiCall {
            httpClient.get("$baseUrl/api/v1/documents")
        }

    override suspend fun uploadDocument(
        title: String,
        fileBytes: ByteArray,
        fileName: String,
        mimeType: String
    ): Resource<DocumentDto, DataError> =
        safeApiCall {
            httpClient.post("$baseUrl/api/v1/documents") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("title", title)
                            append("file", fileBytes, Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                                append(HttpHeaders.ContentType, mimeType)
                            })
                        }
                    )
                )
            }
        }

    override suspend fun deleteDocument(id: Int): EmptyResult<DataError> =
        safeApiCallEmpty {
            httpClient.delete("$baseUrl/api/v1/documents/$id")
        }
}
```

- [ ] **Step 8: Create DocumentRepositoryImpl**

```kotlin
// data/repository/DocumentRepositoryImpl.kt
package io.diasjakupov.dockify.features.documents.data.repository

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.documents.data.datasource.DocumentRemoteDataSource
import io.diasjakupov.dockify.features.documents.data.mapper.DocumentMapper.toDomain
import io.diasjakupov.dockify.features.documents.data.mapper.DocumentMapper.toDomainList
import io.diasjakupov.dockify.features.documents.domain.model.Document
import io.diasjakupov.dockify.features.documents.domain.repository.DocumentRepository
import io.diasjakupov.dockify.features.documents.platform.FilePickResult

class DocumentRepositoryImpl(
    private val remoteDataSource: DocumentRemoteDataSource
) : DocumentRepository {

    override suspend fun getDocuments(): Resource<List<Document>, DataError> =
        remoteDataSource.getDocuments().map { it.toDomainList() }

    override suspend fun uploadDocument(title: String, file: FilePickResult): Resource<Document, DataError> =
        remoteDataSource.uploadDocument(title, file.bytes, file.fileName, file.mimeType)
            .map { it.toDomain() }

    override suspend fun deleteDocument(id: Int): EmptyResult<DataError> =
        remoteDataSource.deleteDocument(id)
}
```

- [ ] **Step 9: Build**

```bash
./gradlew :composeApp:compileKotlinAndroid
```

- [ ] **Step 10: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/documents/data/
git add composeApp/src/commonTest/kotlin/io/diasjakupov/dockify/features/documents/data/
git commit -m "feat: add documents data layer (DTO, mapper, datasource, repository)"
```

---

## Chunk 3: DI + Presentation Layer

### Task 7: Koin DI Modules

**Files:**
- Create: `.../features/documents/di/DocumentsDataModule.kt`
- Create: `.../features/documents/di/DocumentsDomainModule.kt`
- Create: `.../features/documents/di/DocumentsPresentationModule.kt`
- Create: `androidMain/.../features/documents/di/DocumentsAndroidModule.kt`
- Create: `iosMain/.../features/documents/di/DocumentsIosModule.kt`
- Modify: `.../di/AppModule.kt`
- Modify: `androidMain/.../DockifyApplication.kt`
- Modify: `iosMain/.../di/KoinHelper.kt`

> **Read first:** Read `AppModule.kt`, `DockifyApplication.kt`, and `KoinHelper.kt` before modifying them. Understand how existing feature modules are registered.

- [ ] **Step 1: Create DocumentsDataModule**

```kotlin
// features/documents/di/DocumentsDataModule.kt
package io.diasjakupov.dockify.features.documents.di

import io.diasjakupov.dockify.features.documents.data.datasource.DocumentRemoteDataSource
import io.diasjakupov.dockify.features.documents.data.datasource.DocumentRemoteDataSourceImpl
import io.diasjakupov.dockify.features.documents.data.repository.DocumentRepositoryImpl
import io.diasjakupov.dockify.features.documents.domain.repository.DocumentRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

val documentsDataModule = module {
    single<DocumentRemoteDataSource> {
        DocumentRemoteDataSourceImpl(
            httpClient = get(),
            baseUrl = get(named("baseUrl"))
        )
    }
    single<DocumentRepository> {
        DocumentRepositoryImpl(remoteDataSource = get())
    }
}
```

> **Note:** Check `AppModule.kt` or `coreModule` to confirm the exact qualifier name for `baseUrl`. It may be `named("baseUrl")` or injected differently. Match whatever pattern the health remote data source uses.

- [ ] **Step 2: Create DocumentsDomainModule**

```kotlin
// features/documents/di/DocumentsDomainModule.kt
package io.diasjakupov.dockify.features.documents.di

import io.diasjakupov.dockify.features.documents.domain.usecase.DeleteDocumentUseCase
import io.diasjakupov.dockify.features.documents.domain.usecase.GetDocumentsUseCase
import io.diasjakupov.dockify.features.documents.domain.usecase.UploadDocumentUseCase
import org.koin.dsl.module

val documentsDomainModule = module {
    factory { GetDocumentsUseCase(repository = get()) }
    factory { UploadDocumentUseCase(repository = get()) }
    factory { DeleteDocumentUseCase(repository = get()) }
}
```

- [ ] **Step 3: Create DocumentsPresentationModule**

```kotlin
// features/documents/di/DocumentsPresentationModule.kt
package io.diasjakupov.dockify.features.documents.di

import io.diasjakupov.dockify.features.documents.presentation.documents.DocumentsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val documentsPresentationModule = module {
    viewModelOf(::DocumentsViewModel)
}
```

- [ ] **Step 4: Create DocumentsAndroidModule (androidMain)**

```kotlin
// androidMain features/documents/di/DocumentsAndroidModule.kt
package io.diasjakupov.dockify.features.documents.di

import io.diasjakupov.dockify.features.documents.platform.FilePicker
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val documentsAndroidModule = module {
    single { FilePicker(context = androidContext()) }
}
```

- [ ] **Step 5: Create DocumentsIosModule (iosMain)**

```kotlin
// iosMain features/documents/di/DocumentsIosModule.kt
package io.diasjakupov.dockify.features.documents.di

import io.diasjakupov.dockify.features.documents.platform.FilePicker
import org.koin.dsl.module

val documentsIosModule = module {
    single { FilePicker() }
}
```

- [ ] **Step 6: Register in AppModule.kt**

Add to the `appModules()` list:

```kotlin
import io.diasjakupov.dockify.features.documents.di.documentsDataModule
import io.diasjakupov.dockify.features.documents.di.documentsDomainModule
import io.diasjakupov.dockify.features.documents.di.documentsPresentationModule

// In appModules() list:
documentsDataModule,
documentsDomainModule,
documentsPresentationModule,
```

- [ ] **Step 7: Register Android module in DockifyApplication.kt**

Add `documentsAndroidModule` to the modules list — follow the exact same pattern used by `androidHealthModule` or `androidLocationModule`.

- [ ] **Step 8: Register iOS module in KoinHelper.kt**

Add `documentsIosModule` to the modules list — mirror how other iOS-specific modules are registered.

- [ ] **Step 9: Build**

```bash
./gradlew :composeApp:compileKotlinAndroid
```

Expected: compiles. Fix any unresolved references.

- [ ] **Step 10: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/documents/di/
git add composeApp/src/androidMain/kotlin/io/diasjakupov/dockify/features/documents/di/
git add composeApp/src/iosMain/kotlin/io/diasjakupov/dockify/features/documents/di/
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/di/
git add composeApp/src/androidMain/kotlin/io/diasjakupov/dockify/
git add composeApp/src/iosMain/kotlin/io/diasjakupov/dockify/
git commit -m "feat: register documents Koin modules"
```

---

### Task 8: MVI ViewModel

**Files:**
- Create: `.../features/documents/presentation/documents/DocumentsUiState.kt`
- Create: `.../features/documents/presentation/documents/DocumentsUiAction.kt`
- Create: `.../features/documents/presentation/documents/DocumentsUiEffect.kt`
- Create: `.../features/documents/presentation/documents/DocumentsViewModel.kt`

> **Read first:** Read an existing ViewModel (e.g., `HealthViewModel.kt`) and its UiState/UiAction/UiEffect files to understand `BaseViewModel`, `updateState`, `emitEffect`, `launch`, and `collectFlow` usage patterns before writing DocumentsViewModel.

- [ ] **Step 1: Write ViewModel test**

```kotlin
// commonTest .../features/documents/presentation/DocumentsViewModelTest.kt
package io.diasjakupov.dockify.features.documents.presentation

import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.documents.domain.model.Document
import io.diasjakupov.dockify.features.documents.domain.model.FileType
import io.diasjakupov.dockify.features.documents.domain.repository.DocumentRepository
import io.diasjakupov.dockify.features.documents.domain.usecase.DeleteDocumentUseCase
import io.diasjakupov.dockify.features.documents.domain.usecase.GetDocumentsUseCase
import io.diasjakupov.dockify.features.documents.domain.usecase.UploadDocumentUseCase
import io.diasjakupov.dockify.features.documents.platform.FilePickResult
import io.diasjakupov.dockify.features.documents.presentation.documents.DocumentsUiAction
import io.diasjakupov.dockify.features.documents.presentation.documents.DocumentsViewModel
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DocumentsViewModelTest {

    private val fakeDoc = Document(1, "Test", "/url", FileType.PDF, 100L, LocalDateTime(2026, 3, 10, 12, 0))

    private fun makeRepo(
        docs: List<Document> = listOf(fakeDoc),
        uploadResult: Resource<Document, *> = Resource.Success(fakeDoc)
    ) = object : DocumentRepository {
        override suspend fun getDocuments() = Resource.Success(docs)
        override suspend fun uploadDocument(title: String, file: FilePickResult) = uploadResult
        override suspend fun deleteDocument(id: Int) = Resource.Success(Unit)
    }

    @Test
    fun `initial state has empty documents`() {
        val vm = DocumentsViewModel(
            GetDocumentsUseCase(makeRepo()),
            UploadDocumentUseCase(makeRepo()),
            DeleteDocumentUseCase(makeRepo())
        )
        assertFalse(vm.state.value.isLoading)
        assertTrue(vm.state.value.documents.isEmpty())
    }

    @Test
    fun `ShowUploadSheet action sets showUploadSheet true`() {
        val vm = DocumentsViewModel(
            GetDocumentsUseCase(makeRepo()),
            UploadDocumentUseCase(makeRepo()),
            DeleteDocumentUseCase(makeRepo())
        )
        vm.onAction(DocumentsUiAction.ShowUploadSheet)
        assertTrue(vm.state.value.showUploadSheet)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :composeApp:testDebugUnitTest --tests "*DocumentsViewModelTest*"
```

Expected: compile error.

- [ ] **Step 3: Create MVI contracts**

```kotlin
// DocumentsUiState.kt
package io.diasjakupov.dockify.features.documents.presentation.documents

import io.diasjakupov.dockify.features.documents.domain.model.Document
import io.diasjakupov.dockify.ui.base.UiState

data class DocumentsUiState(
    val documents: List<Document> = emptyList(),
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val error: String? = null,
    val showUploadSheet: Boolean = false
) : UiState
```

```kotlin
// DocumentsUiAction.kt
package io.diasjakupov.dockify.features.documents.presentation.documents

import io.diasjakupov.dockify.features.documents.platform.FilePickResult
import io.diasjakupov.dockify.ui.base.UiAction

sealed interface DocumentsUiAction : UiAction {
    data object LoadDocuments : DocumentsUiAction
    data class UploadDocument(val title: String, val file: FilePickResult) : DocumentsUiAction
    data class DeleteDocument(val id: Int) : DocumentsUiAction
    data object ShowUploadSheet : DocumentsUiAction
    data object HideUploadSheet : DocumentsUiAction
}
```

```kotlin
// DocumentsUiEffect.kt
package io.diasjakupov.dockify.features.documents.presentation.documents

import io.diasjakupov.dockify.ui.base.UiEffect

sealed interface DocumentsUiEffect : UiEffect {
    data class ShowError(val message: String) : DocumentsUiEffect
    data object UploadSuccess : DocumentsUiEffect
}
```

- [ ] **Step 4: Create DocumentsViewModel**

```kotlin
// DocumentsViewModel.kt
package io.diasjakupov.dockify.features.documents.presentation.documents

import io.diasjakupov.dockify.core.domain.onError
import io.diasjakupov.dockify.core.domain.onSuccess
import io.diasjakupov.dockify.features.documents.domain.usecase.DeleteDocumentUseCase
import io.diasjakupov.dockify.features.documents.domain.usecase.GetDocumentsUseCase
import io.diasjakupov.dockify.features.documents.domain.usecase.UploadDocumentUseCase
import io.diasjakupov.dockify.ui.base.BaseViewModel

class DocumentsViewModel(
    private val getDocumentsUseCase: GetDocumentsUseCase,
    private val uploadDocumentUseCase: UploadDocumentUseCase,
    private val deleteDocumentUseCase: DeleteDocumentUseCase
) : BaseViewModel<DocumentsUiState, DocumentsUiAction, DocumentsUiEffect>(DocumentsUiState()) {

    init {
        onAction(DocumentsUiAction.LoadDocuments)
    }

    override fun handleAction(action: DocumentsUiAction) {
        when (action) {
            is DocumentsUiAction.LoadDocuments -> loadDocuments()
            is DocumentsUiAction.UploadDocument -> uploadDocument(action.title, action.file)
            is DocumentsUiAction.DeleteDocument -> deleteDocument(action.id)
            is DocumentsUiAction.ShowUploadSheet -> updateState { copy(showUploadSheet = true) }
            is DocumentsUiAction.HideUploadSheet -> updateState { copy(showUploadSheet = false) }
        }
    }

    private fun loadDocuments() {
        updateState { copy(isLoading = true, error = null) }
        launch {
            getDocumentsUseCase()
                .onSuccess { docs -> updateState { copy(documents = docs, isLoading = false) } }
                .onError { err ->
                    val msg = err.toUserMessage()
                    updateState { copy(isLoading = false, error = msg) }
                    emitEffect(DocumentsUiEffect.ShowError(msg))
                }
        }
    }

    private fun uploadDocument(title: String, file: io.diasjakupov.dockify.features.documents.platform.FilePickResult) {
        updateState { copy(isUploading = true, showUploadSheet = false) }
        launch {
            uploadDocumentUseCase(title, file)
                .onSuccess { newDoc ->
                    updateState { copy(isUploading = false, documents = documents + newDoc) }
                    emitEffect(DocumentsUiEffect.UploadSuccess)
                }
                .onError { err ->
                    val msg = err.toUserMessage()
                    updateState { copy(isUploading = false) }
                    emitEffect(DocumentsUiEffect.ShowError(msg))
                }
        }
    }

    private fun deleteDocument(id: Int) {
        launch {
            deleteDocumentUseCase(id)
                .onSuccess { updateState { copy(documents = documents.filterNot { it.id == id }) } }
                .onError { err -> emitEffect(DocumentsUiEffect.ShowError(err.toUserMessage())) }
        }
    }
}
```

> **Note:** `err.toUserMessage()` should work if `DataError` has a `toUserMessage()` extension function. Check where this is defined and confirm it handles `DataError.Document` variants added in Task 1.

- [ ] **Step 5: Run ViewModel tests**

```bash
./gradlew :composeApp:testDebugUnitTest --tests "*DocumentsViewModelTest*"
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/documents/presentation/documents/
git add composeApp/src/commonTest/kotlin/io/diasjakupov/dockify/features/documents/presentation/
git commit -m "feat: add DocumentsViewModel with MVI contracts and tests"
```

---

### Task 9: UI Components and Screen

**Files:**
- Create: `.../features/documents/presentation/components/DocumentCard.kt`
- Create: `.../features/documents/presentation/components/UploadBottomSheet.kt`
- Create: `.../features/documents/presentation/documents/DocumentsScreen.kt`

> **Read first:** Read `HealthScreen.kt` and a card component from the health feature to understand the Scaffold pattern, padding conventions, and how `DockifyScaffold` (or whatever the shared scaffold is called) is used.

- [ ] **Step 1: Create DocumentCard**

```kotlin
// presentation/components/DocumentCard.kt
package io.diasjakupov.dockify.features.documents.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.diasjakupov.dockify.features.documents.domain.model.Document
import io.diasjakupov.dockify.features.documents.domain.model.FileType

@Composable
fun DocumentCard(
    document: Document,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File type icon
            Text(
                text = if (document.fileType == FileType.PDF) "📄" else "🖼️",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(end = 12.dp)
            )
            // Document info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${document.fileType.name} · ${formatFileSize(document.fileSize)} · ${document.createdAt.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Delete button
            IconButton(onClick = onDelete) {
                Text("🗑️")
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1_000_000 -> "%.1f MB".format(bytes / 1_000_000.0)
        bytes >= 1_000 -> "%.0f KB".format(bytes / 1_000.0)
        else -> "$bytes B"
    }
}
```

- [ ] **Step 2: Create UploadBottomSheet**

```kotlin
// presentation/components/UploadBottomSheet.kt
package io.diasjakupov.dockify.features.documents.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.diasjakupov.dockify.features.documents.platform.FilePicker
import io.diasjakupov.dockify.features.documents.platform.FilePickResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadBottomSheet(
    filePicker: FilePicker,
    onUpload: (title: String, file: FilePickResult) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var pickedFile by remember { mutableStateOf<FilePickResult?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Upload Document", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                placeholder = { Text("e.g. Blood Test Jan 2026") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            pickedFile?.let {
                Text(
                    text = "Selected: ${it.fileName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { scope.launch { pickedFile = filePicker.takePhoto() } },
                    modifier = Modifier.weight(1f)
                ) { Text("📷 Camera") }
                OutlinedButton(
                    onClick = { scope.launch { pickedFile = filePicker.pickFile() } },
                    modifier = Modifier.weight(1f)
                ) { Text("📂 Files") }
            }

            Button(
                onClick = {
                    val file = pickedFile ?: return@Button
                    if (title.isNotBlank()) onUpload(title, file)
                },
                enabled = title.isNotBlank() && pickedFile != null,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Upload") }
        }
    }
}
```

- [ ] **Step 3: Create DocumentsScreen**

```kotlin
// presentation/documents/DocumentsScreen.kt
package io.diasjakupov.dockify.features.documents.presentation.documents

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.diasjakupov.dockify.features.documents.platform.FilePicker
import io.diasjakupov.dockify.features.documents.presentation.components.DocumentCard
import io.diasjakupov.dockify.features.documents.presentation.components.UploadBottomSheet
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DocumentsScreen(
    viewModel: DocumentsViewModel = koinViewModel(),
    filePicker: FilePicker = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DocumentsUiEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                is DocumentsUiEffect.UploadSuccess -> snackbarHostState.showSnackbar("Document uploaded successfully")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAction(DocumentsUiAction.ShowUploadSheet) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Upload document")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.documents.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📁", style = MaterialTheme.typography.displayMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No documents yet", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Upload your medical files to keep them organized",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.documents, key = { it.id }) { doc ->
                            DocumentCard(
                                document = doc,
                                onDelete = { viewModel.onAction(DocumentsUiAction.DeleteDocument(doc.id)) }
                            )
                        }
                    }
                }
            }

            if (state.isUploading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    if (state.showUploadSheet) {
        UploadBottomSheet(
            filePicker = filePicker,
            onUpload = { title, file -> viewModel.onAction(DocumentsUiAction.UploadDocument(title, file)) },
            onDismiss = { viewModel.onAction(DocumentsUiAction.HideUploadSheet) }
        )
    }
}
```

> **Note:** Replace `Scaffold` with the app's shared scaffold component (e.g., `DockifyScaffold`) if one exists. Read `HealthScreen.kt` to confirm. Use the same `TopBarConfig` pattern.

- [ ] **Step 4: Build**

```bash
./gradlew :composeApp:compileKotlinAndroid
```

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/documents/presentation/
git commit -m "feat: add DocumentsScreen, DocumentCard, and UploadBottomSheet"
```

---

## Chunk 4: Navigation Wiring

### Task 10: Add DocumentsRoute and Wire Navigation

**Files:**
- Modify: `.../ui/navigation/Routes.kt`
- Modify: `.../ui/navigation/MainScaffold.kt`
- Modify: `.../ui/navigation/AppNavigator.kt`
- Modify: `.../App.kt`

> **Read all four files before making any changes.** Navigation 3 serialization is strict — missing a route from `navSavedStateConfig` causes a runtime crash.

- [ ] **Step 1: Add DocumentsRoute to Routes.kt**

Add after the existing auth routes and before (or with) the main routes:

```kotlin
/** Tab 2: Medical documents */
@Serializable
data object DocumentsRoute : NavKey
```

Update the `navSavedStateConfig` polymorphic block (find the `SerializersModule` lambda):

```kotlin
subclass(DocumentsRoute::class, DocumentsRoute.serializer())
```

Update `TopLevelDestination` enum to insert `DOCUMENTS` at position 2:

```kotlin
enum class TopLevelDestination(val route: NavKey) {
    HEALTH(HealthRoute),
    DOCUMENTS(DocumentsRoute),
    NEARBY(NearbyRoute)
}
```

- [ ] **Step 2: Update MainScaffold.kt**

Find the `BottomNavItem` enum (or equivalent) and add the Documents entry between `HEALTH` and `NEARBY`:

```kotlin
DOCUMENTS(
    route = DocumentsRoute,
    label = "Documents",
    selectedIcon = Icons.Default.Description,   // or Icons.Filled.Article if Description unavailable
    unselectedIcon = Icons.Outlined.Description  // match whichever filled variant you use
)
```

> If `Icons.Outlined.Description` is not available, use `Icons.Default.Folder` / `Icons.Outlined.Folder` as a fallback. Check the existing icon imports in the file.

- [ ] **Step 3: Add navigateToDocuments() to AppNavigator**

```kotlin
fun navigateToDocuments() {
    navigateToRoot(DocumentsRoute)
}
```

- [ ] **Step 4: Add NavEntry for DocumentsRoute in App.kt**

Find the `entryProvider` `when` block and add (between the health entries and the nearby entry):

```kotlin
is DocumentsRoute -> NavEntry(key) {
    MainScaffoldScreen(currentRoute = key, navigator = navigator) {
        DocumentsScreen()
    }
}
```

- [ ] **Step 5: Build**

```bash
./gradlew :composeApp:compileKotlinAndroid
```

Expected: compiles. Fix any unresolved references.

- [ ] **Step 6: Run all unit tests**

```bash
./gradlew :composeApp:testDebugUnitTest
```

Expected: all tests pass.

- [ ] **Step 7: Build debug APK and install on device/emulator**

```bash
./gradlew :composeApp:installDebug
```

Manually verify:
- Documents tab appears as the 2nd tab
- Tapping "+ Upload" shows the bottom sheet
- Camera and Files buttons are visible in the sheet
- Empty state is shown when no documents exist

- [ ] **Step 8: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/ui/navigation/
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/App.kt
git commit -m "feat: wire DocumentsRoute into navigation — adds Documents as tab 2"
```

---

### Task 11: Feature Documentation

**Files:**
- Create: `.../features/documents/feature.md`
- Modify: `CLAUDE.md`

- [ ] **Step 1: Create feature.md**

```markdown
# Documents Feature

## Overview
Allows users to upload, view, and delete medical documents (PDFs and images) stored on the backend server.

## Routes
- `DocumentsRoute` — Tab 2 in the main scaffold (between Health and Nearby)

## Use Cases
- `GetDocumentsUseCase` — Fetches the list of the authenticated user's documents
- `UploadDocumentUseCase` — Validates title (non-blank) then delegates to repository; takes `FilePickResult`
- `DeleteDocumentUseCase` — Deletes a document by ID

## Data Sources
- Remote: `DocumentRemoteDataSourceImpl` — `GET /api/v1/documents`, `POST /api/v1/documents` (multipart), `DELETE /api/v1/documents/:id`
- Local: None (no caching in v1)

## DI Modules
- `documentsDataModule` (commonMain) — `DocumentRemoteDataSource`, `DocumentRepository`
- `documentsDomainModule` (commonMain) — Use cases (factory scope)
- `documentsPresentationModule` (commonMain) — `DocumentsViewModel`
- `documentsAndroidModule` (androidMain) — `FilePicker(context)`
- `documentsIosModule` (iosMain) — `FilePicker()`

## Key Behaviors / Gotchas
- `FilePicker` is platform-specific via expect/actual: Android uses Activity Result API, iOS uses PHPickerViewController / UIImagePickerController
- Multipart uploads require the global `Content-Type: application/json` header to be absent from `HttpClientFactory.defaultRequest` — it was removed as part of this feature
- `DocumentDto.createdAt` is RFC 3339 with `Z` suffix — use `Instant.parse()` not `LocalDateTime.parse()` in the mapper
- The backend returns a JWT token on login — this must be stored in DataStore and sent as `Authorization: Bearer <token>` on all document API calls
```

- [ ] **Step 2: Add feature.md reference to CLAUDE.md**

Find the Key Files table and add a row:

```
| `@composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/documents/feature.md` | Documents feature doc |
```

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/documents/feature.md
git add CLAUDE.md
git commit -m "docs: add documents feature.md and CLAUDE.md reference"
```

---

## Summary

After completing all chunks:

- ✅ `Document` domain model, `FileType` enum, `DocumentRepository` interface
- ✅ `GetDocumentsUseCase`, `UploadDocumentUseCase`, `DeleteDocumentUseCase` (with tests)
- ✅ `FilePicker` expect/actual — camera + file picker on Android and iOS
- ✅ `DocumentDto`, `DocumentMapper` (with tests), `DocumentRemoteDataSourceImpl` (Ktor multipart)
- ✅ `DocumentRepositoryImpl`
- ✅ Koin modules registered for all platforms
- ✅ `DocumentsViewModel` with MVI state/action/effect (with tests)
- ✅ `DocumentsScreen`, `DocumentCard`, `UploadBottomSheet`
- ✅ `DocumentsRoute` as tab 2: Health → **Documents** → Nearby
- ✅ Feature documentation updated
