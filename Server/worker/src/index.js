import { Hono } from 'hono'

const app = new Hono()

const DEFAULT_STATUS_TTL_MS = 15 * 60 * 1000

function jsonError(c, status, message) {
  return c.json({ success: false, message }, status)
}

function parseBool(value) {
  return value === true || value === 1 || value === '1'
}

function asText(value) {
  if (typeof value !== 'string') return null
  const trimmed = value.trim()
  return trimmed.length > 0 ? trimmed : null
}

function asTimestamp(value, fallback) {
  if (value === null || value === undefined || value === '') return fallback
  const num = Number(value)
  if (!Number.isFinite(num)) return fallback
  return Math.floor(num)
}

function parseJsonBody(c) {
  return c.req.json().catch(() => ({}))
}

function parseMeta(meta) {
  if (meta === null || meta === undefined) return null
  if (typeof meta === 'string') {
    try {
      JSON.parse(meta)
      return meta
    } catch {
      return JSON.stringify({ raw: meta })
    }
  }
  if (typeof meta === 'object') {
    try {
      return JSON.stringify(meta)
    } catch {
      return null
    }
  }
  return null
}

function parseMetaForResponse(raw) {
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return raw
  }
}

function normalizeSource(source) {
  return asText(source)?.toLowerCase() ?? null
}

function getTtlMs(env, key, fallback = DEFAULT_STATUS_TTL_MS) {
  const raw = env[key]
  const parsed = Number(raw)
  if (!Number.isFinite(parsed) || parsed <= 0) return fallback
  return Math.floor(parsed)
}

async function ensureClient(db, clientToken, now) {
  await db.prepare(
    `INSERT INTO clients (client_token, created_at, last_sync_at)
     VALUES (?, ?, ?)
     ON CONFLICT(client_token) DO UPDATE SET last_sync_at=excluded.last_sync_at`
  ).bind(clientToken, now, now).run()
}

function mapStatusSourceRow(row) {
  return {
    source: row.source,
    status: row.status,
    observed_at: row.observed_at,
    expires_at: row.expires_at,
    meta: parseMetaForResponse(row.meta)
  }
}

function buildPrimaryStatus(sources) {
  const manual = sources.find((s) => s.source === 'manual')
  if (manual) {
    return { ...manual, offline: false }
  }
  if (sources.length > 0) {
    return { ...sources[0], offline: false }
  }
  return {
    source: 'system',
    status: 'Offline',
    observed_at: null,
    expires_at: null,
    meta: null,
    offline: true
  }
}

async function getValidStatusSources(db, clientToken, now) {
  const result = await db.prepare(
    `SELECT source, status, observed_at, expires_at, meta
     FROM status_sources
     WHERE client_token = ? AND expires_at > ?
     ORDER BY observed_at DESC`
  ).bind(clientToken, now).all()

  return (result.results || []).map(mapStatusSourceRow)
}

async function upsertStatusSource(db, payload) {
  const { clientToken, source, status, observedAt, expiresAt, meta, now } = payload

  await db.prepare(
    `INSERT INTO status_events (
      client_token, source, status, observed_at, expires_at, meta, created_at
    ) VALUES (?, ?, ?, ?, ?, ?, ?)`
  ).bind(
    clientToken,
    source,
    status,
    observedAt,
    expiresAt,
    meta,
    now
  ).run()

  await db.prepare(
    `INSERT INTO status_sources (
      client_token, source, status, observed_at, expires_at, meta, updated_at
    ) VALUES (?, ?, ?, ?, ?, ?, ?)
    ON CONFLICT(client_token, source) DO UPDATE SET
      status=excluded.status,
      observed_at=excluded.observed_at,
      expires_at=excluded.expires_at,
      meta=excluded.meta,
      updated_at=excluded.updated_at`
  ).bind(
    clientToken,
    source,
    status,
    observedAt,
    expiresAt,
    meta,
    now
  ).run()
}

