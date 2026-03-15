package io.diasjakupov.dockify.features.documents.domain.usecase

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.auth.domain.model.AuthCredentials
import io.diasjakupov.dockify.features.auth.domain.model.RegistrationData
import io.diasjakupov.dockify.features.auth.domain.model.User
import io.diasjakupov.dockify.features.auth.domain.repository.AuthRepository
import io.diasjakupov.dockify.features.auth.domain.usecase.GetCurrentUserUseCase
import io.diasjakupov.dockify.features.documents.domain.model.Document
import io.diasjakupov.dockify.features.documents.domain.model.PickedFile
import io.diasjakupov.dockify.features.documents.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertIs

class UploadDocumentUseCaseTest {

    private val fakeUser = User(
        id = "1",
        email = "test@test.com",
        username = "test",
        firstName = "Test",
        lastName = "User"
    )

    private val fakeAuthRepository = object : AuthRepository {
        override suspend fun login(credentials: AuthCredentials) = Resource.Success(fakeUser)
        override suspend fun register(data: RegistrationData) = Resource.Success("ok")
        override suspend fun logout(): EmptyResult<DataError> = Resource.Success(Unit)
        override suspend fun getCurrentUser() = Resource.Success(fakeUser)
        override fun observeAuthState(): Flow<Boolean> = flowOf(true)
        override suspend fun isAuthenticated() = true
    }

    private val fakeDocumentRepository = object : DocumentRepository {
        override suspend fun getDocuments(userId: Int) = Resource.Success(emptyList<Document>())
        override suspend fun uploadDocument(userId: Int, file: PickedFile) = Resource.Success(
            Document("id", userId, file.fileName, file.bytes.size.toLong(), file.contentType, "2026-03-13")
        )
        override suspend fun deleteDocument(id: String): EmptyResult<DataError> = Resource.Success(Unit)
        override suspend fun downloadDocument(id: String): Resource<ByteArray, DataError> = Resource.Success(ByteArray(0))
    }

    @Test
    fun `returns FILE_TOO_LARGE when file exceeds 10MB`() {
        runBlocking {
            val useCase = UploadDocumentUseCase(fakeDocumentRepository, GetCurrentUserUseCase(fakeAuthRepository))
            val oversized = PickedFile("big.pdf", "application/pdf", ByteArray(10 * 1024 * 1024 + 1))

            val result = useCase(oversized)

            assertIs<Resource.Error<DataError>>(result)
            assert(result.error == DataError.Document.FILE_TOO_LARGE)
        }
    }

    @Test
    fun `delegates to repository for valid file`() {
        runBlocking {
            val useCase = UploadDocumentUseCase(fakeDocumentRepository, GetCurrentUserUseCase(fakeAuthRepository))
            val file = PickedFile("doc.pdf", "application/pdf", ByteArray(1024))

            val result = useCase(file)

            assertIs<Resource.Success<Document>>(result)
        }
    }
}
