# Medical Documents — Backend Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add medical document upload/storage endpoints to the Dockify Go backend, protected by JWT auth, with a `FileStorage` abstraction supporting local disk and S3.

**Architecture:** Handlers → Services → Repository follows the existing three-layer pattern. A new `internal/storage` package provides the `FileStorage` interface with `LocalFileStorage` (default) and `S3FileStorage` implementations. JWT middleware is added as a prerequisite for all document routes.

**Tech Stack:** Go 1.24, Gin v1.11, pgx/v5, golang-jwt/jwt/v5, aws-sdk-go-v2 (S3 only)

**Spec:** `docs/superpowers/specs/backend/2026-03-10-medical-documents-design.md`

---

## File Map

| File | Action | Responsibility |
|------|--------|----------------|
| `db/migrations/0003_add_documents_table.up.sql` | Create | Documents table DDL |
| `db/migrations/0003_add_documents_table.down.sql` | Create | Rollback DDL |
| `config/config.go` | Modify | Add `StorageConfig`, `JWTConfig` structs |
| `config/config.json` | Modify | Add storage + JWT fields |
| `internal/storage/storage.go` | Create | `FileStorage` interface |
| `internal/storage/local.go` | Create | `LocalFileStorage` implementation |
| `internal/storage/s3.go` | Create | `S3FileStorage` stub |
| `internal/storage/factory.go` | Create | `New(cfg) FileStorage` factory |
| `internal/storage/storage_test.go` | Create | Unit tests for LocalFileStorage |
| `internal/middleware/auth.go` | Create | `JWTAuth` Gin middleware |
| `internal/middleware/auth_test.go` | Create | Middleware unit tests |
| `internal/models/model.go` | Modify | Add `Document` struct |
| `internal/entity/entity.go` | Modify | Add `DocumentResponse`, `LoginResponse` |
| `internal/repository/documents/documents.go` | Create | `Documents` interface + pgx implementation |
| `internal/repository/repository.go` | Modify | Embed `documents.Documents` |
| `internal/services/documents/documents.go` | Create | `Documents` service interface + implementation |
| `internal/services/documents/documents_test.go` | Create | Service unit tests |
| `internal/services/service.go` | Modify | Embed documents service, accept `FileStorage` + `JWTConfig` |
| `internal/services/user/user.go` | Modify | `Login` returns `(string, models.User, error)` |
| `internal/handlers/documents/documents.go` | Create | `Documents` handler interface + implementation |
| `internal/handlers/documents/documents_test.go` | Create | Handler integration tests |
| `internal/handlers/handler.go` | Modify | Embed documents handler |
| `internal/handlers/user/user.go` | Modify | Login handler returns `LoginResponse` |
| `internal/router/router.go` | Modify | Register protected document routes |
| `main.go` | Modify | Wire FileStorage + JWTConfig |

---

## Chunk 1: Foundation — Migration, Config, FileStorage

### Task 1: Database Migration

**Files:**
- Create: `db/migrations/0003_add_documents_table.up.sql`
- Create: `db/migrations/0003_add_documents_table.down.sql`

- [ ] **Step 1: Create up migration**

```sql
-- db/migrations/0003_add_documents_table.up.sql
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

- [ ] **Step 2: Create down migration**

```sql
-- db/migrations/0003_add_documents_table.down.sql
DROP TABLE IF EXISTS documents;
```

- [ ] **Step 3: Commit**

```bash
git add db/migrations/
git commit -m "feat: add documents table migration"
```

---

### Task 2: Config Additions

**Files:**
- Modify: `config/config.go`
- Modify: `config/config.json`

- [ ] **Step 1: Read current config structure**

Read `config/config.go` to understand the existing `Config` struct shape before modifying.

- [ ] **Step 2: Add StorageConfig and JWTConfig to config.go**

Add these two structs and embed them in `Config`:

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

Embed both in the existing `Config` struct alongside the existing `PostgresConfig` embed (follow the exact same embedding pattern).

- [ ] **Step 3: Add fields to config.json**

```json
"storage_type": "local",
"storage_local_path": "./uploads",
"storage_s3_bucket": "",
"storage_s3_region": "",
"storage_s3_access_key": "",
"storage_s3_secret_key": "",
"jwt_secret": "change-me-in-production",
"jwt_expiry_hours": 72
```

- [ ] **Step 4: Verify build compiles**

```bash
go build ./...
```

Expected: no errors.

- [ ] **Step 5: Commit**

```bash
git add config/
git commit -m "feat: add storage and JWT config fields"
```

---

### Task 3: FileStorage Interface and Local Implementation

**Files:**
- Create: `internal/storage/storage.go`
- Create: `internal/storage/local.go`
- Create: `internal/storage/s3.go`
- Create: `internal/storage/factory.go`
- Create: `internal/storage/storage_test.go`

- [ ] **Step 1: Write the failing test for LocalFileStorage**

```go
// internal/storage/storage_test.go
package storage_test

import (
	"bytes"
	"context"
	"os"
	"path/filepath"
	"strings"
	"testing"

	"github.com/askaroe/dockify-backend/config"
	"github.com/askaroe/dockify-backend/internal/storage"
)

