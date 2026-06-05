# 流星树洞 · 织星海

## 项目定位
匿名情感树洞 + 治愈系星空交互。核心是情绪价值，不做社交、不做匹配。

## 商业模式
### 核心原则
树洞倾诉永远免费，收钱的是从树洞里长出来的东西——星图、回信、仪式感。

### 推荐路径
1. **阶段一（零资质）**：爱发电 / 面包多接「赞助星海」，赞助用户获得专属星图边框 / 星光守护者列表展示
2. **阶段二（验证后）**：注册个体户 → 微信支付商户 → 在"我的"页面内嵌购买星图 / 回信包
3. **阶段三（稳定后）**：小程序 / PWA 分发，接入 AI 回信等增值服务

### 付费点
- **星图下载**：免费预览 + 水印低清 → 付费 3~6 元得高清 PNG 无品牌水印。法律定性为数字内容销售
- **AI 回信**（未来）：用户对捞到的流星付费获取更长的 AI 安慰回信
- **仪式感商品**：发光流星皮肤 / 虚拟信纸（极低价，1~3 元）

### 支付方式
| 方式 | 资质 | 场景 |
|------|------|------|
| 爱发电 / 面包多 | 个人即可 | 阶段一验证 |
| 微信支付商户 | 个体户 / 公司 | 阶段二长期 |
| LemonSqueezy / Stripe | 无需国内资质 | 海外华人市场 |

### 合规三件套
- 用户协议 + 隐私政策（说明数据收集、付费规则）
- 内容审核机制（匿名 UGC 必须有过滤 + 举报 + 删除）
- 未成年人保护（限制夜间使用 / 敏感内容过滤 / 家长联系方式）

## 技术栈
- 前端：React 18 + Vite + Canvas 2D 粒子系统 + WebSocket（迁移自原型单文件 HTML）
- 后端：Spring Boot 3.4 + MyBatis + MySQL 8.4
- 特效：Canvas 2D 粒子系统 + requestAnimationFrame（纯前端模拟星空，无外部数据依赖）
- 音频：HTML5 Audio（背景音乐循环播放）
- 实时推送：WebSocket
- 审核：关键词过滤（可替换为 LLM API）
- 部署：Nginx 反向代理（前端静态资源 + 后端 API）

## 项目结构
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
├── frontend/                   # React 18 + Vite 前端
│   ├── vite.config.js
│   ├── public/                 # 静态资源（背景音乐等）
│   └── src/
│       ├── components/         # AuthGate, MeteorCard, StarMapPage, StarMapCanvas, MusicPlayer, LegalPage 等
│       ├── api/                # Axios API 客户端
│       └── styles/             # 全局 CSS（暗色星空主题）
└── CLAUDE.md
```

## 数据库表
- **user** — 用户（匿名昵称、密码、边框样式、赞助状态、协议同意状态）
- **message** — 流星消息（内容、颜色、审核状态、捞取状态）
- **star_map** — 星图（基于内容 SHA-256 的确定性生成）
- **sponsor** — 赞助记录 / 星光守护者
- **ai_review_log** — AI 审核日志

## API 接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/user/login | 匿名登录/注册 |
| POST | /api/user/register | 账号注册 |
| POST | /api/user/login-password | 账号密码登录 |
| GET | /api/user/{id} | 用户信息 |
| GET | /api/message/floating | 漂流中的流星列表 |
| POST | /api/message/publish | 发布流星（自动 AI 审核） |
| POST | /api/message/catch | 捞起流星 |
| GET | /api/message/user/{uid} | 用户发布的流星 |
| GET | /api/message/caught/{uid} | 用户捞起的流星 |
| GET | /api/star-map/{id} | 星图详情 |
| POST | /api/star-map/unlock | 解锁高清星图 |
| GET | /api/sponsor/guardians | 星光守护者列表 |
| GET | /api/sponsor/count | 守护者数量 |
| GET | /api/admin/pending | 待审核流星列表 |
| POST | /api/admin/review/{id} | 审核流星 |
| DELETE | /api/admin/meteors/{id} | 删除流星 |
| GET | /api/admin/wishes/pending | 待审核回复列表 |
| GET | /api/admin/wishes | 全部回复 |
| POST | /api/admin/wishes/{id}/review | 审核回复 |
| DELETE | /api/admin/wishes/{id} | 删除回复 |
| GET | /api/admin/users | 用户列表 |
| DELETE | /api/admin/users/{id} | 删除用户及所有数据 |
| WS | /ws/meteor | 实时流星推送 |

## 部署

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

## 开发记录
- 登录动画：4 段重叠（卡片解构粒子 → 流星失重 → 文字告白 → 水波纹展开）
- 退出动画：4 段重叠（星空冻结 → 逆向粒子汇聚 → 告别文字 → 闪光重组）
- 星图生成：基于文本 hash 的确定性伪随机 → 同一段话永远生成同一片星空
- 星图治愈特效：浮动星尘粒子 + 星光爆发 + 治愈语句轮播 + Canvas 呼吸光晕
- 背景音乐：右上角圆形唱片图标，播放旋转，点击暂停，自动循环
- 阶段一赞助：个人页添加「赞助星海」入口（爱发电跳转、星图边框预览、星光守护者列表）
- 用户协议 & 隐私政策：全屏覆盖页面，登录注册强制同意，个人页可查看
- 2025-06：完整后端（Spring Boot + MyBatis + MySQL）搭建完成
- 2025-06：React 18 + Vite 前端重构完成，含星空 Canvas 粒子背景
- 审核系统：关键词 + 规则过滤，接口可替换为 LLM API
- 实时推送：WebSocket 广播新流星 / 捞起事件
- 回复审核：回复（Wish）发布时自动 AI 审核，管理员可审核/删除/管理用户
- 用户可删除自己的回复，管理员可删除任意流星/回复/用户
- 用户协议 & 隐私政策更新：新增内容管理与审核章节，明确回复删除权限
- 星空背景：纯 Canvas 2D 模拟地球视角星空（800 颗随机星星 + 12 颗一等星 + 星座连线 + 流星效果），移除 Three.js 依赖
- 2025-06：Nginx 部署配置完成
