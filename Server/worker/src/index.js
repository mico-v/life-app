import { Hono } from 'hono'

const app = new Hono()

const DEFAULT_PROFILE = {
  displayName: 'Life App User',
  motto: 'Push to Start, Pop to Finish',
  status: 'Available'
}

function jsonError(c, status, message) {
  return c.json({ success: false, message }, status)
}

function parseBool(value) {
  return value === true || value === 1 || value === '1'
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
  const body = await c.req.json().catch(() => ({}))
  const clientTasks = Array.isArray(body.tasks) ? body.tasks : []
  const lastSync = body.last_sync
  const profile = body.profile
  const now = Date.now()

  await db.prepare(
    `INSERT INTO clients (client_token, created_at, last_sync_at)
     VALUES (?, ?, ?)
     ON CONFLICT(client_token) DO UPDATE SET last_sync_at=excluded.last_sync_at`
  ).bind(clientToken, now, now).run()

  if (profile) {
    await db.prepare(
      `INSERT INTO profiles (client_token, display_name, motto, status, updated_at)
       VALUES (?, ?, ?, ?, ?)
       ON CONFLICT(client_token) DO UPDATE SET
         display_name=excluded.display_name,
         motto=excluded.motto,
         status=excluded.status,
         updated_at=excluded.updated_at`
    ).bind(
      clientToken,
      profile.displayName || DEFAULT_PROFILE.displayName,
      profile.motto || DEFAULT_PROFILE.motto,
      profile.status || DEFAULT_PROFILE.status,
      now
    ).run()
  }

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

app.get('/api/v1/profile', auth, async (c) => {
  const db = c.env.DB
  const clientToken = c.get('clientToken')
  const profileResult = await db.prepare('SELECT * FROM profiles WHERE client_token = ?').bind(clientToken).first()
  const tasksResult = await db.prepare('SELECT * FROM tasks WHERE client_token = ?').bind(clientToken).all()
  const tasks = tasksResult.results || []
  const todayStart = new Date(); todayStart.setHours(0,0,0,0)
  const completedToday = tasks.filter(t => t.is_completed === 1 && t.completed_at && t.completed_at >= todayStart.getTime()).length

  return c.json({
    success: true,
    profile: profileResult ? {
      displayName: profileResult.display_name,
      motto: profileResult.motto,
      status: profileResult.status
    } : DEFAULT_PROFILE,
    stats: {
      activeTasks: tasks.filter(t => t.is_completed === 0).length,
      completedTasks: tasks.filter(t => t.is_completed === 1).length,
      completedToday,
      totalTasks: tasks.length
    }
  })
})

app.put('/api/v1/profile', auth, async (c) => {
  const db = c.env.DB
  const clientToken = c.get('clientToken')
  const body = await c.req.json().catch(() => ({}))
  await db.prepare(
    `INSERT INTO profiles (client_token, display_name, motto, status, updated_at)
     VALUES (?, ?, ?, ?, ?)
     ON CONFLICT(client_token) DO UPDATE SET
       display_name=excluded.display_name,
       motto=excluded.motto,
       status=excluded.status,
       updated_at=excluded.updated_at`
  ).bind(
    clientToken,
    body.displayName || DEFAULT_PROFILE.displayName,
    body.motto || DEFAULT_PROFILE.motto,
    body.status || DEFAULT_PROFILE.status,
    Date.now()
  ).run()

  return c.json({ success: true, message: 'Profile updated' })
})

app.get('/api/v1/public/dashboard', async (c) => {
  const db = c.env.DB
  const profilesResult = await db.prepare('SELECT display_name, motto, status FROM profiles').all()
  const tasksResult = await db.prepare('SELECT * FROM tasks WHERE is_public = 1').all()
  const tasks = tasksResult.results || []
  const now = Date.now()

  return c.json({
    success: true,
    profiles: (profilesResult.results || []).map((p) => ({
      displayName: p.display_name,
      motto: p.motto,
      status: p.status
    })),
    publicTasks: tasks.map((t) => ({
      id: t.task_id,
      title: t.title,
      description: t.description,
      deadline: t.deadline,
      priority: t.priority,
      progress: t.progress,
      isCompleted: t.is_completed === 1,
      completedAt: t.completed_at,
      tags: t.tags
    })),
    stats: {
      totalPublicTasks: tasks.length,
      activeTasks: tasks.filter(t => t.is_completed === 0).length,
      overdueCount: tasks.filter(t => t.is_completed === 0 && t.deadline && t.deadline < now).length
    }
  })
})

app.get('*', async (c) => c.env.ASSETS.fetch(c.req.raw))

export default app