func TestLocalFileStorage_Upload(t *testing.T) {
	dir := t.TempDir()
	cfg := config.StorageConfig{StorageType: "local", StorageLocalPath: dir}
	s := storage.New(cfg)

	url, err := s.Upload(context.Background(), bytes.NewBufferString("test content"), "test.pdf", "application/pdf")
	if err != nil {
		t.Fatalf("Upload failed: %v", err)
	}
	if !strings.HasSuffix(url, ".pdf") {
		t.Errorf("expected URL to end with .pdf, got %s", url)
	}
	// Verify file was actually written
	files, _ := filepath.Glob(filepath.Join(dir, "*.pdf"))
	if len(files) == 0 {
		t.Error("expected file to be written to disk")
	}
}

func TestLocalFileStorage_Delete(t *testing.T) {
	dir := t.TempDir()
	cfg := config.StorageConfig{StorageType: "local", StorageLocalPath: dir}
	s := storage.New(cfg)

	// Upload first
	url, _ := s.Upload(context.Background(), bytes.NewBufferString("hello"), "doc.pdf", "application/pdf")

	// Extract filename from URL and verify it exists
	filename := filepath.Base(url)
	fullPath := filepath.Join(dir, filename)
	if _, err := os.Stat(fullPath); os.IsNotExist(err) {
		t.Fatalf("file should exist before delete, path: %s", fullPath)
	}

	// Delete
	if err := s.Delete(context.Background(), url); err != nil {
		t.Fatalf("Delete failed: %v", err)
	}
	if _, err := os.Stat(fullPath); !os.IsNotExist(err) {
		t.Error("file should not exist after delete")
	}
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
go test ./internal/storage/... -v
```

Expected: compile error — `storage` package doesn't exist yet.

- [ ] **Step 3: Create storage interface**

```go
// internal/storage/storage.go
package storage

import (
	"context"
	"io"
)

// FileStorage abstracts file upload and delete operations.
// The backend decides whether to use local disk or cloud storage.
type FileStorage interface {
	Upload(ctx context.Context, file io.Reader, filename string, contentType string) (url string, err error)
	Delete(ctx context.Context, url string) error
}
```

- [ ] **Step 4: Create LocalFileStorage**

```go
// internal/storage/local.go
package storage

import (
	"context"
	"fmt"
	"io"
	"os"
	"path/filepath"
	"time"

	"github.com/askaroe/dockify-backend/config"
)

type localFileStorage struct {
	basePath string
}

func newLocalFileStorage(cfg config.StorageConfig) FileStorage {
	if err := os.MkdirAll(cfg.StorageLocalPath, 0755); err != nil {
		panic(fmt.Sprintf("storage: failed to create upload directory: %v", err))
	}
	return &localFileStorage{basePath: cfg.StorageLocalPath}
}

func (s *localFileStorage) Upload(_ context.Context, file io.Reader, filename string, _ string) (string, error) {
	// Sanitize filename to prevent path traversal
	safe := filepath.Base(filename)
	unique := fmt.Sprintf("%d_%s", time.Now().UnixNano(), safe)
	dest := filepath.Join(s.basePath, unique)

	f, err := os.Create(dest)
	if err != nil {
		return "", fmt.Errorf("storage: create file: %w", err)
	}
	defer f.Close()

	if _, err := io.Copy(f, file); err != nil {
		return "", fmt.Errorf("storage: write file: %w", err)
	}

	// Return the relative path as URL — router serves /uploads as static
	return "/uploads/" + unique, nil
}

func (s *localFileStorage) Delete(_ context.Context, url string) error {
	// url is like /uploads/12345_filename.pdf
	filename := filepath.Base(url)
	path := filepath.Join(s.basePath, filename)
	if err := os.Remove(path); err != nil && !os.IsNotExist(err) {
		return fmt.Errorf("storage: delete file: %w", err)
	}
	return nil
}
```

- [ ] **Step 5: Create S3 stub**

```go
// internal/storage/s3.go
package storage

import (
	"context"
	"errors"
	"io"

	"github.com/askaroe/dockify-backend/config"
)

type s3FileStorage struct{}

func newS3FileStorage(_ config.StorageConfig) FileStorage {
	return &s3FileStorage{}
}

func (s *s3FileStorage) Upload(_ context.Context, _ io.Reader, _ string, _ string) (string, error) {
	return "", errors.New("storage: S3 not yet configured — set storage_type to 'local'")
}

func (s *s3FileStorage) Delete(_ context.Context, _ string) error {
	return errors.New("storage: S3 not yet configured — set storage_type to 'local'")
}
```

- [ ] **Step 6: Create factory**

```go
// internal/storage/factory.go
package storage

import "github.com/askaroe/dockify-backend/config"

// New returns the FileStorage implementation selected by cfg.StorageType.
// Defaults to LocalFileStorage for any unrecognized type.
func New(cfg config.StorageConfig) FileStorage {
	switch cfg.StorageType {
	case "s3":
		return newS3FileStorage(cfg)
	default:
		return newLocalFileStorage(cfg)
	}
}
```

- [ ] **Step 7: Run tests**

```bash
go test ./internal/storage/... -v
```

Expected: both tests PASS.

- [ ] **Step 8: Commit**

```bash
git add internal/storage/
git commit -m "feat: add FileStorage interface with LocalFileStorage and S3 stub"
```

---

## Chunk 2: JWT Auth Middleware + Login Update

### Task 4: Add JWT Dependency

**Files:**
- Modify: `go.mod`, `go.sum`

- [ ] **Step 1: Install JWT library**

> All Go commands in this plan are run from the `dockify-backend/` directory (the Go module root).

```bash
go get github.com/golang-jwt/jwt/v5
```

Expected: `go.mod` updated with `github.com/golang-jwt/jwt/v5`.

- [ ] **Step 2: Commit**

```bash
git add go.mod go.sum
git commit -m "chore: add golang-jwt/jwt/v5 dependency"
```

---

### Task 5: JWT Middleware

**Files:**
- Create: `internal/middleware/auth.go`
- Create: `internal/middleware/auth_test.go`

- [ ] **Step 1: Write failing test**

```go
// internal/middleware/auth_test.go
package middleware_test

import (
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/askaroe/dockify-backend/config"
	"github.com/askaroe/dockify-backend/internal/middleware"
	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
)

func makeToken(secret string, userID int, expiry time.Duration) string {
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, jwt.MapClaims{
		"user_id": float64(userID),
		"exp":     time.Now().Add(expiry).Unix(),
	})
	s, _ := token.SignedString([]byte(secret))
	return s
}

