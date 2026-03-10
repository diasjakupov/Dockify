# Medical Documents — Backend Design Spec

**Date:** 2026-03-10
**Language:** Go + Gin
**Phase:** 1 — Upload & Storage

---

## Overview

Add document upload and management endpoints to the Dockify backend. Files are stored via a `FileStorage` abstraction that supports local disk (dev) and S3 (prod). All document endpoints are protected by JWT auth middleware.

---

## Decisions

| Decision | Choice |
|----------|--------|
| Storage abstraction | `FileStorage` interface — `LocalFileStorage` (default) + `S3FileStorage` |
| Auth | JWT middleware added to document routes (prerequisite) |
| File size limit | 10 MB per upload |
| `storage_type` config | `"local"` or `"s3"`, selected at startup |

---

## New Files

```
dockify-backend/
  db/migrations/
    0003_add_documents_table.up.sql
  internal/
    storage/
      storage.go          # FileStorage interface
      local.go            # LocalFileStorage implementation
      s3.go               # S3FileStorage implementation
      factory.go          # New(cfg StorageConfig) FileStorage
    middleware/
      auth.go             # JWTAuth gin.HandlerFunc
    repository/documents/
      documents.go        # Documents interface + pgx implementation
    services/documents/
      documents.go        # Documents service interface + implementation
    handlers/documents/
      documents.go        # Documents handler interface + implementation
  config/
    config.go             # StorageConfig + JWTConfig added
    config.json           # New fields added
```

**Modified files:**
- `internal/models/model.go` — add `Document` struct
- `internal/entity/entity.go` — add `DocumentResponse`, `LoginResponse`
- `internal/repository/repository.go` — embed `documents.Documents`
- `internal/services/service.go` — embed `documents.Documents`, accept `FileStorage` + `JWTConfig`
- `internal/services/user/user.go` — `Login` returns `(string, models.User, error)` (JWT token)
- `internal/handlers/user/user.go` — update Login handler to return `LoginResponse`
- `internal/handlers/handler.go` — embed `documents.Documents`
- `internal/router/router.go` — register protected route group + document routes
- `main.go` — wire `FileStorage`, pass `JWTConfig` to services and router

---

## Database Migration

**File:** `db/migrations/0003_add_documents_table.up.sql`

```sql
CREATE TABLE documents (
  id SERIAL PRIMARY KEY,
  user_id INT REFERENCES users(id) ON DELETE CASCADE,
  title VARCHAR(255) NOT NULL,
  file_url TEXT NOT NULL,
  file_type VARCHAR(20) NOT NULL,
  file_size BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT NOW()
);
```

Also create `0003_add_documents_table.down.sql`:

```sql
DROP TABLE IF EXISTS documents;
```

---

## Config

### `config/config.json` additions

```json
{
  "storage_type": "local",
  "storage_local_path": "./uploads",
  "storage_s3_bucket": "",
  "storage_s3_region": "",
  "storage_s3_access_key": "",
  "storage_s3_secret_key": "",
  "jwt_secret": "change-me-in-production",
  "jwt_expiry_hours": 72
}
```

### `config/config.go` additions

```go
type StorageConfig struct {
    StorageType        string `json:"storage_type"`
    StorageLocalPath   string `json:"storage_local_path"`
    StorageS3Bucket    string `json:"storage_s3_bucket"`
    StorageS3Region    string `json:"storage_s3_region"`
    StorageS3AccessKey string `json:"storage_s3_access_key"`
    StorageS3SecretKey string `json:"storage_s3_secret_key"`
}

type JWTConfig struct {
    JWTSecret      string `json:"jwt_secret"`
    JWTExpiryHours int    `json:"jwt_expiry_hours"`
}
```

Both embedded in `Config`.

---

## FileStorage Abstraction

```go
// internal/storage/storage.go
type FileStorage interface {
    Upload(ctx context.Context, file io.Reader, filename string, contentType string) (url string, err error)
    Delete(ctx context.Context, url string) error
}
```

**LocalFileStorage** (`local.go`):
- Writes to `{StorageLocalPath}/{timestamp}_{sanitized_filename}`
- Returns URL: `http://{host}:{port}/uploads/{filename}`
- `Delete` calls `os.Remove`
- Router serves `/uploads` as static: `r.Static("/uploads", "./uploads")`

**S3FileStorage** (`s3.go`):
- Uses `aws-sdk-go-v2` — `s3.PutObjectInput` / `DeleteObjectInput`
- Returns public S3 URL: `https://{bucket}.s3.{region}.amazonaws.com/{key}`
- Implement as stub returning `errors.New("s3 not configured")` for MVP; fill in when needed

**Factory** (`factory.go`):
```go
func New(cfg config.StorageConfig) FileStorage {
    switch cfg.StorageType {
    case "s3":
        return newS3FileStorage(cfg)
    default:
        return newLocalFileStorage(cfg)
    }
}
```

---

## JWT Middleware (Prerequisite)

### Dependency

```bash
go get github.com/golang-jwt/jwt/v5
```

### `internal/middleware/auth.go`

- Reads `Authorization: Bearer <token>` header
- Verifies HMAC signature with `cfg.JWTSecret`
- Extracts `user_id` from claims and sets it on Gin context via `c.Set(ContextUserIDKey, userID)` where `const ContextUserIDKey = "user_id"`
- Returns `401` if token is missing or invalid

### Login endpoint change

`services/user/user.go` — `Login` now signs a JWT and returns `(token string, user models.User, error)`:
- Token claims: `{"user_id": <id>, "exp": <now + JWTExpiryHours>}`
- Signed with `jwt.SigningMethodHS256`

`entity/entity.go` — add `LoginResponse`:
```go
type LoginResponse struct {
    Token string     `json:"token"`
    User  UserResponse `json:"user"`
}
```

