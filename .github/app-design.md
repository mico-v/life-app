# App Design: Life App (Status First)

## Product Focus
Life App is now a **personal status + post stream** product.

Primary goals:
- Publish short personal status updates with expiry (`expires_at`)
- Publish lightweight posts
- Render a public status board page
- Keep old task sync API only for compatibility

## Experience Surfaces

### Android App
- `Status`: read public feed
- `Profile`: local profile/config info
- `Publish`: publish status and posts

### Web
- `/`: public dashboard (primary status, source list, posts)
- `/?view=publish` or `/publish`: publish/manage interface

### Backend
- Cloudflare Worker + D1
- Status validity rule: only `expires_at > now` is active
- Primary status rule: `manual` first, then latest active source, else `Offline`

## Non-goals (for now)
- Multi-user routing
- Complex social interactions
- Rebuilding legacy task UI

## Source of Truth
- API behavior: `Server/worker/src/index.js`
- Public UI behavior: `Server/public/app.js` + `Server/public/config.json`
- Android behavior: `app/src/main/java/com/example/android16demo`
