# 流星树洞 · 织星海 — 前端

匿名情感树洞 + 治愈系星空交互应用。

## 技术栈

- React 18 + Vite
- Canvas 2D 粒子系统（纯前端星空模拟）
- WebSocket 实时推送
- CSS 暗色星空主题

## 主要功能

- 🌠 **流星发射** — 写下烦恼，发射到星空，支持 AI 审核与治愈回响
- ✨ **捞取流星** — 随机捞取别人的流星，留下回复与祝福
- 🌌 **星图生成** — 输入文字生成独一无二的星空图，支持 2K 高清导出
- 👤 **个人中心** — 昵称签名编辑、头像上传、统计数据、修改密码
- 🔐 **登录注册** — 匿名登录 + 账号密码登录，登录态持久化
- 🛡️ **管理后台** — 审核流星/回复、用户管理、数据统计

## UI/UX 特性

- 🎨 暗色星空主题 + 玻璃拟态设计
- ✨ 页面切换动画（入场/退出）
- 📱 移动端适配（动态视口高度 + 刘海屏安全区域）
- ♿ 无障碍支持（ARIA 标签、键盘导航、减少动画偏好）
- 💀 骨架屏加载状态
- 🔔 自定义确认弹窗与 Toast 提示
- 🎵 背景音乐播放器

## 开发

```bash
npm install
npm run dev
```

## 构建

```bash
npm run build
```

## 项目结构

```
src/
├── components/         # 页面组件
│   ├── AuthGate.jsx    # 登录注册
│   ├── LaunchPage.jsx  # 发射流星
│   ├── CatchPage.jsx   # 捞取流星
│   ├── ProfilePage.jsx # 个人中心
│   ├── MeteorDetailPage.jsx # 流星详情
│   ├── StarMapPage.jsx # 星图生成
│   ├── AdminPage.jsx   # 管理后台
│   ├── StarField.jsx   # 星空背景
│   ├── StarMapCanvas.jsx # 星图 Canvas
│   ├── MusicPlayer.jsx # 音乐播放器
│   ├── Toast.jsx       # 消息提示
│   ├── ConfirmModal.jsx # 确认弹窗
│   ├── Skeleton.jsx    # 骨架屏
│   ├── ErrorBoundary.jsx # 错误边界
│   └── ...
├── api/                # API 客户端
├── utils.js            # 共享工具函数
└── styles/             # 全局样式
```