func setupRouter(cfg config.JWTConfig) *gin.Engine {
	gin.SetMode(gin.TestMode)
	r := gin.New()
	r.Use(middleware.JWTAuth(cfg))
	r.GET("/test", func(c *gin.Context) {
		uid := c.GetInt(middleware.ContextUserIDKey)
		c.JSON(http.StatusOK, gin.H{"user_id": uid})
	})
	return r
}

func TestJWTAuth_ValidToken(t *testing.T) {
	cfg := config.JWTConfig{JWTSecret: "testsecret", JWTExpiryHours: 1}
	r := setupRouter(cfg)

	token := makeToken("testsecret", 42, time.Hour)
	req := httptest.NewRequest(http.MethodGet, "/test", nil)
	req.Header.Set("Authorization", "Bearer "+token)
	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Errorf("expected 200, got %d: %s", w.Code, w.Body.String())
	}
}

func TestJWTAuth_MissingToken(t *testing.T) {
	cfg := config.JWTConfig{JWTSecret: "testsecret", JWTExpiryHours: 1}
	r := setupRouter(cfg)

	req := httptest.NewRequest(http.MethodGet, "/test", nil)
	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)

	if w.Code != http.StatusUnauthorized {
		t.Errorf("expected 401, got %d", w.Code)
	}
}

func TestJWTAuth_InvalidToken(t *testing.T) {
	cfg := config.JWTConfig{JWTSecret: "testsecret", JWTExpiryHours: 1}
	r := setupRouter(cfg)

	req := httptest.NewRequest(http.MethodGet, "/test", nil)
	req.Header.Set("Authorization", "Bearer invalid.token.here")
	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)

	if w.Code != http.StatusUnauthorized {
		t.Errorf("expected 401, got %d", w.Code)
	}
}

func TestJWTAuth_ExpiredToken(t *testing.T) {
	cfg := config.JWTConfig{JWTSecret: "testsecret", JWTExpiryHours: 1}
	r := setupRouter(cfg)

	token := makeToken("testsecret", 1, -time.Hour) // already expired
	req := httptest.NewRequest(http.MethodGet, "/test", nil)
	req.Header.Set("Authorization", "Bearer "+token)
	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)

	if w.Code != http.StatusUnauthorized {
		t.Errorf("expected 401, got %d", w.Code)
	}
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
go test ./internal/middleware/... -v
```

Expected: compile error — `middleware` package doesn't exist.

- [ ] **Step 3: Implement middleware**

```go
// internal/middleware/auth.go
package middleware

import (
	"net/http"
	"strings"

	"github.com/askaroe/dockify-backend/config"
	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
)

// ContextUserIDKey is the Gin context key for the authenticated user's ID.
const ContextUserIDKey = "user_id"

// JWTAuth returns a Gin middleware that validates a Bearer token and
// injects the user_id claim into the Gin context.
func JWTAuth(cfg config.JWTConfig) gin.HandlerFunc {
	return func(c *gin.Context) {
		authHeader := c.GetHeader("Authorization")
		if !strings.HasPrefix(authHeader, "Bearer ") {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"message": "missing or invalid authorization header"})
			return
		}
		tokenStr := strings.TrimPrefix(authHeader, "Bearer ")
		token, err := jwt.Parse(tokenStr, func(t *jwt.Token) (interface{}, error) {
			if _, ok := t.Method.(*jwt.SigningMethodHMAC); !ok {
				return nil, jwt.ErrSignatureInvalid
			}
			return []byte(cfg.JWTSecret), nil
		})
		if err != nil || !token.Valid {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"message": "invalid or expired token"})
			return
		}
		claims, ok := token.Claims.(jwt.MapClaims)
		if !ok {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"message": "invalid token claims"})
			return
		}
		userID := int(claims["user_id"].(float64))
		c.Set(ContextUserIDKey, userID)
		c.Next()
	}
}
```

- [ ] **Step 4: Run tests**

```bash
go test ./internal/middleware/... -v
```

Expected: all 4 tests PASS.

- [ ] **Step 5: Commit**

```bash
git add internal/middleware/
git commit -m "feat: add JWT auth middleware"
```

---

### Task 6: Update Login to Issue JWT

**Files:**
- Modify: `internal/entity/entity.go`
- Modify: `internal/services/user/user.go`
- Modify: `internal/handlers/user/user.go`
- Modify: `internal/services/service.go`

- [ ] **Step 1: Read the files you're about to modify**

Read all four files listed above before touching anything.

- [ ] **Step 2: Add LoginResponse to entity.go**

Find the existing entity definitions and add:

Read `entity/entity.go` first. The spec defines `LoginResponse.User` as type `UserResponse`. If a `UserResponse` type already exists in the file use it; if the existing type is named differently (e.g., just `User`), use that name. Add:

```go
type LoginResponse struct {
    Token string       `json:"token"`
    User  UserResponse `json:"user"` // use whichever response entity type the existing Login returns
}
```

The key addition is the `Token string` field. Match the `User` field type to whatever the existing login response returns — do not introduce a new type.

- [ ] **Step 3: Update UserService.Login signature**

In `internal/services/user/user.go`, change the `User` interface:

```go
// Before (current):
Login(ctx context.Context, request entity.UserLoginRequest) (models.User, error)

