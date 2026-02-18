# Life App Cloudflare Worker Deployment

## 1) Install

```bash
cd Server/worker
npm install
```

## 2) Create D1 database

```bash
npx wrangler d1 create life-app-db
```

Copy generated `database_id` into `wrangler.toml`.

## 3) Apply migrations

```bash
npm run db:migrate:remote
```

## 4) Configure secret

```bash
npx wrangler secret put SERVER_PASSWORD
```

## 5) Deploy

```bash
npm run deploy
```

After deployment, static assets in `Server/public` and API routes under `/api/v1/*` are served by the same Worker.