async function getStatusView(db, clientToken, now) {
  const sources = await getValidStatusSources(db, clientToken, now)
  const primary = buildPrimaryStatus(sources)

  return {
    primary,
    sources,
    offline: primary.offline
  }
}

async function resolvePublicClientToken(db) {
  const fromStatus = await db.prepare(
    'SELECT client_token FROM status_sources ORDER BY updated_at DESC LIMIT 1'
  ).first()
  if (fromStatus?.client_token) return fromStatus.client_token

  const fromPosts = await db.prepare(
    'SELECT client_token FROM posts WHERE deleted_at IS NULL ORDER BY created_at DESC LIMIT 1'
  ).first()
  if (fromPosts?.client_token) return fromPosts.client_token

  const fromClient = await db.prepare(
    'SELECT client_token FROM clients ORDER BY last_sync_at DESC LIMIT 1'
  ).first()

  return fromClient?.client_token ?? null
}

async function auth(c, next) {
  const serverPassword = c.env.SERVER_PASSWORD
  if (!serverPassword) {
    return jsonError(c, 500, 'Server password not configured')
  }

  const clientToken = c.req.header('x-client-token')
  const providedPassword = c.req.header('x-server-password')

  if (!clientToken) return jsonError(c, 401, 'Missing client token')
  if (!providedPassword) return jsonError(c, 401, 'Missing server password')
  if (providedPassword !== serverPassword) return jsonError(c, 403, 'Invalid server password')

  c.set('clientToken', clientToken)
  await next()
}

app.get('/api/v1/health', (c) => c.json({ success: true, message: 'Life App Worker is running', serverTime: Date.now() }))

app.post('/api/v1/sync', auth, async (c) => {
  const db = c.env.DB
  const clientToken = c.get('clientToken')
  const body = await parseJsonBody(c)
  const clientTasks = Array.isArray(body.tasks) ? body.tasks : []
  const lastSync = body.last_sync
  const now = Date.now()

  await ensureClient(db, clientToken, now)

  for (const task of clientTasks) {
    await db.prepare(
      `INSERT INTO tasks (
        client_token, task_id, title, description, created_at, start_time, deadline,
        is_completed, completed_at, progress, priority, is_public, tags, updated_at
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      ON CONFLICT(client_token, task_id) DO UPDATE SET
        title=excluded.title,
        description=excluded.description,
        created_at=excluded.created_at,
        start_time=excluded.start_time,
        deadline=excluded.deadline,
        is_completed=excluded.is_completed,
        completed_at=excluded.completed_at,
        progress=excluded.progress,
        priority=excluded.priority,
        is_public=excluded.is_public,
        tags=excluded.tags,
        updated_at=excluded.updated_at`
    ).bind(
      clientToken,
      task.id,
      task.title,
      task.description ?? null,
      task.created_at ?? task.createdAt ?? now,
      task.start_time ?? task.startTime ?? null,
      task.deadline ?? null,
      parseBool(task.is_completed ?? task.isCompleted) ? 1 : 0,
      task.completed_at ?? task.completedAt ?? null,
      task.progress ?? 0,
      task.priority ?? 1,
      parseBool(task.is_public ?? task.isPublic) ? 1 : 0,
      task.tags ?? null,
      now
    ).run()
  }

  let updatedTasks = []
  if (lastSync) {
    const result = await db.prepare(
      `SELECT * FROM tasks WHERE client_token = ? AND updated_at > ?`
    ).bind(clientToken, Number(lastSync)).all()

    updatedTasks = (result.results || []).map((t) => ({
      id: t.task_id,
      title: t.title,
      description: t.description,
      created_at: t.created_at,
      start_time: t.start_time,
      deadline: t.deadline,
      is_completed: t.is_completed === 1,
      completed_at: t.completed_at,
      progress: t.progress,
      priority: t.priority,
      is_public: t.is_public === 1,
      tags: t.tags
    }))
  }

  return c.json({
    success: true,
    message: 'Sync completed',
    server_time: now,
    updated_tasks: updatedTasks
  })
})