// After:
Login(ctx context.Context, request entity.UserLoginRequest) (string, models.User, error)
```

Update the implementation to generate and return a JWT token:

```go
import (
    "time"
    "github.com/askaroe/dockify-backend/config"
    "github.com/golang-jwt/jwt/v5"
)

// Add cfg to the user service struct and NewUserService constructor:
type userService struct {
    repo *repository.Repository
    cfg  config.JWTConfig
}

func NewUserService(repo *repository.Repository, cfg config.JWTConfig) User {
    return &userService{repo: repo, cfg: cfg}
}

func (s *userService) Login(ctx context.Context, req entity.UserLoginRequest) (string, models.User, error) {
    user, err := s.repo.User.GetByEmail(ctx, req.Email) // or however existing logic retrieves user
    if err != nil {
        return "", models.User{}, err
    }
    // existing bcrypt check
    if err := bcrypt.CompareHashAndPassword([]byte(user.PasswordHash), []byte(req.Password)); err != nil {
        return "", models.User{}, errors.New("invalid credentials")
    }
    token := jwt.NewWithClaims(jwt.SigningMethodHS256, jwt.MapClaims{
        "user_id": user.ID,
        "exp":     time.Now().Add(time.Duration(s.cfg.JWTExpiryHours) * time.Hour).Unix(),
    })
    signed, err := token.SignedString([]byte(s.cfg.JWTSecret))
    if err != nil {
        return "", models.User{}, fmt.Errorf("failed to sign token: %w", err)
    }
    return signed, user, nil
}
```

> Note: read the existing Login implementation carefully and preserve its structure — only add token generation after the password check succeeds.

- [ ] **Step 4: Update service.go to pass JWTConfig to NewUserService**

In `internal/services/service.go`, update `NewService` signature and the call to `user.NewUserService`:

```go
func NewService(repo *repository.Repository, gw *gateway.Gateway, cfg config.JWTConfig) *Service {
    return &Service{
        Health:   health.NewHealthService(repo),
        User:     user.NewUserService(repo, cfg),
        Location: location.NewLocationService(repo),
        Gateway:  gw,
    }
}
```

- [ ] **Step 5: Update Login handler to return LoginResponse**

In `internal/handlers/user/user.go`, update the handler to destructure the new return value:

```go
func (h *userHandler) Login(c *gin.Context) {
    var req entity.UserLoginRequest
    if err := c.ShouldBindJSON(&req); err != nil {
        c.JSON(http.StatusBadRequest, gin.H{"message": err.Error()})
        return
    }
    token, user, err := h.s.User.Login(c.Request.Context(), req)
    if err != nil {
        c.JSON(http.StatusUnauthorized, gin.H{"message": err.Error()})
        return
    }
    c.JSON(http.StatusOK, entity.LoginResponse{
        Token: token,
        User:  entity.User{ /* map fields from models.User */ },
    })
}
```

Map `models.User` → `entity.User` (or whatever entity type matches the existing response). The key addition is wrapping it in `LoginResponse{Token: token, User: ...}`.

- [ ] **Step 6: Build to catch compile errors**

```bash
go build ./...
```

Fix any compile errors (callers of `NewService` in `main.go` now need to pass `cfg.JWTConfig`).

- [ ] **Step 7: Run all tests**

```bash
go test ./... -v
```

Expected: all passing.

- [ ] **Step 8: Commit**

```bash
git add internal/entity/ internal/services/ internal/handlers/user/
git commit -m "feat: login now issues JWT token"
```

---

## Chunk 3: Repository, Service, Handler

### Task 7: Domain Model and Entities

**Files:**
- Modify: `internal/models/model.go`
- Modify: `internal/entity/entity.go`

- [ ] **Step 1: Add Document to models/model.go**

Read the existing file, then append:

```go
type Document struct {
	ID        int        `json:"id"`
	UserID    int        `json:"user_id"`
	Title     string     `json:"title"`
	FileURL   string     `json:"file_url"`
	FileType  string     `json:"file_type"`
	FileSize  int64      `json:"file_size"`
	CreatedAt *time.Time `json:"created_at"`
}
```

(`time` should already be imported — verify before adding import.)

- [ ] **Step 2: Add DocumentResponse to entity/entity.go**

```go
type DocumentResponse struct {
	ID        int        `json:"id"`
	UserID    int        `json:"user_id"`
	Title     string     `json:"title"`
	FileURL   string     `json:"file_url"`
	FileType  string     `json:"file_type"`
	FileSize  int64      `json:"file_size"`
	CreatedAt *time.Time `json:"created_at"`
}
```

- [ ] **Step 3: Build**

```bash
go build ./...
```

- [ ] **Step 4: Commit**

```bash
git add internal/models/ internal/entity/
git commit -m "feat: add Document model and DocumentResponse entity"
```

---

### Task 8: Repository Layer

**Files:**
- Create: `internal/repository/documents/documents.go`
- Modify: `internal/repository/repository.go`

- [ ] **Step 1: Read repository.go and an existing repository implementation (e.g., health/health.go)**

Understand the exact pattern: interface definition, struct with `db *psql.Client`, `New...` constructor, SQL queries using `db.Pool.QueryRow` / `db.Pool.Query` / `db.Pool.Exec`.

- [ ] **Step 2: Create documents repository**

```go
// internal/repository/documents/documents.go
package documents

