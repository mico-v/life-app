# 04 - ViewModel Layer

## Overview

Current ViewModel layer is split by screen and centered on status/post publishing workflows.

## ViewModels

### `StatusViewModel`
- File: `app/src/main/java/com/example/android16demo/viewmodel/StatusViewModel.kt`
- Loads and refreshes public feed.
- Exposes UI state for:
  - primary status
  - status sources
  - posts
  - loading/error flags

### `WebProfileViewModel`
- File: `app/src/main/java/com/example/android16demo/viewmodel/WebProfileViewModel.kt`
- Manages local profile/config fields.
- Handles save/reset style operations for profile-related values.

### `PublishViewModel`
- File: `app/src/main/java/com/example/android16demo/viewmodel/PublishViewModel.kt`
- Handles:
  - publish manual status
  - publish post
  - load my posts
  - delete my post
- Also tracks action messages and validation errors.

## Factory Pattern

`MainActivity.kt` uses a lightweight helper factory (`webFactory`) instead of DI frameworks.
