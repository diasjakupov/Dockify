package io.diasjakupov.dockify.features.chat.data.datasource

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.core.network.safeApiCall
import io.diasjakupov.dockify.core.network.safeApiCallEmpty
import io.diasjakupov.dockify.core.network.defaultJson
import io.diasjakupov.dockify.features.chat.data.dto.ChatMessageResponseDto
import io.diasjakupov.dockify.features.chat.data.dto.ChatRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ChatRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val streamingHttpClient: HttpClient,
    private val baseUrl: String
) : ChatRemoteDataSource {

    override fun sendMessageStream(
        userId: Int,
        docId: String?,
        message: String
    ): Flow<String> = flow {
        val body = defaultJson.encodeToString(
            ChatRequestDto.serializer(),
            ChatRequestDto(userId = userId, docId = docId, message = message)
        )
        streamingHttpClient.preparePost("$baseUrl/api/v1/chat/stream") {
            setBody(TextContent(body, ContentType.Application.Json))
        }.execute { response ->
            val channel = response.bodyAsChannel()
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: break
                if (line.isBlank()) continue
                // Gin SSE format: "event:message\ndata:<token>\n\n"
                // No extra space after "data:" — any space IS part of the token content
                if (!line.startsWith("data:")) continue
                val data = line.removePrefix("data:")
                if (data == "[DONE]") break
                if (data.isNotEmpty()) {
                    emit(data)
                }
            }
        }
    }

    override suspend fun getHistory(
        userId: Int,
        docId: String?
    ): Resource<List<ChatMessageResponseDto>, DataError> {
        return safeApiCall {
            httpClient.get("$baseUrl/api/v1/chat") {
                parameter("user_id", userId)
                if (docId != null) parameter("doc_id", docId)
            }
        }
    }

    override suspend fun clearHistory(
        userId: Int,
        docId: String?
    ): EmptyResult<DataError> {
        return safeApiCallEmpty {
            httpClient.delete("$baseUrl/api/v1/chat") {
                parameter("user_id", userId)
                if (docId != null) parameter("doc_id", docId)
            }
        }
    }
}
