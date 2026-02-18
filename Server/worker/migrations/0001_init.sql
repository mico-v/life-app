CREATE TABLE IF NOT EXISTS clients (
  client_token TEXT PRIMARY KEY,
  created_at INTEGER NOT NULL,
  last_sync_at INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS profiles (
  client_token TEXT PRIMARY KEY,
  display_name TEXT NOT NULL,
  motto TEXT NOT NULL,
  status TEXT NOT NULL,
  updated_at INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS tasks (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  client_token TEXT NOT NULL,
  task_id TEXT NOT NULL,
  title TEXT NOT NULL,
  description TEXT,
  created_at INTEGER NOT NULL,
  start_time INTEGER,
  deadline INTEGER,
  is_completed INTEGER NOT NULL DEFAULT 0,
  completed_at INTEGER,
  progress REAL NOT NULL DEFAULT 0,
  priority INTEGER NOT NULL DEFAULT 1,
  is_public INTEGER NOT NULL DEFAULT 0,
  tags TEXT,
  updated_at INTEGER NOT NULL,
  UNIQUE(client_token, task_id)
);

CREATE INDEX IF NOT EXISTS idx_tasks_client_token ON tasks(client_token);
CREATE INDEX IF NOT EXISTS idx_tasks_updated_at ON tasks(updated_at);
CREATE INDEX IF NOT EXISTS idx_tasks_public ON tasks(is_public);