import (
	"context"
	"fmt"

	"github.com/askaroe/dockify-backend/internal/models"
	"github.com/askaroe/dockify-backend/pkg/psql"
)

type Documents interface {
	Create(ctx context.Context, doc models.Document) (int, error)
	GetByUserID(ctx context.Context, userID int) ([]models.Document, error)
	GetByID(ctx context.Context, id int) (models.Document, error)
	Delete(ctx context.Context, id int) (models.Document, error)
}

type documents struct {
	db *psql.Client
}

func NewDocumentsRepository(db *psql.Client) Documents {
	return &documents{db: db}
}

func (r *documents) Create(ctx context.Context, doc models.Document) (int, error) {
	var id int
	err := r.db.Pool.QueryRow(ctx,
		`INSERT INTO documents (user_id, title, file_url, file_type, file_size)
		 VALUES ($1, $2, $3, $4, $5) RETURNING id`,
		doc.UserID, doc.Title, doc.FileURL, doc.FileType, doc.FileSize,
	).Scan(&id)
	if err != nil {
		return 0, fmt.Errorf("documents.Create: %w", err)
	}
	return id, nil
}

func (r *documents) GetByUserID(ctx context.Context, userID int) ([]models.Document, error) {
	rows, err := r.db.Pool.Query(ctx,
		`SELECT id, user_id, title, file_url, file_type, file_size, created_at
		 FROM documents WHERE user_id = $1 ORDER BY created_at DESC`,
		userID,
	)
	if err != nil {
		return nil, fmt.Errorf("documents.GetByUserID: %w", err)
	}
	defer rows.Close()

	var docs []models.Document
	for rows.Next() {
		var d models.Document
		if err := rows.Scan(&d.ID, &d.UserID, &d.Title, &d.FileURL, &d.FileType, &d.FileSize, &d.CreatedAt); err != nil {
			return nil, fmt.Errorf("documents.GetByUserID scan: %w", err)
		}
		docs = append(docs, d)
	}
	return docs, rows.Err()
}

func (r *documents) GetByID(ctx context.Context, id int) (models.Document, error) {
	var d models.Document
	err := r.db.Pool.QueryRow(ctx,
		`SELECT id, user_id, title, file_url, file_type, file_size, created_at
		 FROM documents WHERE id = $1`,
		id,
	).Scan(&d.ID, &d.UserID, &d.Title, &d.FileURL, &d.FileType, &d.FileSize, &d.CreatedAt)
	if err != nil {
		return models.Document{}, fmt.Errorf("documents.GetByID: %w", err)
	}
	return d, nil
}

func (r *documents) Delete(ctx context.Context, id int) (models.Document, error) {
	var d models.Document
	err := r.db.Pool.QueryRow(ctx,
		`DELETE FROM documents WHERE id = $1
		 RETURNING id, user_id, title, file_url, file_type, file_size, created_at`,
		id,
	).Scan(&d.ID, &d.UserID, &d.Title, &d.FileURL, &d.FileType, &d.FileSize, &d.CreatedAt)
	if err != nil {
		return models.Document{}, fmt.Errorf("documents.Delete: %w", err)
	}
	return d, nil
}
```

- [ ] **Step 3: Embed Documents in repository.go**

Read `repository.go`, then add the import and embed following the exact same pattern as other features:

```go
import documentsRepo "github.com/askaroe/dockify-backend/internal/repository/documents"

// In Repository struct:
documentsRepo.Documents

// In NewRepository:
Documents: documentsRepo.NewDocumentsRepository(client),
```

- [ ] **Step 4: Build**

```bash
go build ./...
```

- [ ] **Step 5: Commit**

```bash
git add internal/repository/
git commit -m "feat: add documents repository layer"
```

---

### Task 9: Service Layer

**Files:**
- Create: `internal/services/documents/documents.go`
- Create: `internal/services/documents/documents_test.go`
- Modify: `internal/services/service.go`

- [ ] **Step 1: Write failing service tests**

```go
// internal/services/documents/documents_test.go
package documents_test