app.get('/api/v1/tasks', auth, async (c) => {
  const db = c.env.DB
  const clientToken = c.get('clientToken')
  const result = await db.prepare(`SELECT * FROM tasks WHERE client_token = ? ORDER BY created_at DESC`).bind(clientToken).all()
  const tasks = (result.results || []).map((t) => ({
    id: t.task_id,
    title: t.title,
    description: t.description,
    created_at: t.created_at,
    start_time: t.start_time,
    deadline: t.deadline,
    is_completed: t.is_completed === 1,
    completed_at: t.completed_at,
    progress: t.progress,
    priority: t.priority,
    is_public: t.is_public === 1,
    tags: t.tags
  }))
  return c.json({ success: true, tasks })
})

app.post('/api/v1/posts', auth, async (c) => {
  const db = c.env.DB
  const clientToken = c.get('clientToken')
  const body = await parseJsonBody(c)
  const now = Date.now()

  const content = asText(body.content)
  if (!content) return jsonError(c, 400, 'content is required')

  const tags = asText(body.tags)
  const location = asText(body.location)
  const visibility = asText(body.visibility) || 'public'

  if (!['public', 'private'].includes(visibility)) {
    return jsonError(c, 400, 'visibility must be public or private')
  }

  await ensureClient(db, clientToken, now)

  const postId = crypto.randomUUID()
  await db.prepare(
    `INSERT INTO posts (
      client_token, post_id, content, tags, location, visibility, created_at, updated_at, deleted_at
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NULL)`
  ).bind(
    clientToken,
    postId,
    content,
    tags,
    location,
    visibility,
    now,
    now
  ).run()

  return c.json({
    success: true,
    post: {
      id: postId,
      content,
      tags,
      location,
      visibility,
      created_at: now,
      updated_at: now
    }
  })
})

app.put('/api/v1/posts/:postId', auth, async (c) => {
  const db = c.env.DB
  const clientToken = c.get('clientToken')
  const postId = c.req.param('postId')
  const body = await parseJsonBody(c)
  const now = Date.now()

  const content = asText(body.content)
  if (!content) return jsonError(c, 400, 'content is required')

  const tags = asText(body.tags)
  const location = asText(body.location)
  const visibility = asText(body.visibility) || 'public'

  if (!['public', 'private'].includes(visibility)) {
    return jsonError(c, 400, 'visibility must be public or private')
  }

  const updateResult = await db.prepare(
    `UPDATE posts
     SET content = ?, tags = ?, location = ?, visibility = ?, updated_at = ?
     WHERE client_token = ? AND post_id = ? AND deleted_at IS NULL`
  ).bind(
    content,
    tags,
    location,
    visibility,
    now,
    clientToken,
    postId
  ).run()

  if ((updateResult.meta?.changes || 0) === 0) {
    return jsonError(c, 404, 'post not found')
  }

  return c.json({
    success: true,
    post: {
      id: postId,
      content,
      tags,
      location,
      visibility,
      updated_at: now
    }
  })
})

app.delete('/api/v1/posts/:postId', auth, async (c) => {
  const db = c.env.DB
  const clientToken = c.get('clientToken')
  const postId = c.req.param('postId')
  const now = Date.now()

  const deleteResult = await db.prepare(
    `UPDATE posts
     SET deleted_at = ?, updated_at = ?
     WHERE client_token = ? AND post_id = ? AND deleted_at IS NULL`
  ).bind(
    now,
    now,
    clientToken,
    postId
  ).run()

  if ((deleteResult.meta?.changes || 0) === 0) {
    return jsonError(c, 404, 'post not found')
  }

  return c.json({ success: true, message: 'post deleted' })
})

app.get('/api/v1/posts', auth, async (c) => {
  const db = c.env.DB
  const clientToken = c.get('clientToken')

  const result = await db.prepare(
    `SELECT post_id, content, tags, location, visibility, created_at, updated_at
     FROM posts
     WHERE client_token = ? AND deleted_at IS NULL
     ORDER BY created_at DESC`
  ).bind(clientToken).all()

  const posts = (result.results || []).map((row) => ({
    id: row.post_id,
    content: row.content,
    tags: row.tags,
    location: row.location,
    visibility: row.visibility,
    created_at: row.created_at,
    updated_at: row.updated_at
  }))

  return c.json({ success: true, posts })
})

