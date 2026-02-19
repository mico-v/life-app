# Life App Worker (Cloudflare)

Cloudflare Worker + D1 backend for Life App status/post stream.

## 1. Install

```bash
cd Server/worker
corepack enable
pnpm install
```

## 2. Configure

### D1 database

First time only:

```bash
pnpm dlx wrangler d1 create life-app-db
```

Put `database_id` into `wrangler.toml`.

### Secrets

Required:
- `SERVER_PASSWORD`

Optional:
- `DEFAULT_STATUS_TTL_MS` (default `900000`)

Set secret:

```bash
pnpm dlx wrangler secret put SERVER_PASSWORD
```

## 3. Local Development

```bash
pnpm run db:migrate:local
pnpm run dev
```

## 4. Deploy

Manual release:

```bash
pnpm run db:migrate:remote
pnpm run deploy
```

or

```bash
pnpm run release
```

## 5. API Overview

### Auth required (`x-client-token`, `x-server-password`)
- `POST /api/v1/status/events`
- `GET /api/v1/status`
- `POST /api/v1/posts`
- `PUT /api/v1/posts/:postId`
- `DELETE /api/v1/posts/:postId`
- `GET /api/v1/posts`

### Public
- `GET /api/v1/public/feed`
- `GET /api/v1/health`

### Compatibility
Old task APIs are still present for transition:
- `POST /api/v1/sync`
- `GET /api/v1/tasks`

## 6. Status Rules

- If `expires_at` is not provided, backend uses `observed_at + DEFAULT_STATUS_TTL_MS`.
- Valid status means `expires_at > now`.
- Primary status selection:
  1. latest valid `manual`
  2. else latest valid from other sources
  3. else `Offline`

## 7. Migrations

- `migrations/0001_init.sql` (legacy task/profile base)
- `migrations/0002_posts.sql`
- `migrations/0003_status.sql`

Apply migrations before deploy.
