# 🌠 流星树洞 · 织星海

> 匿名情感树洞 + 治愈系星空交互。把心里话写成流星，让它漂进星海。

![Vue 3](https://img.shields.io/badge/Vue-3-42b883)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-6db33f)
![MySQL](https://img.shields.io/badge/MySQL-8.4-00758f)
![Vite](https://img.shields.io/badge/Vite-5-646cff)

## ✨ 项目特性

- 🌌 **星空粒子背景** — Canvas 粒子系统 + requestAnimationFrame，沉浸式星空体验
- ☄️ **漂流流星** — 写下心事化作流星，漂浮在星海中等别人捞起
- 🎭 **匿名树洞** — 无需真实身份，安全表达内心
- 🗺️ **专属星图** — 基于内容 SHA-256 确定性生成，同一段话永远对应同一片星空
- 📡 **实时推送** — WebSocket 广播，新流星 / 捞起事件即时通知
- 🛡️ **内容审核** — 关键词过滤 + 可扩展 AI 审核
- 💖 **星光守护者** — 赞助体系，专属星图边框与荣誉展示

## 📁 项目结构

```
StarWeave/
├── sql/                        # 数据库建表脚本
│   └── 000-init-schema.sql
├── backend/                    # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/starweave/
│       │   ├── config/         # CORS, WebSocket 配置
│       │   ├── entity/         # 数据实体
│       │   ├── mapper/         # MyBatis Mapper 接口
│       │   ├── service/        # 业务逻辑
│       │   ├── controller/     # REST API
│       │   ├── dto/            # 数据传输对象
│       │   ├── handler/        # 全局异常处理
│       │   └── websocket/      # WebSocket 推送
│       └── resources/
│           ├── application.yml
│           └── mapper/         # MyBatis XML 映射
├── frontend/                   # Vue 3 + Vite 前端
│   ├── vite.config.js
│   └── src/
│       ├── components/         # StarrySky, MeteorCard, SponsorSection
│       ├── views/              # Login, Home, Write, Profile, StarMap
│       ├── router/             # Vue Router
│       ├── api/                # Axios API 客户端
│       └── styles/             # 全局 CSS（暗色星空主题）
└── README.md
```

## 🚀 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.4+

### 后端启动

```bash
# 1. 创建数据库并执行建表脚本
mysql -u root -p < sql/000-init-schema.sql

# 2. 修改数据库配置
cd backend
# 编辑 src/main/resources/application.yml 中的数据库连接信息

# 3. 启动后端
./mvnw spring-boot:run
```

后端默认运行在 `http://localhost:8080`

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`

## 📡 API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/user/login` | 匿名登录/注册 |
| `GET` | `/api/user/{id}` | 用户信息 |
| `GET` | `/api/message/floating` | 漂流中的流星列表 |
| `POST` | `/api/message/publish` | 发布流星（自动审核） |
| `POST` | `/api/message/catch` | 捞起流星 |
| `GET` | `/api/message/user/{uid}` | 用户发布的流星 |
| `GET` | `/api/message/caught/{uid}` | 用户捞起的流星 |
| `GET` | `/api/star-map/{id}` | 星图详情 |
| `POST` | `/api/star-map/unlock` | 解锁高清星图 |
| `GET` | `/api/sponsor/guardians` | 星光守护者列表 |
| `GET` | `/api/sponsor/count` | 守护者数量 |
| `WS` | `/ws/meteor` | 实时流星推送 |

## 🎨 视觉特效

- **登录动画**：4 段重叠（卡片解构粒子 → 流星失重 → 文字告白 → 水波纹展开）
- **退出动画**：4 段重叠（星空冻结 → 逆向粒子汇聚 → 告别文字 → 闪光重组）
- **星图生成**：基于文本 hash 的确定性伪随机，同一段话永远生成同一片星空

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
- ✅ 内容审核机制（关键词过滤 + 举报 + 删除）
- ✅ 未成年人保护

## 📝 开发日志

- 2025-06：完整后端（Spring Boot + MyBatis + MySQL）搭建完成
- 2025-06：Vue 3 + Vite 前端重构完成，含星空 Canvas 粒子背景
- 2025-06：登录 / 退出动画系统实现
- 2025-06：星图确定性生成算法实现
- 2025-06：WebSocket 实时推送接入

---

<p align="center">
  <sub>⭐ 如果这个项目让你感到温暖，请给个 Star</sub>
</p>
