# Life App Project Summary

## 1. Overview

Life App is a status-first personal project.

- Core object: **status event** (`source`, `status`, `observed_at`, `expires_at`)
- Companion object: **post** (`content`, `tags`, `location`)
- Public result: one personal page showing current status and post stream

## 2. Current Architecture

### Android (`app/`)
- `LifeApp.kt`: app-level dependency holder
- `MainActivity.kt`: Compose navigation host
- `viewmodel/`
  - `StatusViewModel`
  - `WebProfileViewModel`
  - `PublishViewModel`
- `data/repository/WebRepository.kt`: API access wrapper
- `data/sync/SyncPreferences.kt`: local settings storage

### Web + API (`Server/`)
- `Server/worker`: Cloudflare Worker + D1 API
- `Server/public`: static frontend for public page and publish page

## 3. API Scope

### Active endpoints
- `POST /api/v1/status/events`
- `GET /api/v1/status`
- `POST /api/v1/posts`
- `PUT /api/v1/posts/:postId`
- `DELETE /api/v1/posts/:postId`
- `GET /api/v1/posts`
- `GET /api/v1/public/feed`

### Compatibility endpoints
- `POST /api/v1/sync`
- `GET /api/v1/tasks`

## 4. Status Decision Rules

1. Only `expires_at > now` is valid.
2. Primary status selection:
   - Prefer latest valid `source = manual`
   - Otherwise latest valid status from other sources
   - Otherwise `Offline`

## 5. Data (D1)

Main tables:
- `posts`
- `status_events`
- `status_sources`

Legacy-compatible tables remain from `0001_init.sql` for old task sync.

## 6. Delivery

- Worker deploy workflow: `.github/workflows/worker_deploy.yml`
- Android release workflow: `.github/workflows/android_release.yml`
