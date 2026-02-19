# 02 - Data Layer

## Current State

The app no longer uses Room entities/DAOs as the primary path.
Current data layer is lightweight and network-first.

## Core Components

### `SyncPreferences`
- File: `app/src/main/java/com/example/android16demo/data/sync/SyncPreferences.kt`
- Stores:
  - `serverUrl`
  - `serverPassword`
  - `clientToken`
  - language/theme and profile-related local settings

### `WebRepository`
- File: `app/src/main/java/com/example/android16demo/data/repository/WebRepository.kt`
- Responsibilities:
  - Fetch public feed
  - Publish manual status
  - Publish post
  - Query my posts
  - Delete post

## Notes

- Legacy task-related code has been removed from active app flow.
- Worker still keeps old task sync endpoints for compatibility.