import (
	"bytes"
	"context"
	"io"
	"mime/multipart"
	"testing"
	"time"

	documentsrepo "github.com/askaroe/dockify-backend/internal/repository/documents"
	"github.com/askaroe/dockify-backend/internal/models"
	"github.com/askaroe/dockify-backend/internal/services/documents"
	"github.com/askaroe/dockify-backend/internal/storage"
)

// mockStorage implements storage.FileStorage for testing
type mockStorage struct {
	uploadedURL string
	uploadErr   error
	deleteErr   error
}

func (m *mockStorage) Upload(_ context.Context, _ io.Reader, filename, _ string) (string, error) {
	if m.uploadErr != nil {
		return "", m.uploadErr
	}
	return "/uploads/" + filename, nil
}

func (m *mockStorage) Delete(_ context.Context, _ string) error {
	return m.deleteErr
}

// mockRepo implements repository-level Documents interface for testing
type mockDocsRepo struct {
	created    models.Document
	createErr  error
	byUserDocs []models.Document
	byUserErr  error
	byIDDoc    models.Document
	byIDErr    error
	deleted    models.Document
	deleteErr  error
}

func (m *mockDocsRepo) Create(_ context.Context, doc models.Document) (int, error) {
	if m.createErr != nil { return 0, m.createErr }
	return 1, nil
}
func (m *mockDocsRepo) GetByUserID(_ context.Context, _ int) ([]models.Document, error) {
	return m.byUserDocs, m.byUserErr
}
func (m *mockDocsRepo) GetByID(_ context.Context, _ int) (models.Document, error) {
	return m.byIDDoc, m.byIDErr
}
func (m *mockDocsRepo) Delete(_ context.Context, _ int) (models.Document, error) {
	return m.deleted, m.deleteErr
}

// TestDocumentsService tests use a thin wrapper that accepts the Documents repo interface
// directly, avoiding the need for a real DB. See documents.go for the constructor.

func TestDocumentsService_ListByUser_Delegates(t *testing.T) {
	now := time.Now()
	repo := &mockDocsRepo{byUserDocs: []models.Document{{ID: 1, Title: "Test", CreatedAt: &now}}}
	store := &mockStorage{}
	svc := documents.NewDocumentsServiceWithRepo(repo, store)

	docs, err := svc.ListByUser(context.Background(), 1)
	if err != nil { t.Fatalf("unexpected error: %v", err) }
	if len(docs) != 1 { t.Errorf("expected 1 doc, got %d", len(docs)) }
}

func TestDocumentsService_Delete_CallsStorageDelete(t *testing.T) {
	now := time.Now()
	repo := &mockDocsRepo{deleted: models.Document{ID: 1, FileURL: "/uploads/test.pdf", CreatedAt: &now}}
	store := &mockStorage{}
	svc := documents.NewDocumentsServiceWithRepo(repo, store)

	if err := svc.Delete(context.Background(), 1); err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
}
```

> **Note on testability:** The service layer uses two constructors:
> - `NewDocumentsService(repo *repository.Repository, s storage.FileStorage)` — used in production wiring
> - `NewDocumentsServiceWithRepo(repo documentsrepo.Documents, s storage.FileStorage)` — used in tests, accepts the interface directly
>
> See Task 9 Step 2 for the full implementation of both constructors.

- [ ] **Step 2: Create documents service**

```go
// internal/services/documents/documents.go
package documents

import (
	"context"
	"fmt"
	"mime/multipart"
	"path/filepath"
	"strings"
	"time"

	"github.com/askaroe/dockify-backend/internal/models"
	"github.com/askaroe/dockify-backend/internal/repository"
	documentsrepo "github.com/askaroe/dockify-backend/internal/repository/documents"
	"github.com/askaroe/dockify-backend/internal/storage"
)

type Documents interface {
	Upload(ctx context.Context, userID int, title string, fh *multipart.FileHeader) (models.Document, error)
	ListByUser(ctx context.Context, userID int) ([]models.Document, error)
	GetByID(ctx context.Context, id int) (models.Document, error)
	Delete(ctx context.Context, id int) error
}

type documentsService struct {
	repo    documentsrepo.Documents  // interface — testable without real DB
	storage storage.FileStorage
}

// NewDocumentsService is used in production wiring (gets the repo from the aggregate).
func NewDocumentsService(repo *repository.Repository, s storage.FileStorage) Documents {
	return &documentsService{repo: repo.Documents, storage: s}
}

// NewDocumentsServiceWithRepo is used in unit tests — accepts the interface directly.
func NewDocumentsServiceWithRepo(repo documentsrepo.Documents, s storage.FileStorage) Documents {
	return &documentsService{repo: repo, storage: s}
}

func (s *documentsService) Upload(ctx context.Context, userID int, title string, fh *multipart.FileHeader) (models.Document, error) {
	file, err := fh.Open()
	if err != nil {
		return models.Document{}, fmt.Errorf("documents.Upload: open file: %w", err)
	}
	defer file.Close()

	contentType := fh.Header.Get("Content-Type")
	if contentType == "" {
		contentType = "application/octet-stream"
	}
	safeName := filepath.Base(fh.Filename)

	url, err := s.storage.Upload(ctx, file, safeName, contentType)
	if err != nil {
		return models.Document{}, fmt.Errorf("documents.Upload: storage: %w", err)
	}

	fileType := "image"
	if strings.HasSuffix(strings.ToLower(safeName), ".pdf") || contentType == "application/pdf" {
		fileType = "pdf"
	}

	now := time.Now()
	doc := models.Document{
		UserID:    userID,
		Title:     title,
		FileURL:   url,
		FileType:  fileType,
		FileSize:  fh.Size,
		CreatedAt: &now,
	}
	id, err := s.repo.Documents.Create(ctx, doc)
	if err != nil {
		// Best-effort cleanup if DB insert fails
		_ = s.storage.Delete(ctx, url)
		return models.Document{}, fmt.Errorf("documents.Upload: create record: %w", err)
	}
	doc.ID = id
	return doc, nil
}

