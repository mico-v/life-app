class LifeAppV11 {
  constructor() {
    this.path = window.location.pathname
    this.query = new URLSearchParams(window.location.search)
    this.isPublish = this.query.get('view') === 'publish' || this.path.startsWith('/publish')
    this.feed = null
    this.refreshSeconds = 30
    this.config = {
      siteTitle: 'Life App - Status Board',
      brandName: 'Life App',
      ownerName: 'Life App User',
      ownerMotto: '',
      motto: '实时状态与帖子流',
      headlineTemplate: '看看 {name} 在干什么 O.O',
      profileNameTemplate: '{name}',
      profileMottoTemplate: '{motto}',
      sectionTitles: {
        primaryStatus: '当前主状态',
        sourceStatus: '来源状态',
        posts: '最新帖子',
        stats: '状态概览',
        links: '外部链接'
      },
      statsLabels: {
        postCount: '帖子数',
        sourceCount: '激活来源',
        primaryStatus: '主状态'
      },
      links: [
        { label: 'Steam Profile', url: 'https://steamcommunity.com/', icon: 'open_in_new' }
      ]
    }

    this.bootstrap()
  }

  async bootstrap() {
    await this.loadConfig()
    this.applyConfig()
    this.initTabs()
    this.initView()

    if (this.isPublish) {
      this.initManage()
    } else {
      this.loadFeed()
      setInterval(() => this.loadFeed(), this.refreshSeconds * 1000)
    }
  }

  async loadConfig() {
    try {
      const res = await fetch(`/config.json?v=${Date.now()}`, { cache: 'no-store' })
      if (!res.ok) {
        console.warn('config.json not loaded:', res.status)
        return
      }
      const data = await res.json()
      if (data && typeof data === 'object') {
        this.config = { ...this.config, ...data }
      }
    } catch (err) {
      console.warn('config.json parse failed:', err)
    }
  }

  applyConfig() {
    document.title = this.config.siteTitle || document.title

    const brandName = document.getElementById('brand-name')
    if (brandName) brandName.textContent = this.config.brandName || 'Life App'

    const motto = document.getElementById('motto')
    if (motto) motto.textContent = this.config.motto || '实时状态与帖子流'

    this.applyTextConfig()
    this.renderExternalLinks()
  }

  applyTextConfig() {
    const titles = this.config.sectionTitles || {}
    const statsLabels = this.config.statsLabels || {}

    const map = [
      ['title-primary-status', titles.primaryStatus],
      ['title-source-status', titles.sourceStatus],
      ['title-posts', titles.posts],
      ['title-stats', titles.stats],
      ['title-links', titles.links],
      ['label-post-count', statsLabels.postCount],
      ['label-source-count', statsLabels.sourceCount],
      ['label-primary-status', statsLabels.primaryStatus]
    ]

    map.forEach(([id, text]) => {
      if (!text) return
      const el = document.getElementById(id)
      if (el) el.textContent = text
    })
  }

  renderExternalLinks() {
    const container = document.getElementById('external-links')
    if (!container) return

    const links = Array.isArray(this.config.links) ? this.config.links : []
    container.innerHTML = links.map((item) => {
      const label = this.escapeHtml(item.label || 'Link')
      const url = this.escapeHtml(item.url || '#')
      const icon = this.escapeHtml(item.icon || 'open_in_new')
      const external = url.startsWith('http')
      return `
        <a class="link-btn" href="${url}" ${external ? 'target="_blank" rel="noopener noreferrer"' : ''}>
          <span class="material-icons">${icon}</span>
          ${label}
        </a>
      `
    }).join('')
  }

  initTabs() {
    document.querySelectorAll('.tab').forEach((tab) => {
      const href = tab.getAttribute('href') || ''
      const isPublishTab = href.includes('view=publish') || href === '/publish'
      const isActive = (this.isPublish && isPublishTab) || (!this.isPublish && href === '/')
      tab.classList.toggle('active', isActive)
    })
  }

  initView() {
    document.getElementById('feed-view').classList.toggle('hidden', this.isPublish)
    document.getElementById('publish-view').classList.toggle('hidden', !this.isPublish)
  }

  initAuthStoragePairs(pairs) {
    pairs.forEach(({ inputId, key }) => {
      const input = document.getElementById(inputId)
      if (!input) return
      input.value = localStorage.getItem(key) || ''
      input.addEventListener('change', () => {
        localStorage.setItem(key, input.value.trim())
      })
    })
  }

  readAuth(clientTokenId, serverPasswordId) {
    const token = (document.getElementById(clientTokenId)?.value || '').trim()
    const password = (document.getElementById(serverPasswordId)?.value || '').trim()
    if (!token || !password) {
      throw new Error('请先填写 client token 与 server password')
    }

    localStorage.setItem('life_client_token', token)
    localStorage.setItem('life_server_password', password)

    return {
      'content-type': 'application/json',
      'x-client-token': token,
      'x-server-password': password
    }
  }

  async loadFeed() {
    try {
      const res = await fetch('/api/v1/public/feed')
      const data = await res.json()
      if (!data.success) throw new Error(data.message || 'Failed to load feed')
      this.feed = data
      this.renderFeed(data)
    } catch (err) {
      this.showMessage(err.message || '加载失败', true)
    }
  }

  renderFeed(data) {
    const displayName = this.config.ownerName || 'Life App User'
    const pageMotto = this.config.motto || '实时状态与帖子流'
    const profileMotto = this.config.ownerMotto || pageMotto

    const template = this.config.headlineTemplate || '看看 {name} 在干什么 O.O'
    document.getElementById('headline').textContent = this.applyTemplate(template, {
      name: displayName,
      motto: pageMotto
    })
    document.getElementById('motto').textContent = pageMotto
    const profileNameTemplate = this.config.profileNameTemplate || '{name}'
    const profileMottoTemplate = this.config.profileMottoTemplate || '{motto}'
    document.getElementById('profile-name').textContent = this.applyTemplate(profileNameTemplate, {
      name: displayName,
      motto: profileMotto
    })
    document.getElementById('profile-motto').textContent = this.applyTemplate(profileMottoTemplate, {
      name: displayName,
      motto: profileMotto
    })

    const avatar = document.querySelector('.avatar')
    if (avatar) {
      const firstChar = displayName.trim().charAt(0) || 'L'
      avatar.textContent = firstChar.toUpperCase()
    }

    const primary = data.status?.primary || { status: 'Offline', source: 'system' }
    const primaryText = primary.status || 'Offline'
    document.getElementById('primary-status-text').textContent = primaryText
    document.getElementById('primary-status-meta').textContent = `source: ${primary.source || 'system'}`
    document.getElementById('last-updated').textContent = primary.observed_at ? `updated ${this.formatDateTime(primary.observed_at)}` : 'no active status'

    document.getElementById('stat-primary').textContent = primaryText

    const sourceList = document.getElementById('source-status-list')
    const sources = data.status?.sources || []
    document.getElementById('stat-source-count').textContent = String(sources.length)

    if (sources.length === 0) {
      sourceList.innerHTML = '<div class="empty">暂无有效来源状态</div>'
    } else {
      sourceList.innerHTML = sources.map((s) => {
        const expires = s.expires_at ? this.formatRelative(s.expires_at) : '-'
        const sourceClass = s.source === 'manual' ? 'manual' : (s.source === 'custom' ? 'custom' : 'other')
        return `
          <article class="source-item ${sourceClass}">
            <div class="source-head">${this.escapeHtml(s.source)} · ${this.escapeHtml(s.status)}</div>
            <p class="muted">observed ${this.formatDateTime(s.observed_at)}</p>
            <p class="muted">expires in ${expires}</p>
          </article>
        `
      }).join('')
    }

    const posts = data.posts || []
    document.getElementById('stat-post-count').textContent = String(posts.length)

    const postsList = document.getElementById('posts-list')
    if (posts.length === 0) {
      postsList.innerHTML = '<div class="empty">暂无帖子</div>'
    } else {
      postsList.innerHTML = posts.map((p) => this.renderPostCard(p)).join('')
    }
  }

  initManage() {
    this.initAuthStoragePairs([
      { inputId: 'auth-client-token', key: 'life_client_token' },
      { inputId: 'auth-server-password', key: 'life_server_password' }
    ])

    document.getElementById('publish-status').addEventListener('click', async () => {
      try {
        const ttl = Number(document.getElementById('status-ttl').value || '15')
        const metaRaw = document.getElementById('status-meta').value.trim()
        await this.publishStatus({
          authClientId: 'auth-client-token',
          authPasswordId: 'auth-server-password',
          statusId: 'status-text',
          ttlMinutes: ttl,
          metaRaw
        })
        this.showMessage('状态已发布')
      } catch (err) {
        this.showMessage(err.message || '状态发布失败', true)
      }
    })

    document.getElementById('publish-post').addEventListener('click', async () => {
      try {
        await this.publishPost({
          authClientId: 'auth-client-token',
          authPasswordId: 'auth-server-password',
          contentId: 'post-content',
          tagsId: 'post-tags',
          locationId: 'post-location'
        })
        document.getElementById('post-content').value = ''
        document.getElementById('post-tags').value = ''
        document.getElementById('post-location').value = ''
        await this.loadMyPosts()
        this.showMessage('帖子已发布')
      } catch (err) {
        this.showMessage(err.message || '帖子发布失败', true)
      }
    })

    document.getElementById('refresh-posts').addEventListener('click', async () => {
      try {
        await this.loadMyPosts()
      } catch (err) {
        this.showMessage(err.message || '加载失败', true)
      }
    })

    this.loadMyPosts().catch((err) => this.showMessage(err.message || '加载失败', true))
  }

  async publishStatus({ authClientId, authPasswordId, statusId, ttlMinutes, metaRaw }) {
    const headers = this.readAuth(authClientId, authPasswordId)
    const status = (document.getElementById(statusId)?.value || '').trim()
    if (!status) throw new Error('状态内容不能为空')

    let meta = undefined
    if (metaRaw) {
      try {
        meta = JSON.parse(metaRaw)
      } catch {
        throw new Error('Meta 必须是合法 JSON')
      }
    }

    const observedAt = Date.now()
    const expiresAt = observedAt + Math.max(1, Number(ttlMinutes) || 15) * 60 * 1000

    const res = await fetch('/api/v1/status/events', {
      method: 'POST',
      headers,
      body: JSON.stringify({
        source: 'manual',
        status,
        observed_at: observedAt,
        expires_at: expiresAt,
        meta
      })
    })

    const data = await res.json()
    if (!res.ok || !data.success) throw new Error(data.message || '请求失败')
  }

  async publishPost({ authClientId, authPasswordId, contentId, tagsId, locationId }) {
    const headers = this.readAuth(authClientId, authPasswordId)
    const content = (document.getElementById(contentId)?.value || '').trim()
    const tags = (document.getElementById(tagsId)?.value || '').trim()
    const location = (document.getElementById(locationId)?.value || '').trim()

    if (!content) throw new Error('帖子内容不能为空')

    const res = await fetch('/api/v1/posts', {
      method: 'POST',
      headers,
      body: JSON.stringify({ content, tags: tags || null, location: location || null })
    })

    const data = await res.json()
    if (!res.ok || !data.success) throw new Error(data.message || '请求失败')
  }

  async loadMyPosts() {
    const headers = this.readAuth('auth-client-token', 'auth-server-password')
    const res = await fetch('/api/v1/posts', { headers })
    const data = await res.json()
    if (!res.ok || !data.success) throw new Error(data.message || '请求失败')

    const container = document.getElementById('manage-posts-list')
    const posts = data.posts || []
    if (posts.length === 0) {
      container.innerHTML = '<div class="empty">暂无帖子</div>'
      return
    }

    container.innerHTML = posts.map((p) => `
      <article class="post-card">
        <div class="post-main">
          <p>${this.escapeHtml(p.content)}</p>
          <div class="post-meta">
            <span>${this.formatDateTime(p.created_at)}</span>
            ${p.tags ? `<span>#${this.escapeHtml(p.tags)}</span>` : ''}
            ${p.location ? `<span>@${this.escapeHtml(p.location)}</span>` : ''}
          </div>
        </div>
        <div class="post-actions">
          <button class="btn ghost" data-action="edit" data-id="${p.id}">编辑</button>
          <button class="btn danger" data-action="delete" data-id="${p.id}">删除</button>
        </div>
      </article>
    `).join('')

    container.querySelectorAll('button[data-action="edit"]').forEach((button) => {
      button.addEventListener('click', async () => {
        const id = button.dataset.id
        const oldPost = posts.find((item) => item.id === id)
        if (!oldPost) return

        const nextContent = window.prompt('编辑内容', oldPost.content)
        if (nextContent === null) return

        const nextTags = window.prompt('编辑标签(逗号分隔)', oldPost.tags || '')
        if (nextTags === null) return

        const nextLocation = window.prompt('编辑地点', oldPost.location || '')
        if (nextLocation === null) return

        try {
          await this.updatePost(id, nextContent, nextTags, nextLocation)
          await this.loadMyPosts()
          this.showMessage('帖子已更新')
        } catch (err) {
          this.showMessage(err.message || '更新失败', true)
        }
      })
    })

    container.querySelectorAll('button[data-action="delete"]').forEach((button) => {
      button.addEventListener('click', async () => {
        const id = button.dataset.id
        if (!window.confirm('确认删除这条帖子？')) return
        try {
          await this.deletePost(id)
          await this.loadMyPosts()
          this.showMessage('帖子已删除')
        } catch (err) {
          this.showMessage(err.message || '删除失败', true)
        }
      })
    })
  }

  async updatePost(id, content, tags, location) {
    const headers = this.readAuth('auth-client-token', 'auth-server-password')
    const payload = {
      content: content.trim(),
      tags: tags.trim() || null,
      location: location.trim() || null
    }
    if (!payload.content) throw new Error('帖子内容不能为空')

    const res = await fetch(`/api/v1/posts/${encodeURIComponent(id)}`, {
      method: 'PUT',
      headers,
      body: JSON.stringify(payload)
    })

    const data = await res.json()
    if (!res.ok || !data.success) throw new Error(data.message || '请求失败')
  }

  async deletePost(id) {
    const headers = this.readAuth('auth-client-token', 'auth-server-password')
    const res = await fetch(`/api/v1/posts/${encodeURIComponent(id)}`, {
      method: 'DELETE',
      headers
    })

    const data = await res.json()
    if (!res.ok || !data.success) throw new Error(data.message || '请求失败')
  }

  renderPostCard(post) {
    return `
      <article class="post-card">
        <p>${this.escapeHtml(post.content)}</p>
        <div class="post-meta">
          <span>${this.formatDateTime(post.created_at)}</span>
          ${post.tags ? `<span>#${this.escapeHtml(post.tags)}</span>` : ''}
          ${post.location ? `<span>@${this.escapeHtml(post.location)}</span>` : ''}
        </div>
      </article>
    `
  }

  formatDateTime(timestamp) {
    if (!timestamp) return '-'
    return new Date(timestamp).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  formatRelative(timestamp) {
    const diffMs = timestamp - Date.now()
    const diffMin = Math.round(diffMs / 60000)
    if (diffMin <= 0) return 'soon'
    return `${diffMin}m`
  }

  showMessage(text, isError = false) {
    const message = document.getElementById('message')
    message.textContent = text
    message.classList.remove('hidden')
    message.classList.toggle('error', isError)
    clearTimeout(this.messageTimer)
    this.messageTimer = setTimeout(() => {
      message.classList.add('hidden')
    }, 2500)
  }

  escapeHtml(text) {
    const div = document.createElement('div')
    div.textContent = text || ''
    return div.innerHTML
  }

  applyTemplate(template, vars) {
    let result = String(template || '')
    Object.entries(vars || {}).forEach(([key, value]) => {
      result = result.split(`{${key}}`).join(String(value ?? ''))
    })
    return result
  }
}

document.addEventListener('DOMContentLoaded', () => {
  window.app = new LifeAppV11()
})
