# Life App Cloudflare Worker Deployment

This directory supports:
- Local preview with `wrangler dev`
- One-command release (D1 migration + Worker deploy)
- GitHub Actions auto deploy on `main`

## 1) Install

```bash
cd Server/worker
npm install
```

## 2) One-time initialization

If you already created the Worker and D1 once, skip creation and keep using the same config.

Create D1 (first time only):

```bash
npx wrangler d1 create life-app-db
```

Copy generated `database_id` into `wrangler.toml`:
- `name` should stay your existing worker name
- `database_id` should stay your existing D1 id

Configure runtime secret (first time or rotation):

```bash
npx wrangler secret put SERVER_PASSWORD
```

## 3) Local development

```bash
npm run dev
```

Local D1 migration (for dev):

```bash
npm run db:migrate:local
```

## 4) Manual production release

This runs remote D1 migrations first, then deploys Worker:

```bash
npm run release
```

You can still run steps manually:

```bash
npm run db:migrate:remote
npm run deploy
```

## 5) GitHub Actions auto deploy

Workflow file: `.github/workflows/worker_deploy.yml`

Trigger:
- Push to `main` when files under `Server/worker/**` or `Server/public/**` change
- Manual run (`workflow_dispatch`)

Required GitHub repository secrets:
- `CLOUDFLARE_API_TOKEN`
- `CLOUDFLARE_ACCOUNT_ID`

Token scope should include permissions for Workers and D1 on your Cloudflare account.

## Notes

- D1 migrations are incremental; only new migration files are applied remotely.
- Static assets in `Server/public` and API routes under `/api/v1/*` are served by the same Worker.
