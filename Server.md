# Life App 服务端部署指南

## 概述

Life App 服务端是一个基于 Node.js + Express 的轻量级同步服务器，提供任务数据同步和 Material Design 3 风格的 Web 仪表盘。

## 特性

- 🔐 基于密码的简单认证机制（无需用户名/密码登录）
- 📱 客户端自动生成唯一 Token
- 📊 Material Design 3 风格的 Web 仪表盘
- 🗄️ 使用 NeDB 嵌入式数据库（无需外部数据库）
- 🌐 支持中英文界面

## 系统要求

- Node.js 18.0.0 或更高版本
- npm 或 yarn 包管理器

## 快速开始

### 1. 进入服务端目录

```bash
cd Server
```

### 2. 安装依赖

```bash
npm install
```

### 3. 配置环境变量

复制示例配置文件并修改：

```bash
cp .env.example .env
```

编辑 `.env` 文件，设置您的服务器密码：

```env
# 服务器端口
PORT=3000

# 服务器密码（必须设置，用于客户端认证）
# 请使用强密码！
SERVER_PASSWORD=your_secure_password_here

# 数据存储目录
DATA_DIR=./data

# CORS 配置（允许的来源，* 表示允许所有）
CORS_ORIGINS=*

# 运行环境
NODE_ENV=production
```

### 4. 启动服务器

开发模式（支持热重载）：
```bash
npm run dev
```

生产模式：
```bash
npm start
```

### 5. 访问服务

- Web 仪表盘: `http://localhost:3000`
- API 端点: `http://localhost:3000/api/v1`

## 客户端配置

在 Life App 应用的设置页面：

1. **服务器地址**: 输入您的服务器地址和端口（例如：`192.168.1.100:3000` 或 `your-domain.com:3000`）
2. **服务器密码**: 输入您在 `.env` 中设置的 `SERVER_PASSWORD`
3. 客户端会自动生成唯一的 Client Token

## API 文档

### 认证方式

所有需要认证的请求必须包含以下 HTTP 头：

```
X-Client-Token: <客户端自动生成的Token>
X-Server-Password: <服务器密码>
```

### 端点列表

#### 健康检查
```
GET /api/v1/health
```

无需认证，返回服务器状态。

#### 同步任务
```
POST /api/v1/sync

Headers:
  X-Client-Token: <token>
  X-Server-Password: <password>

Body:
{
  "tasks": [TaskDto],
  "last_sync": 1234567890,  // 可选，上次同步时间戳
  "profile": {              // 可选，用户资料
    "displayName": "用户名",
    "motto": "个人格言",
    "status": "Available"
  }
}

Response:
{
  "success": true,
  "message": "Sync completed",
  "server_time": 1234567890,
  "updated_tasks": [TaskDto]
}
```

#### 获取任务列表
```
GET /api/v1/tasks

Headers:
  X-Client-Token: <token>
  X-Server-Password: <password>
```

#### 获取/更新个人资料
```
GET /api/v1/profile
PUT /api/v1/profile

Headers:
  X-Client-Token: <token>
  X-Server-Password: <password>
```

#### 获取时间轴数据
```
GET /api/v1/timeline

Headers:
  X-Client-Token: <token>
  X-Server-Password: <password>
```

#### 获取公开仪表盘数据（无需认证）
```
GET /api/v1/public/dashboard
```

返回所有公开任务和用户状态。

## 数据结构

### TaskDto

```json
{
  "id": "uuid",
  "title": "任务标题",
  "description": "任务描述",
  "created_at": 1234567890,
  "start_time": 1234567890,
  "deadline": 1234567890,
  "is_completed": false,
  "completed_at": null,
  "progress": 0.5,
  "priority": 2,
  "is_public": true,
  "tags": "标签1,标签2"
}
```

### Priority 优先级

- `1`: 低
- `2`: 中
- `3`: 高

## 生产部署

### 使用 PM2

```bash
# 安装 PM2
npm install -g pm2

# 启动服务
pm2 start src/index.js --name life-app-server

# 查看状态
pm2 status

# 查看日志
pm2 logs life-app-server

# 设置开机自启
pm2 startup
pm2 save
```

### 使用 Docker

创建 `Dockerfile`:

```dockerfile
FROM node:18-alpine

WORKDIR /app

COPY package*.json ./
RUN npm ci --only=production

COPY . .

EXPOSE 3000

CMD ["npm", "start"]
```

构建并运行：

```bash
docker build -t life-app-server .
docker run -d -p 3000:3000 \
  -e SERVER_PASSWORD=your_password \
  -v $(pwd)/data:/app/data \
  life-app-server
```

### 使用 Nginx 反向代理

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://127.0.0.1:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }
}
```

### HTTPS 配置

建议使用 Let's Encrypt 免费 SSL 证书：

```bash
# 安装 certbot
apt install certbot python3-certbot-nginx

# 获取证书
certbot --nginx -d your-domain.com
```

## 数据备份

数据存储在 `data/` 目录下的 NeDB 文件中：

- `tasks.db` - 任务数据
- `clients.db` - 客户端信息
- `profiles.db` - 用户资料

定期备份此目录即可：

```bash
# 备份
tar -czvf backup-$(date +%Y%m%d).tar.gz data/

# 恢复
tar -xzvf backup-20231201.tar.gz
```

## 安全建议

1. **使用强密码**: `SERVER_PASSWORD` 应使用至少 16 位的随机字符串
2. **启用 HTTPS**: 生产环境务必使用 HTTPS
3. **限制 CORS**: 在 `.env` 中设置具体的允许域名而非 `*`
4. **防火墙**: 只开放必要的端口
5. **定期更新**: 保持 Node.js 和依赖包更新

## 故障排除

### 服务器无法启动

检查端口是否被占用：
```bash
lsof -i :3000
```

### 客户端无法连接

1. 检查服务器地址和端口是否正确
2. 检查服务器密码是否匹配
3. 检查防火墙是否开放端口

### 数据同步失败

1. 检查网络连接
2. 查看服务器日志：`npm start` 会输出请求日志
3. 确认客户端 Token 和密码正确

## 更新日志

### v1.0.0
- 初始版本
- 基于密码的认证机制
- Material Design 3 Web 仪表盘
- 任务同步 API
- 时间轴视图
- 标签过滤功能

## 许可证

MIT License