`handlers/user/user.go` — Login handler responds with `LoginResponse`.

> **Breaking change:** The login response shape changes. Coordinate with the mobile client to update token storage and `Authorization` header sending.

### DocumentResponse

All document handler responses use this entity:

```go
type DocumentResponse struct {
    ID        int        `json:"id"`
    UserID    int        `json:"user_id"`
    Title     string     `json:"title"`
    FileURL   string     `json:"file_url"`
    FileType  string     `json:"file_type"`   // "pdf" | "image"
    FileSize  int64      `json:"file_size"`
    CreatedAt *time.Time `json:"created_at"`
}
```

---

## Endpoints

All under `/api/v1`, protected by `JWTAuth` middleware.

| Method | Route | Description |
|--------|-------|-------------|
| `POST` | `/documents` | Upload file — multipart/form-data: `title` + `file` |
| `GET` | `/documents` | List authenticated user's documents |
| `GET` | `/documents/:id` | Get single document (ownership check required) |
| `DELETE` | `/documents/:id` | Delete document + underlying file |

### Upload request

```
Content-Type: multipart/form-data

Fields:
  title  (string, required)
  file   (binary, required — PDF or image)
```

Max size: 10 MB. Enforce with `c.Request.ParseMultipartForm(10 << 20)`.

---

## Repository Layer

**File:** `internal/repository/documents/documents.go`

```go
type Documents interface {
    Create(ctx context.Context, doc models.Document) (int, error)
    GetByUserID(ctx context.Context, userID int) ([]models.Document, error)
    GetByID(ctx context.Context, id int) (models.Document, error)
    Delete(ctx context.Context, id int) (models.Document, error) // returns doc for file URL
}
```

- `Delete` returns the full `models.Document` so the service can call `storage.Delete(doc.FileURL)`
- `Create` uses `RETURNING id`
- `GetByUserID` orders by `created_at DESC`

---

## Service Layer

**File:** `internal/services/documents/documents.go`

```go
type Documents interface {
    Upload(ctx context.Context, userID int, title string, fh *multipart.FileHeader) (models.Document, error)
    ListByUser(ctx context.Context, userID int) ([]models.Document, error)
    GetByID(ctx context.Context, id int) (models.Document, error)
    Delete(ctx context.Context, id int) error
}
```

- `Upload`: open `fh`, call `storage.Upload`, then `repo.Create` with returned URL
- `Delete`: call `repo.Delete` (get file URL), then `storage.Delete(url)`
- Filename sanitization: `fmt.Sprintf("%d_%s", time.Now().UnixNano(), filepath.Base(fh.Filename))`

---

## Handler Layer

**File:** `internal/handlers/documents/documents.go`

```go
type Documents interface {
    Upload(c *gin.Context)
    List(c *gin.Context)
    GetByID(c *gin.Context)
    Delete(c *gin.Context)
}
```

- Extract `user_id` from Gin context: `c.GetInt(middleware.ContextUserIDKey)`
- `GetByID` / `Delete`: verify `doc.UserID == userID`, return `403` if mismatch
- All handlers return `DocumentResponse` (or slice) on success

---

## Router Changes

```go
// protected group for document routes
protected := api.Group("/")
protected.Use(middleware.JWTAuth(jwtCfg))
{
    docs := protected.Group("/documents")
    {
        docs.POST("", handler.Documents.Upload)
        docs.GET("", handler.Documents.List)
        docs.GET("/:id", handler.Documents.GetByID)
        docs.DELETE("/:id", handler.Documents.Delete)
    }
}

// serve local uploads as static
r.Static("/uploads", "./uploads")
```

Existing public routes (`/register`, `/login`, `/metrics`, `/recommendation`, etc.) are **not** changed to protected — backward compatibility preserved.

---

## `main.go` Wiring Changes

```go
store := storage.New(cfg.StorageConfig)
s := services.NewService(repo, gw, cfg.JWTConfig, store)
handler := handlers.NewHandler(logger, s)
r := router.NewRouter(handler, cfg.JWTConfig)
```

---

## Implementation Order

```
1. Migration (0003_add_documents_table.up.sql)   — independent
2. Config changes (StorageConfig + JWTConfig)    — independent
3. FileStorage interface + LocalFileStorage      — needs 2
4. JWT middleware + Login change                 — needs 2
5. models.Document                               — needs 1
6. entity.DocumentResponse + LoginResponse       — needs 5
7. Repository layer                              — needs 1, 5
8. Service layer                                 — needs 3, 7
9. Handler layer                                 — needs 4, 8
10. Router + main.go wiring                      — needs 4, 9
```

Steps 1 and 2 are independent and can run in parallel. Steps 3 and 4 can run in parallel after step 2. Steps 5 and 6 can run in parallel after step 1.

---

## Architectural Risks

| Risk | Severity | Mitigation |
|------|----------|------------|
| JWT is a cross-cutting change — Login response shape changes | High | Coordinate with mobile client; implement JWT before document routes |
| Existing routes remain unprotected (intentional) | Low | Acceptable for MVP; add auth to other routes in a separate pass |
| Filename collisions in LocalFileStorage | Medium | Prefix with `time.Now().UnixNano()` + `filepath.Base()` sanitization |
| Large file OOM | Medium | `ParseMultipartForm(10 << 20)` + 10 MB limit enforced at handler |
| S3 not implemented for MVP | Low | Stub returns error; `storage_type: "local"` is the default |
| `metric_value FLOAT` vs `string` mismatch in existing schema | None | Pre-existing issue, out of scope |

---

## Out of Scope (Future Phases)

- AI analysis / content extraction from documents
- S3FileStorage full implementation
- Document preview / thumbnail generation
- JWT refresh tokens
- Applying auth middleware to existing routes
