# 03 - Network Layer

## API Interface

- File: `app/src/main/java/com/example/android16demo/network/api/LifeAppApi.kt`

Implemented endpoints:
- `GET /api/v1/public/feed`
- `POST /api/v1/status/events`
- `POST /api/v1/posts`
- `GET /api/v1/posts`
- `DELETE /api/v1/posts/{postId}`

## DTO Models

- File: `app/src/main/java/com/example/android16demo/network/model/ApiModels.kt`

Main models:
- `PublicFeedResponse`
- `FeedStatusDto`, `StatusSourceDto`
- `PostDtoV2`
- `StatusEventRequest`, `StatusEventResponse`
- `CreatePostRequest`, `CreatePostResponse`
- `PostsResponse`, `ApiMessageResponse`

## Client Construction

- File: `app/src/main/java/com/example/android16demo/network/RetrofitClient.kt`
- Base URL comes from `SyncPreferences.serverUrl` through `WebRepository`.

## Auth Contract

For write/private APIs, headers are required:
- `x-client-token`
- `x-server-password`

Public feed does not require auth.