app.post('/api/v1/status/events', auth, async (c) => {
  const db = c.env.DB
  const clientToken = c.get('clientToken')
  const body = await parseJsonBody(c)
  const now = Date.now()

  const source = normalizeSource(body.source)
  const status = asText(body.status)

  if (!source) return jsonError(c, 400, 'source is required')
  if (!status) return jsonError(c, 400, 'status is required')

  const observedAt = asTimestamp(body.observed_at, now)
  const ttlMs = getTtlMs(c.env, 'DEFAULT_STATUS_TTL_MS', DEFAULT_STATUS_TTL_MS)
  const expiresAt = asTimestamp(body.expires_at, observedAt + ttlMs)

  if (!Number.isFinite(observedAt) || observedAt <= 0) return jsonError(c, 400, 'invalid observed_at')
  if (!Number.isFinite(expiresAt) || expiresAt <= observedAt) return jsonError(c, 400, 'expires_at must be greater than observed_at')

  const meta = parseMeta(body.meta)

  await ensureClient(db, clientToken, now)
  await upsertStatusSource(db, {
    clientToken,
    source,
    status,
    observedAt,
    expiresAt,
    meta,
    now
  })

  return c.json({
    success: true,
    source_status: {
      source,
      status,
      observed_at: observedAt,
      expires_at: expiresAt,
      meta: parseMetaForResponse(meta)
    }
  })
})

app.get('/api/v1/status', auth, async (c) => {
  const db = c.env.DB
  const clientToken = c.get('clientToken')
  const now = Date.now()

  const statusView = await getStatusView(db, clientToken, now)

  return c.json({
    success: true,
    primary_status: statusView.primary,
    sources: statusView.sources,
    offline: statusView.offline,
    server_time: now
  })
})

app.get('/api/v1/public/feed', async (c) => {
  const db = c.env.DB
  const now = Date.now()
  const clientToken = await resolvePublicClientToken(db)

  if (!clientToken) {
    return c.json({
      success: true,
      status: {
        primary: {
          source: 'system',
          status: 'Offline',
          observed_at: null,
          expires_at: null,
          meta: null,
          offline: true
        },
        sources: [],
        offline: true
      },
      posts: [],
      stats: {
        totalPosts: 0,
        activeSources: 0
      },
      server_time: now
    })
  }

  const postsResult = await db.prepare(
    `SELECT post_id, content, tags, location, visibility, created_at, updated_at
     FROM posts
     WHERE client_token = ? AND visibility = 'public' AND deleted_at IS NULL
     ORDER BY created_at DESC
     LIMIT 100`
  ).bind(clientToken).all()

  const posts = (postsResult.results || []).map((row) => ({
    id: row.post_id,
    content: row.content,
    tags: row.tags,
    location: row.location,
    visibility: row.visibility,
    created_at: row.created_at,
    updated_at: row.updated_at
  }))

  const statusView = await getStatusView(db, clientToken, now)

  return c.json({
    success: true,
    status: {
      primary: statusView.primary,
      sources: statusView.sources,
      offline: statusView.offline
    },
    posts,
    stats: {
      totalPosts: posts.length,
      activeSources: statusView.sources.length
    },
    server_time: now
  })
})

app.get('/publish', async (c) => {
  const url = new URL('/index.html', c.req.url)
  return c.env.ASSETS.fetch(new Request(url.toString(), c.req.raw))
})

app.get('/publish/*', async (c) => {
  const url = new URL('/index.html', c.req.url)
  return c.env.ASSETS.fetch(new Request(url.toString(), c.req.raw))
})

app.get('*', async (c) => c.env.ASSETS.fetch(c.req.raw))

export default app
