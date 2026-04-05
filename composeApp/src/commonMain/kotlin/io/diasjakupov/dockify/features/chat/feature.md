# Chat Feature

## Overview
AI-powered medical chat assistant using the DeepSeek backend. Supports general health questions and document-specific conversations with SSE streaming responses.

## Routes
- `ChatRoute(docId: String? = null, documentName: String? = null)` — General chat when docId is null, document-specific when provided

## Use Cases
- `SendMessageUseCase(docId, message)` — Sends message via SSE streaming, returns `Flow<String>` of response chunks
- `GetChatHistoryUseCase(docId)` — Fetches persisted chat history from backend
- `ClearChatHistoryUseCase(docId)` — Deletes chat history on backend

## Data Sources
- Remote: `ChatRemoteDataSourceImpl` — API endpoints:
  - `POST /api/v1/chat/stream` (SSE streaming)
  - `GET /api/v1/chat?user_id=&doc_id=` (history)
  - `DELETE /api/v1/chat?user_id=&doc_id=` (clear)

## DI Modules
- `chatDataModule` — data source + repository
- `chatDomainModule` — use cases
- `chatPresentationModule` — ChatViewModel (parameterized with docId, documentName)

## Key Behaviors
- SSE streaming parsed via Ktor `bodyAsChannel()` line-by-line
- Messages appended to state in real-time during streaming
- Send button disabled while streaming
- Partial responses preserved on network failure
- History fetched on screen entry, no local cache
- General chat (bottom nav tab) uses `docId = null`
- Document chat navigated from DocumentSummaryBottomSheet passes `docId` and `documentName`
