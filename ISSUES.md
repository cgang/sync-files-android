# Protocol Issues & Questions

This document tracks ambiguous, incomplete, or unclear aspects of the File Hub server protocol and API. These issues need clarification from server developers.

**Last Updated**: 2026-02-26  
**Status**: 🟡 Active (awaiting server team response)

---

## Table of Contents

- [Authentication API](#authentication-api)
- [File Operations API](#file-operations-api)
- [Upload Protocol](#upload-protocol)
- [Sync Protocol](#sync-protocol)
- [Error Handling](#error-handling)
- [DCIM Sync Specifics](#dcim-sync-specifics)

---

## Authentication API

### AUTH-001: Login Response Format

**Status**: ❓ Unknown

**Issue**: The expected response format for `/api/auth/login` is unclear.

**Questions**:
- Does the login endpoint return a session cookie automatically, or should we expect a token in the response body?
- What is the exact session cookie name and expiration behavior?
- Is there a refresh token mechanism for long-lived sessions?

**Current Assumption**: Session cookie named `filehub_session` with 24-hour expiration.

**Affected Code**: `AuthRepositoryImpl.kt`, `AuthInterceptor.kt`

---

### AUTH-002: Setup Endpoint Availability

**Status**: ❓ Unknown

**Issue**: The `/api/auth/setup` endpoint purpose is unclear.

**Questions**:
- Is this for creating the first admin user only, or any user registration?
- Is this endpoint typically enabled in production or admin-only?
- What fields are required vs. optional?

**Current Assumption**: Creates initial admin user with username, password, email.

**Affected Code**: `AuthRepositoryImpl.kt`, `SetupUseCase.kt`

---

### AUTH-003: Server Status Endpoint

**Status**: ❓ Unknown

**Issue**: The `/api/status` endpoint response format is assumed.

**Questions**:
- What are the possible values for the `status` field?
- What does the `database` field indicate?
- Is `has_users` the correct field name (snake_case) or `hasUsers` (camelCase)?

**Current Assumption**: Response has `status`, `database`, `has_users` fields. Status "ok" or "ready" means server is available.

**Affected Code**: `FileHubApi.kt`, `CheckServerStatusUseCase.kt`

---

## File Operations API

### FILE-001: List Directory Endpoint

**Status**: ❓ Unknown

**Issue**: The exact URL structure and parameters for listing directory contents.

**Questions**:
- Is the endpoint `/api/files/{repo}/list` or `/api/files/list/{repo}`?
- Is `path` a query parameter or path parameter?
- How is the root directory specified - empty string, `/`, or omitted?
- What is the default and maximum `limit` for pagination?
- Does `offset` work with cursor-based pagination or numeric offset?

**Current Assumption**: `/api/files/{repoName}/list?path={encodedPath}&offset={n}&limit={n}`

**Affected Code**: `FileHubApi.kt`, `FileRepositoryImpl.kt`

---

### FILE-002: File Path Encoding

**Status**: ⚠️ Needs Verification

**Issue**: How should file paths with special characters be encoded?

**Questions**:
- Should paths be URL-encoded when passed as query parameters?
- Are there any characters that are forbidden in file paths?
- How are nested paths handled (e.g., `/photos/2024/vacation.jpg`)?

**Current Assumption**: Using `URLEncoder.encode(path, "UTF-8")` for all path parameters.

**Affected Code**: `FileHubApi.kt`

---

### FILE-003: Download Endpoint Behavior

**Status**: ❓ Unknown

**Issue**: The download endpoint response format is unclear.

**Questions**:
- Does the endpoint return raw file content or a JSON response with download URL?
- Is there a size limit for direct downloads?
- Are large files automatically chunked on download?

**Current Assumption**: Returns raw file content as response body for all file sizes.

**Affected Code**: `FileHubApi.kt`, `UploadManager.kt`

---

## Upload Protocol

### UPLOAD-001: Chunked Upload Flow

**Status**: ❓ Unknown

**Issue**: The complete chunked upload protocol is based on assumptions.

**Questions**:
- **Begin Upload**: What parameters are required? (`path`, `size`, `checksum`)
- **Checksum Algorithm**: Is MD5 acceptable, or should we use SHA-256?
- **Chunk Size**: Who determines chunk size - client or server?
- **Resume Support**: Does `uploaded_chunks` response indicate already-uploaded chunks for resume?
- **Parallel Uploads**: Can chunks be uploaded in parallel or must be sequential?
- **Finalize**: What validation happens on finalize?

**Current Assumption**:
```
POST /api/files/{repo}/upload/begin?path={path}&size={size}&checksum={md5}
→ { upload_id, total_chunks, chunk_size, uploaded_chunks }

PUT /api/files/{repo}/upload/chunk?upload_id={id}&index={n}
→ { success, message }

POST /api/files/{repo}/upload/finalize?upload_id={id}
→ { etag, size }
```

**Affected Code**: `UploadManager.kt`, `FileHubApi.kt`

---

### UPLOAD-002: Upload Error Recovery

**Status**: ❓ Unknown

**Issue**: Error handling during chunked uploads is unclear.

**Questions**:
- If a chunk upload fails, should we retry the same chunk or restart from begin?
- How long does an upload session (`upload_id`) remain valid?
- Is there a cancel endpoint, or do incomplete uploads auto-cleanup?
- What happens if finalize is called with missing chunks?

**Current Assumption**: Failed chunks are retried, session times out after some period, cancel endpoint exists.

**Affected Code**: `UploadManager.kt`

---

### UPLOAD-003: Repository Creation

**Status**: ❓ Unknown

**Issue**: How are repositories created for uploads?

**Questions**:
- Does the first upload to a repo auto-create it?
- Is there a separate repo creation endpoint?
- What are repo naming restrictions?
- Is there a default repo for DCIM sync?

**Current Assumption**: Repositories auto-create on first upload. Using "dcim" as default repo name.

**Affected Code**: `SyncDcimUseCase.kt`, `SyncRepository.kt`

---

## Sync Protocol

### SYNC-001: Change Log Endpoint

**Status**: ❓ Unknown

**Issue**: The change detection protocol is based on DTO structure only.

**Questions**:
- What is the format of the `version` field - integer, UUID, timestamp?
- What operations are tracked? (create, modify, delete, move, copy)
- How are conflicts detected and reported?
- Is there a maximum number of changes returned per request?

**Current Assumption**: Version is a string, changes include operation type and path.

**Affected Code**: `FileHubApi.kt`, `SyncDto.kt`

---

### SYNC-002: Version Vector Format

**Status**: ❓ Unknown

**Issue**: The version vector structure for conflict detection.

**Questions**:
- What is the exact format of the `vector` field in version response?
- How should clients merge version vectors?
- What is the conflict resolution strategy?

**Current Assumption**: Not yet implemented - waiting for clarification.

**Affected Code**: `SyncDto.kt` (defined but not used)

---

### SYNC-003: Initial Sync Behavior

**Status**: ❓ Unknown

**Issue**: How should a client perform initial sync of existing files?

**Questions**:
- Should we fetch all files via list endpoint or is there a bulk export?
- Is there a way to get current version without changes?
- What's the recommended batch size for initial sync?

**Current Assumption**: Use list directory recursively with pagination.

**Affected Code**: `FileRepositoryImpl.kt`

---

## Error Handling

### ERROR-001: Error Response Format

**Status**: ❓ Unknown

**Issue**: Standard error response structure is unclear.

**Questions**:
- Do all errors return JSON with consistent structure?
- What fields are included? (error code, message, details, field errors)
- Are there standard HTTP status codes for specific errors?
- Is there a rate limit response with retry-after header?

**Current Assumption**: Errors return JSON with `success: false` and `message` field.

**Affected Code**: All API calls in `FileHubApi.kt`

---

### ERROR-002: Authentication Errors

**Status**: ❓ Unknown

**Issue**: How are authentication failures communicated?

**Questions**:
- Does expired session return 401 or a specific error code?
- Is there a token refresh endpoint?
- Should we redirect to login or attempt re-authentication?

**Current Assumption**: 401 triggers cookie clear and login redirect.

**Affected Code**: `AuthInterceptor.kt`

---

## DCIM Sync Specifics

### DCIM-001: Server Folder Structure

**Status**: ❓ Unknown

**Issue**: How should uploaded photos be organized on the server?

**Questions**:
- Should we preserve device folder structure or flatten?
- Is year/month organization (`/2024/02/IMG_001.jpg`) acceptable?
- How are duplicate filenames handled?
- Should we include device/camera metadata?

**Current Assumption**: Organize by `/{repo}/{year}/{month}/{filename}`.

**Affected Code**: `LocalMediaFile.kt` - `getServerPath()` method

---

### DCIM-002: Duplicate Detection

**Status**: ❓ Unknown

**Issue**: How does the server detect and handle duplicate files?

**Questions**:
- Is checksum-based deduplication server-side?
- Should client check for existing files before upload?
- What constitutes a duplicate - same checksum, same name, or both?

**Current Assumption**: Client tracks uploaded checksums in local database to avoid re-upload.

**Affected Code**: `SyncRepository.kt`, `SyncStateEntity.kt`

---

### DCIM-003: Metadata Preservation

**Status**: ❓ Unknown

**Issue**: EXIF metadata and file timestamps handling.

**Questions**:
- Does server preserve EXIF data or strip it?
- Should we send `dateTaken` as separate metadata?
- How is file modification time handled on server?

**Current Assumption**: Server preserves file content as-is. Client tracks `dateTaken` locally.

**Affected Code**: `LocalMediaFile.kt`, `SyncStateEntity.kt`

---

## Pending Clarifications Summary

| ID | Area | Priority | Status |
|----|------|----------|--------|
| AUTH-001 | Authentication | High | ❓ Unknown |
| AUTH-002 | Authentication | Medium | ❓ Unknown |
| AUTH-003 | Authentication | Medium | ❓ Unknown |
| FILE-001 | File Operations | High | ❓ Unknown |
| FILE-002 | File Operations | Medium | ⚠️ Needs Verification |
| FILE-003 | File Operations | Medium | ❓ Unknown |
| UPLOAD-001 | Upload Protocol | High | ❓ Unknown |
| UPLOAD-002 | Upload Protocol | High | ❓ Unknown |
| UPLOAD-003 | Upload Protocol | Medium | ❓ Unknown |
| SYNC-001 | Sync Protocol | Medium | ❓ Unknown |
| SYNC-002 | Sync Protocol | Low | ❓ Unknown |
| SYNC-003 | Sync Protocol | Low | ❓ Unknown |
| ERROR-001 | Error Handling | High | ❓ Unknown |
| ERROR-002 | Error Handling | High | ❓ Unknown |
| DCIM-001 | DCIM Sync | Medium | ❓ Unknown |
| DCIM-002 | DCIM Sync | Medium | ❓ Unknown |
| DCIM-003 | DCIM Sync | Low | ❓ Unknown |

---

## Notes for Server Team

When responding to these issues, please provide:

1. **Endpoint URLs** - Exact paths with HTTP methods
2. **Request/Response Examples** - JSON bodies with all fields
3. **Error Codes** - List of possible errors per endpoint
4. **Field Types** - Explicit types (string, integer, boolean, null)
5. **Field Naming** - Confirm snake_case vs camelCase
6. **Rate Limits** - Any throttling or quotas
7. **Version** - Server version these APIs apply to

---

## Changelog

- **2026-02-26**: Initial document created with 17 issues across 6 categories
