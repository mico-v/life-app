CREATE TABLE IF NOT EXISTS status_events (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  client_token TEXT NOT NULL,
  source TEXT NOT NULL,
  status TEXT NOT NULL,
  observed_at INTEGER NOT NULL,
  expires_at INTEGER NOT NULL,
  meta TEXT,
  created_at INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS status_sources (
  client_token TEXT NOT NULL,
  source TEXT NOT NULL,
  status TEXT NOT NULL,
  observed_at INTEGER NOT NULL,
  expires_at INTEGER NOT NULL,
  meta TEXT,
  updated_at INTEGER NOT NULL,
  PRIMARY KEY (client_token, source)
);

CREATE INDEX IF NOT EXISTS idx_status_sources_client_expires ON status_sources(client_token, expires_at);
CREATE INDEX IF NOT EXISTS idx_status_events_client_observed ON status_events(client_token, observed_at DESC);
