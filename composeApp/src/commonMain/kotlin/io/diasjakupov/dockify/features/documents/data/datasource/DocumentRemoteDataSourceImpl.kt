package io.diasjakupov.dockify.features.documents.data.datasource

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.core.network.safeApiCall
import io.diasjakupov.dockify.features.documents.data.dto.DocumentResponseDto
import io.diasjakupov.dockify.features.documents.domain.model.PickedFile
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlin.coroutines.cancellation.CancellationException

class DocumentRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String
) : DocumentRemoteDataSource {

    override suspend fun getDocuments(userId: Int): Resource<List<DocumentResponseDto>, DataError> {
        return safeApiCall {
            httpClient.get("$baseUrl/api/v1/documents") {
                parameter("user_id", userId)
            }
        }
    }

    override suspend fun uploadDocument(
        userId: Int,
        file: PickedFile
    ): Resource<DocumentResponseDto, DataError> {
        return safeApiCall {
            httpClient.post("$baseUrl/api/v1/documents/upload") {
                setBody(MultiPartFormDataContent(formData {
                    append("user_id", userId.toString())
                    append("file", file.bytes, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"${file.fileName}\"")
                        append(HttpHeaders.ContentType, file.contentType)
                    })
                }))
            }
        }
    }

    // Does NOT use safeApiCallEmpty — 404 means the file was auto-purged, treat as success.
    override suspend fun deleteDocument(id: String): EmptyResult<DataError> {
        return try {
            val response: HttpResponse = httpClient.delete("$baseUrl/api/v1/documents/$id")
            when (response.status) {
                HttpStatusCode.OK,
                HttpStatusCode.NoContent,
                HttpStatusCode.NotFound -> Resource.Success(Unit)
                HttpStatusCode.Unauthorized -> Resource.Error(DataError.Auth.UNAUTHORIZED)
                else -> Resource.Error(DataError.Network.SERVER_ERROR)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: ClientRequestException) {
            when (e.response.status) {
                HttpStatusCode.NotFound -> Resource.Success(Unit)
                HttpStatusCode.Unauthorized -> Resource.Error(DataError.Auth.UNAUTHORIZED)
                else -> Resource.Error(DataError.Network.UNKNOWN)
            }
        } catch (e: Exception) {
            Resource.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun downloadDocument(id: String): Resource<ByteArray, DataError> {
        return safeApiCall {
            httpClient.get("$baseUrl/api/v1/documents/$id/download")
        }
    }
}
