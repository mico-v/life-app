# 06 - Background Workers

## Current State

Android-side WorkManager workers are not part of the active app flow now.

- `app/src/main/java/com/example/android16demo/worker/` is currently empty.
- No scheduled local job is required for core status/post publishing.

## Why

The product focus moved to:
- direct publish actions
- pull-based feed refresh
- backend-managed status expiration

## Future Options

If needed later, workers can be reintroduced for:
- periodic background refresh
- retry queue for failed publishes
- local reminders unrelated to task management
