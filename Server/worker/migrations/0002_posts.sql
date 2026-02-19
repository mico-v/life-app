CREATE TABLE IF NOT EXISTS posts (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  client_token TEXT NOT NULL,
  post_id TEXT NOT NULL,
  content TEXT NOT NULL,
  tags TEXT,
  location TEXT,
  visibility TEXT NOT NULL DEFAULT 'public',
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  deleted_at INTEGER,
  UNIQUE(client_token, post_id)
);

CREATE INDEX IF NOT EXISTS idx_posts_client_token ON posts(client_token);
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts(created_at);
CREATE INDEX IF NOT EXISTS idx_posts_visibility ON posts(visibility);
CREATE INDEX IF NOT EXISTS idx_posts_deleted_at ON posts(deleted_at);
CREATE INDEX IF NOT EXISTS idx_posts_client_created ON posts(client_token, created_at DESC);
