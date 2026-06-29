# 🌠 流星树洞 · 织星海

> 匿名情感树洞 + 治愈系星空交互。把心里话写成流星，让它漂进星海。

![React](https://img.shields.io/badge/React-18-61dafb)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-6db33f)
![MySQL](https://img.shields.io/badge/MySQL-8.4-00758f)
![Vite](https://img.shields.io/badge/Vite-6-646cff)
![Android](https://img.shields.io/badge/Android-Kotlin-7f52ff)
![Canvas](https://img.shields.io/badge/Canvas-2D-f7921e)

## ✨ 项目特性

- 🌌 **星空粒子背景** — Canvas 2D 粒子系统 + requestAnimationFrame，纯前端模拟地球视角星空（800 颗随机星星 + 12 颗一等星 + 星座连线 + 流星效果）
- ☄️ **漂流流星** — 写下心事化作流星，漂浮在星海中等别人捞起
- 🎭 **匿名树洞** — 无需真实身份，安全表达内心
- 🗺️ **专属星图** — 基于内容 SHA-256 确定性生成，同一段话永远对应同一片星空
- ✨ **星图治愈特效** — 浮动星尘、星光爆发、治愈语句轮播、呼吸光晕
- 🎵 **背景音乐** — 右上角旋转唱片图标，点击切换播放 / 暂停
- 📡 **实时推送** — WebSocket 广播，新流星 / 捞起事件即时通知
- 🔐 **JWT 单设备登录** — 同一账号仅允许一台设备在线，新登录挤掉旧设备
- 🛡️ **图形验证码** — 注册 / 登录双重人机验证，防止恶意刷号
- 🔎 **内容审核** — 流星 + 回复双重 AI 审核，管理员可审核/删除/管理用户
- 📜 **用户协议 & 隐私政策** — 登录注册前必须同意，个人页可随时查看
- 💖 **星光守护者** — 赞助体系，专属星图边框与荣誉展示
- 🤖 **AI 回信** — DeepSeek AI 动态生成治愈回复，离线兜底文案

## 📁 项目结构

```
StarWeave/
├── sql/                        # 数据库建表脚本
│   └── init.sql
├── backend/                    # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/starweave/
│       │   ├── config/         # CORS, WebSocket, JWT, Security 配置
│       │   ├── entity/         # 数据实体
│       │   ├── mapper/         # MyBatis Mapper 接口
│       │   ├── service/        # 业务逻辑
│       │   ├── controller/     # REST API
│       │   ├── dto/            # 数据传输对象（LoginResult 等）
│       │   ├── handler/        # 全局异常处理
│       │   └── websocket/      # WebSocket 推送
│       └── resources/
│           ├── application.yml
│           └── mapper/         # MyBatis XML 映射
├── android/                    # Android 原生客户端（Kotlin + Jetpack）
│   ├── app/
│   │   └── src/main/java/com/starweave/android/
│   │       ├── api/            # Retrofit API 客户端
│   │       ├── model/          # 数据模型
│   │       ├── navigation/     # Navigation 路由
│   │       ├── service/        # 后台服务（音乐播放）
│   │       ├── ui/
│   │       │   ├── components/ # UI 组件
│   │       │   ├── screen/     # 页面（登录/注册/捞流星/星图/管理…）
│   │       │   └── starmap/    # 星图 Canvas 绘制
│   │       ├── util/           # 工具类
│   │       └── viewmodel/      # ViewModel
│   ├── build.gradle.kts
│   └── settings.gradle.kts
├── frontend/                   # React 18 + Vite 前端
│   ├── vite.config.js
│   ├── public/                 # 静态资源（背景音乐等）
│   └── src/
│       ├── components/         # AuthGate, MeteorCard, StarMapPage, MusicPlayer 等
│       ├── api/                # Axios API 客户端
│       └── styles/             # 全局 CSS（暗色星空主题）
└── README.md
```

## 🚀 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.4+
- Android Studio (可选，构建 Android 客户端)

### 数据库

```bash
mysql -u root -p < sql/init.sql
```

编辑 `backend/src/main/resources/application.yml` 中的数据库连接信息。

### 后端启动

```bash
cd backend
./mvnw spring-boot:run
```

后端默认运行在 `http://localhost:8080`。

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`。

### Android 客户端

使用 Android Studio 打开 `android/` 目录，同步 Gradle 后即可运行。后端地址在 `ApiClient.kt` 中配置。

## 📡 API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/user/register` | 账号注册 |
| `POST` | `/api/user/login/password` | 账号密码登录 |
| `GET` | `/api/captcha` | 获取图形验证码 |
| `GET` | `/api/user/{id}` | 用户信息 |
| `PUT` | `/api/user/{id}` | 更新用户资料 |
| `POST` | `/api/user/{id}/password` | 修改密码 |
| `POST` | `/api/user/{id}/avatar` | 设置头像 |
| `POST` | `/api/user/{id}/avatar/upload` | 上传头像文件 |
| `GET` | `/api/user/{id}/stats` | 用户统计数据 |
| `GET` | `/api/message/floating` | 漂流中的流星列表 |
| `POST` | `/api/message/publish` | 发布流星（自动 AI 审核） |
| `POST` | `/api/message/catch` | 捞起流星 |
| `GET` | `/api/message/user/{uid}` | 用户发布的流星 |
| `GET` | `/api/message/caught/{uid}` | 用户捞起的流星 |
| `GET` | `/api/star-map/{id}` | 星图详情 |
| `POST` | `/api/star-map/unlock` | 解锁高清星图 |
| `GET` | `/api/sponsor/guardians` | 星光守护者列表 |
| `GET` | `/api/sponsor/count` | 守护者数量 |
| `GET` | `/api/admin/pending` | 待审核流星列表 |
| `POST` | `/api/admin/review/{id}` | 审核流星 |
| `DELETE` | `/api/admin/meteors/{id}` | 删除流星 |
| `GET` | `/api/admin/wishes/pending` | 待审核回复列表 |
| `GET` | `/api/admin/wishes` | 全部回复 |
| `POST` | `/api/admin/wishes/{id}/review` | 审核回复 |
| `DELETE` | `/api/admin/wishes/{id}` | 删除回复 |
| `GET` | `/api/admin/users` | 用户列表 |
| `DELETE` | `/api/admin/users/{id}` | 删除用户及所有数据 |
| `GET` | `/api/admin/stats` | 管理后台统计 |
| `GET` | `/api/admin/messages` | 全部流星列表 |
| `WS` | `/ws/meteor` | 实时流星推送 |

### 认证方式

所有需要登录的请求在 HTTP Header 中附加：

```
Authorization: Bearer <JWT_TOKEN>
```

登录/注册成功后返回 `LoginResult`，包含 `token`（JWT）、`user`（用户信息）。

### 单设备登录

系统使用 JWT `tokenVersion` 机制实现单设备登录。同一账号在新设备登录后，旧设备的所有 token 自动失效，旧设备的下一次请求会收到 `401` 及提示"账号已在其他设备登录，请重新登录"。

## 🚢 部署

### Nginx 配置

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态资源
    location / {
        root /var/www/starweave/frontend/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 代理
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # WebSocket 代理
    location /ws/ {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_read_timeout 86400;
    }
}
```

### 部署步骤

```bash
# 1. 构建前端
cd frontend && npm run build

# 2. 复制构建产物到服务器
scp -r frontend/dist/* user@server:/var/www/starweave/frontend/dist/

# 3. 启动后端
cd backend && nohup java -jar target/starweave-*.jar > app.log 2>&1 &

# 4. 重载 Nginx
sudo nginx -s reload
```

## 🎨 视觉特效

- **星空背景**：纯 Canvas 2D 模拟地球视角星空（800 颗随机星星 + 12 颗一等星 + 星座连线 + 流星效果），无外部数据依赖
- **登录动画**：4 段重叠（卡片解构粒子 → 流星失重 → 文字告白 → 水波纹展开）
- **退出动画**：4 段重叠（星空冻结 → 逆向粒子汇聚 → 告别文字 → 闪光重组）
- **星图生成**：基于文本 hash 的确定性伪随机，同一段话永远生成同一片星空
- **星图治愈特效**：浮动星尘粒子 + 生成时星光爆发 + 治愈语句轮播 + Canvas 呼吸光晕
- **背景音乐**：右上角圆形唱片图标，播放时旋转，点击暂停

## 💰 商业模式

> 树洞倾诉永远免费，收钱的是从树洞里长出来的东西。

| 阶段 | 方式 | 说明 |
|------|------|------|
| 阶段一 | 爱发电 / 面包多 | 个人即可，验证需求 |
| 阶段二 | 微信支付商户 | 注册个体户，长期运营 |
| 阶段三 | 小程序 / PWA | 接入 AI 回信等增值服务 |

### 付费点

- 🗺️ **星图下载** — 免费预览 + 水印低清 → 付费高清 PNG
- 💌 **AI 回信**（规划中）— 付费获取更长的 AI 安慰回信
- ✨ **仪式感商品** — 发光流星皮肤 / 虚拟信纸

## 📄 合规

- ✅ 用户协议 + 隐私政策
- ✅ 内容审核机制（关键词过滤 + AI 审核 + 举报 + 删除）
- ✅ 未成年人保护

## 📝 开发日志

- `2026-06-05 10:08:41` — 完整项目初始化（Spring Boot 后端 + React 前端 + 登录/退出动画 + 星图生成 + WebSocket + 用户协议 + 星图治愈特效 + 背景音乐 + 回复审核 + 赞助体系）
- `2026-06-05 13:25:09` — 星空背景改为纯 Canvas 2D 模拟（移除 Three.js 依赖）+ Nginx 部署配置
- `2026-06-05 17:45:12` — 修复移动端浏览器底部工具栏遮挡页面内容问题（`100vh` → JS 动态 `--vh` CSS 变量）
- `2026-06-06 19:13:21` — 前端 UI/UX 全面优化（设计令牌、骨架屏、自定义弹窗、错误边界、无障碍、登录态持久化）
- `2026-06-06 19:48:28` — 图形验证码系统 + Android 原生客户端项目初始化
- `2026-06-06 21:30:00` — 修复 iOS Safari 输入框英文重复输入 bug + 爱发电账号绑定后端
- `2026-06-28 12:00:00` — JWT 单设备登录系统（tokenVersion 机制）+ SecurityConfig + 后台日志集中化
- `2026-06-28 14:00:00` — DeepSeek AI 动态生成回信 / 治愈文案 + Android 多页面 UI/UX 重构
- `2026-06-29 10:00:00` — Android 增量加载（IncrementalList）+ 阿里云实名认证基础设施

---

<p align="center">
  <sub>⭐ 如果这个项目让你感到温暖，请给个 Star</sub>
</p>