func (s *documentsService) ListByUser(ctx context.Context, userID int) ([]models.Document, error) {
	return s.repo.Documents.GetByUserID(ctx, userID)
}

func (s *documentsService) GetByID(ctx context.Context, id int) (models.Document, error) {
	return s.repo.Documents.GetByID(ctx, id)
}

func (s *documentsService) Delete(ctx context.Context, id int) error {
	doc, err := s.repo.Documents.Delete(ctx, id)
	if err != nil {
		return fmt.Errorf("documents.Delete: %w", err)
	}
	return s.storage.Delete(ctx, doc.FileURL)
}
```

- [ ] **Step 3: Embed documents service in service.go**

Read `service.go`, then add:

```go
import (
    documentsService "github.com/askaroe/dockify-backend/internal/services/documents"
    "github.com/askaroe/dockify-backend/internal/storage"
)

// In Service struct — use a NAMED field (not anonymous embed) to avoid
// naming ambiguity with the repository-level Documents interface:
Documents documentsService.Documents

// Update NewService signature — add store parameter:
func NewService(repo *repository.Repository, gw *gateway.Gateway, cfg config.JWTConfig, store storage.FileStorage) *Service {
    return &Service{
        Health:    health.NewHealthService(repo),
        User:      user.NewUserService(repo, cfg),
        Location:  location.NewLocationService(repo),
        Documents: documentsService.NewDocumentsService(repo, store),
        Gateway:   gw,
    }
}
```

- [ ] **Step 4: Build**

```bash
go build ./...
```

Fix compile errors in `main.go` (pass `store` to `NewService`). You'll add `store := storage.New(cfg.StorageConfig)` in main.

- [ ] **Step 5: Commit**

```bash
git add internal/services/
git commit -m "feat: add documents service layer"
```

---

### Task 10: Handler Layer

**Files:**
- Create: `internal/handlers/documents/documents.go`
- Create: `internal/handlers/documents/documents_test.go`
- Modify: `internal/handlers/handler.go`

- [ ] **Step 1: Write handler tests**

```go
// internal/handlers/documents/documents_test.go
package documents_test

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/askaroe/dockify-backend/internal/entity"
	"github.com/askaroe/dockify-backend/internal/middleware"
	"github.com/gin-gonic/gin"
)

func setupTestRouter() *gin.Engine {
	gin.SetMode(gin.TestMode)
	r := gin.New()
	// inject a fake user_id as if middleware already ran
	r.Use(func(c *gin.Context) {
		c.Set(middleware.ContextUserIDKey, 1)
		c.Next()
	})
	return r
}

func TestDocumentsHandler_List_Empty(t *testing.T) {
	// Placeholder: requires wiring a mock service
	// Full test will return empty array and 200
	t.Skip("implement after handler is wired with mock service")
}

func TestDocumentsHandler_Upload_MissingTitle(t *testing.T) {
	t.Skip("implement after handler is wired with mock service")
}
```

> The handler tests require a mock `services.Service`. Expand these tests after the handler is implemented and wired.

- [ ] **Step 2: Implement handler**

```go
// internal/handlers/documents/documents.go
package documents

import (
	"net/http"
	"strconv"

	"github.com/askaroe/dockify-backend/internal/entity"
	"github.com/askaroe/dockify-backend/internal/middleware"
	"github.com/askaroe/dockify-backend/internal/services"
	"github.com/askaroe/dockify-backend/pkg/utils"
	"github.com/gin-gonic/gin"
)

type Documents interface {
	Upload(c *gin.Context)
	List(c *gin.Context)
	GetByID(c *gin.Context)
	Delete(c *gin.Context)
}

type documentsHandler struct {
	s      *services.Service
	logger *utils.Logger
}

func NewDocumentsHandler(s *services.Service, logger *utils.Logger) Documents {
	return &documentsHandler{s: s, logger: logger}
}

func (h *documentsHandler) Upload(c *gin.Context) {
	if err := c.Request.ParseMultipartForm(10 << 20); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"message": "file too large or invalid form"})
		return
	}
	title := c.PostForm("title")
	if title == "" {
		c.JSON(http.StatusBadRequest, gin.H{"message": "title is required"})
		return
	}
	fh, err := c.FormFile("file")
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"message": "file is required"})
		return
	}
	userID := c.GetInt(middleware.ContextUserIDKey)
	doc, err := h.s.Documents.Upload(c.Request.Context(), userID, title, fh)
	if err != nil {
		h.logger.Error("documents upload failed", err)
		c.JSON(http.StatusInternalServerError, gin.H{"message": err.Error()})
		return
	}
	c.JSON(http.StatusCreated, toResponse(doc))
}

func (h *documentsHandler) List(c *gin.Context) {
	userID := c.GetInt(middleware.ContextUserIDKey)
	docs, err := h.s.Documents.ListByUser(c.Request.Context(), userID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"message": err.Error()})
		return
	}
	resp := make([]entity.DocumentResponse, len(docs))
	for i, d := range docs {
		resp[i] = toResponse(d)
	}
	c.JSON(http.StatusOK, resp)
}

func (h *documentsHandler) GetByID(c *gin.Context) {
	id, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"message": "invalid document id"})
		return
	}
	userID := c.GetInt(middleware.ContextUserIDKey)
	doc, err := h.s.Documents.GetByID(c.Request.Context(), id)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"message": "document not found"})
		return
	}
	if doc.UserID != userID {
		c.JSON(http.StatusForbidden, gin.H{"message": "access denied"})
		return
	}
	c.JSON(http.StatusOK, toResponse(doc))
}

func (h *documentsHandler) Delete(c *gin.Context) {
	id, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"message": "invalid document id"})
		return
	}
	userID := c.GetInt(middleware.ContextUserIDKey)
	doc, err := h.s.Documents.GetByID(c.Request.Context(), id)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"message": "document not found"})
		return
	}
	if doc.UserID != userID {
		c.JSON(http.StatusForbidden, gin.H{"message": "access denied"})
		return
	}
	if err := h.s.Documents.Delete(c.Request.Context(), id); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"message": err.Error()})
		return
	}
	c.Status(http.StatusNoContent)
}

func toResponse(d models.Document) entity.DocumentResponse {
	return entity.DocumentResponse{
		ID:        d.ID,
		UserID:    d.UserID,
		Title:     d.Title,
		FileURL:   d.FileURL,
		FileType:  d.FileType,
		FileSize:  d.FileSize,
		CreatedAt: d.CreatedAt,
	}
}
```

- [ ] **Step 3: Embed handler in handler.go**

Read `handler.go`, then add following the existing pattern:

```go
import documentsHandler "github.com/askaroe/dockify-backend/internal/handlers/documents"

// In Handler struct:
documentsHandler.Documents

// In NewHandler:
Documents: documentsHandler.NewDocumentsHandler(s, logger),
```

- [ ] **Step 4: Build**

```bash
go build ./...
```

- [ ] **Step 5: Commit**

```bash
git add internal/handlers/
git commit -m "feat: add documents handler layer"
```

---

## Chunk 4: Router + main.go Wiring

### Task 11: Register Routes and Wire main.go

**Files:**
- Modify: `internal/router/router.go`
- Modify: `main.go`

- [ ] **Step 1: Read router.go and main.go**

Understand the existing router setup: how `handler` is used, how the router function signature looks, and how `main.go` wires everything together.

- [ ] **Step 2: Update router.go**

Add `jwtCfg config.JWTConfig` parameter to `NewRouter` (or however the router is constructed). Add the protected group and static file serving:

```go
import (
    "github.com/askaroe/dockify-backend/config"
    "github.com/askaroe/dockify-backend/internal/middleware"
)

// Add jwtCfg to the NewRouter signature — check existing signature first
func NewRouter(handler *handlers.Handler, jwtCfg config.JWTConfig) *gin.Engine {
    // ... existing routes unchanged ...

    // Protected routes (require valid JWT)
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

    // Serve uploaded files as static content
    r.Static("/uploads", "./uploads")

    return r
}
```

- [ ] **Step 3: Update main.go**

Add storage initialization and pass new args:

```go
// After existing service/repo setup:
store := storage.New(cfg.StorageConfig)
s := services.NewService(repo, gw, cfg.JWTConfig, store)
handler := handlers.NewHandler(logger, s)
r := router.NewRouter(handler, cfg.JWTConfig)
```

- [ ] **Step 4: Final build**

```bash
go build ./...
```

Expected: compiles with no errors.

- [ ] **Step 5: Run all tests**

```bash
go test ./... -v
```

Expected: all tests pass.

- [ ] **Step 6: Manual smoke test (optional)**

Start the server and test with curl:

```bash
go run main.go &

# Register a user
curl -s -X POST http://localhost:8080/api/v1/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@test.com","password":"pass","first_name":"T","last_name":"T"}'

# Login and capture token
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"pass"}' | jq -r '.token')

# Upload a file
curl -s -X POST http://localhost:8080/api/v1/documents \
  -H "Authorization: Bearer $TOKEN" \
  -F "title=My Test Doc" \
  -F "file=@/path/to/test.pdf"

# List documents
curl -s http://localhost:8080/api/v1/documents \
  -H "Authorization: Bearer $TOKEN"
```

- [ ] **Step 7: Final commit**

```bash
git add internal/router/ main.go
git commit -m "feat: wire documents routes and FileStorage into main"
```

---

## Summary

After completing all chunks, the backend will have:

- ✅ `documents` table in PostgreSQL
- ✅ `FileStorage` abstraction (local disk default, S3 stub)
- ✅ JWT auth middleware protecting document routes
- ✅ Login endpoint returns a signed JWT token
- ✅ `POST /api/v1/documents` — multipart upload
- ✅ `GET /api/v1/documents` — list user's documents
- ✅ `GET /api/v1/documents/:id` — get single document (ownership check)
- ✅ `DELETE /api/v1/documents/:id` — delete document + file
- ✅ Static file serving at `/uploads/`
