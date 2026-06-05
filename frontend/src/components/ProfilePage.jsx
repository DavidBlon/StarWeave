import { useState, useEffect, useCallback, useRef } from 'react';
import { getUserStats, updateProfile, uploadAvatar, uploadAvatarFile, changePassword, getUserMeteors, getCaughtMeteors, getUserWishes } from '../api';
import Pagination from './Pagination';

export default function ProfilePage({
  user: initialUser,
  onLogout,
  onOpenAvatar,
  onUserUpdate,
  pendingAvatarData,
  onClearPendingAvatar,
  onShowToast,
  onShowPolicy,
}) {
  const [user, setUser] = useState(initialUser);
  const [stats, setStats] = useState({ publishedCount: 0, caughtCount: 0, wishCount: 0 });

  // 统计列表展开
  const [viewList, setViewList] = useState(null); // 'caught' | 'published' | 'wishes' | null
  const [listData, setListData] = useState([]);
  const [listLoading, setListLoading] = useState(false);

  const LIST_TITLES = {
    caught: '捞到的流星',
    published: '发射的流星',
    wishes: '留下的回复',
  };

  // 密码修改
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [passwordForm, setPasswordForm] = useState({ oldPassword: '', newPassword: '', confirmPassword: '' });
  const [changingPassword, setChangingPassword] = useState(false);
  const [passwordError, setPasswordError] = useState('');

  // 编辑状态
  const [editingNickname, setEditingNickname] = useState(false);
  const [editingBio, setEditingBio] = useState(false);
  const [nicknameDraft, setNicknameDraft] = useState('');
  const [bioDraft, setBioDraft] = useState('');
  const [saving, setSaving] = useState(false);

  const nicknameRef = useRef(null);
  const bioRef = useRef(null);

  // 解析 emoji:char:bg:border 格式
  const parseEmojiAvatar = useCallback((url) => {
    if (!url || !url.startsWith('emoji:')) return null;
    const parts = url.split(':');
    if (parts.length < 4) return null;
    return {
      type: 'gradient',
      char: parts[1],
      bg: decodeURIComponent(parts[2]),
      border: decodeURIComponent(parts[3]),
    };
  }, []);

  // 根据 avatarUrl 构建展示 URL（仅图片类型）
  const buildAvatarDisplayUrl = useCallback((url, userId) => {
    if (!url) return null;
    if (url.startsWith('emoji:')) return null;
    if (url.startsWith('data:')) return url;
    // 文件类型 — 通过原始文件端点获取，用文件名作缓存键（含时间戳，天然防缓存）
    const filename = url.includes('/') ? url.substring(url.lastIndexOf('/') + 1) : url;
    return `/api/user/${userId}/avatar/raw?v=${encodeURIComponent(filename)}`;
  }, []);

  // 头像本地状态
  const [avatarDisplay, setAvatarDisplay] = useState(() => {
    const url = initialUser?.avatarUrl;
    // 优先解析 emoji
    const emoji = parseEmojiAvatar(url);
    if (emoji) return emoji;
    const display = buildAvatarDisplayUrl(url, initialUser?.id);
    if (display) {
      return { type: 'url', value: display };
    }
    const seed = (initialUser?.id || 0) % 8;
    const colors = [
      ['#8be9fd', '#c9a7ff'],
      ['#ff9ff3', '#f368e0'],
      ['#ffd93d', '#ff9a3c'],
      ['#6bcb77', '#2d6a4f'],
      ['#ff6b6b', '#ee5a24'],
      ['#a29bfe', '#6c5ce7'],
      ['#fd79a8', '#e84393'],
      ['#55efc4', '#00b894'],
    ];
    const c = colors[seed];
    return { type: 'gradient', bg: `linear-gradient(135deg, ${c[0]}, ${c[1]})`, border: c[0] + '44', char: (initialUser?.nickname?.charAt(0) || '✦') };
  });

  // 加载数据
  const loadData = useCallback(async () => {
    if (!user?.id) return;
    try {
      const statsRes = await getUserStats(user.id);
      if (statsRes.code === 200) setStats(statsRes.data);
    } catch (e) {
      // 静默失败
    }
  }, [user?.id]);

  // 展开统计列表
  const openList = useCallback(async (type) => {
    if (!user?.id) return;
    setViewList(type);
    setListLoading(true);
    setListData([]);
    try {
      let res;
      if (type === 'published') {
        res = await getUserMeteors(user.id);
      } else if (type === 'caught') {
        res = await getCaughtMeteors(user.id);
      } else if (type === 'wishes') {
        res = await getUserWishes(user.id);
      }
      if (res && res.code === 200) setListData(res.data || []);
    } catch (e) {
      // 静默
    } finally {
      setListLoading(false);
    }
  }, [user?.id]);

  const closeList = useCallback(() => {
    setViewList(null);
    setListData([]);
  }, []);

  useEffect(() => {
    setUser(initialUser);
    loadData();
  }, [initialUser, loadData]);

  // 同步头像（图片 URL / emoji 格式均处理）
  useEffect(() => {
    if (user?.avatarUrl) {
      const emoji = parseEmojiAvatar(user.avatarUrl);
      if (emoji) {
        setAvatarDisplay(emoji);
        return;
      }
      const display = buildAvatarDisplayUrl(user.avatarUrl, user.id);
      if (display) {
        setAvatarDisplay({ type: 'url', value: display });
      }
    }
  }, [user?.avatarUrl, user?.id, buildAvatarDisplayUrl, parseEmojiAvatar]);

  // ===== 处理待处理的头像数据 =====
  useEffect(() => {
    if (!pendingAvatarData) return;

    const processAvatar = async () => {
      const data = pendingAvatarData;
      if (data.type === 'image') {
        // 立即用 data URL 预览 — 上传 + 加载都不需要等待
        setAvatarDisplay({ type: 'url', value: data.url });

        // 后台异步上传到服务器
        try {
          const res = await uploadAvatarFile(user.id, data.file);
          if (res.code === 200) {
            const updated = res.data;
            setUser(updated);
            if (onUserUpdate) onUserUpdate(updated);
            const displayUrl = buildAvatarDisplayUrl(updated.avatarUrl, updated.id);
            if (displayUrl) {
              setAvatarDisplay({ type: 'url', value: displayUrl });
            }
          }
          // 上传失败：data URL 已经展示，保留即可
        } catch (e) {
          // 上传异常：data URL 已经展示，保留即可
        }
      } else if (data.type === 'emoji') {
        // emoji/渐变头像 — 存标识
        try {
          const emojiUrl = `emoji:${data.char}:${encodeURIComponent(data.bg)}:${encodeURIComponent(data.borderColor)}`;
          const res = await uploadAvatar(user.id, emojiUrl);
          if (res.code === 200) {
            const updated = res.data;
            setUser(updated);
            if (onUserUpdate) onUserUpdate(updated);
            setAvatarDisplay({ type: 'gradient', bg: data.bg, border: data.borderColor, char: data.char });
          } else {
            setAvatarDisplay({ type: 'gradient', bg: data.bg, border: data.borderColor, char: data.char });
          }
        } catch (e) {
          setAvatarDisplay({ type: 'gradient', bg: data.bg, border: data.borderColor, char: data.char });
        }
      }
      if (onClearPendingAvatar) onClearPendingAvatar();
    };

    processAvatar();
  }, [pendingAvatarData, user?.id, onUserUpdate, onClearPendingAvatar]);

  // --- 编辑昵称 ---
  const startEditNickname = () => {
    setNicknameDraft(user?.nickname || '');
    setEditingNickname(true);
    setTimeout(() => nicknameRef.current?.focus(), 50);
  };

  const saveNickname = async () => {
    const val = nicknameDraft?.trim();
    if (!val || val === user?.nickname) {
      setEditingNickname(false);
      return;
    }
    setSaving(true);
    try {
      const res = await updateProfile(user.id, { nickname: val });
      if (res.code === 200) {
        const updated = res.data;
        setUser(updated);
        if (onUserUpdate) onUserUpdate(updated);
        setAvatarDisplay(prev => {
          if (prev.type === 'gradient') return { ...prev, char: val.charAt(0) };
          return prev;
        });
      } else {
        alert(res.message || '保存失败');
      }
    } catch (e) {
      alert('保存失败，请重试');
    } finally {
      setSaving(false);
      setEditingNickname(false);
    }
  };

  const cancelEditNickname = () => setEditingNickname(false);

  // --- 编辑签名 ---
  const startEditBio = () => {
    setBioDraft(user?.bio || '');
    setEditingBio(true);
    setTimeout(() => bioRef.current?.focus(), 50);
  };

  const saveBio = async () => {
    const val = bioDraft?.trim() || '';
    if (val === (user?.bio || '')) {
      setEditingBio(false);
      return;
    }
    setSaving(true);
    try {
      const res = await updateProfile(user.id, { bio: val, nickname: user.nickname });
      if (res.code === 200) {
        const updated = res.data;
        setUser(updated);
        if (onUserUpdate) onUserUpdate(updated);
      } else {
        alert(res.message || '保存失败');
      }
    } catch (e) {
      alert('保存失败，请重试');
    } finally {
      setSaving(false);
      setEditingBio(false);
    }
  };

  const cancelEditBio = () => setEditingBio(false);

  // 修改密码
  const handleChangePassword = useCallback(async () => {
    setPasswordError('');
    if (!passwordForm.oldPassword) {
      setPasswordError('请输入旧密码');
      return;
    }
    if (passwordForm.newPassword.length < 6) {
      setPasswordError('新密码至少 6 位');
      return;
    }
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setPasswordError('两次输入的新密码不一致');
      return;
    }
    if (passwordForm.oldPassword === passwordForm.newPassword) {
      setPasswordError('新密码不能与旧密码相同');
      return;
    }
    setChangingPassword(true);
    try {
      const res = await changePassword(user.id, passwordForm.oldPassword, passwordForm.newPassword);
      if (res.code === 200) {
        setShowPasswordModal(false);
        setPasswordForm({ oldPassword: '', newPassword: '', confirmPassword: '' });
        if (onShowToast) onShowToast('密码修改成功');
      } else {
        setPasswordError(res.message || '修改失败');
      }
    } catch (e) {
      setPasswordError('修改失败，请重试');
    } finally {
      setChangingPassword(false);
    }
  }, [user, passwordForm, onShowToast]);

  const nickname = user?.nickname || '匿名旅人';
  const bioText = user?.bio || '在星河中漂流，捡拾别人的故事';

  return (
    <div className="page active" id="pageProfile">
      {/* 头像 */}
      <div className="profile-header">
        <div
          className={`profile-avatar ${avatarDisplay.type === 'url' ? 'has-image' : ''}`}
          style={
            avatarDisplay.type === 'url'
              ? { background: `url(${avatarDisplay.value}) center/cover no-repeat`, borderColor: 'transparent' }
              : { background: avatarDisplay.bg, borderColor: avatarDisplay.border }
          }
          onClick={onOpenAvatar}
        >
          {avatarDisplay.type !== 'url' && <span className="avatar-char">{avatarDisplay.char}</span>}
          <div className="edit-hint">换头像</div>
        </div>

        {/* 昵称 */}
        {editingNickname ? (
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6, marginBottom: 2 }}>
            <input
              ref={nicknameRef}
              className="auth-input"
              style={{ width: 160, textAlign: 'center', margin: 0 }}
              value={nicknameDraft}
              maxLength={20}
              onChange={e => setNicknameDraft(e.target.value)}
              onKeyDown={e => { if (e.key === 'Enter') saveNickname(); if (e.key === 'Escape') cancelEditNickname(); }}
              onBlur={saveNickname}
              disabled={saving}
            />
          </div>
        ) : (
          <div className="profile-name" onClick={startEditNickname} style={{ cursor: 'pointer' }}>
            {nickname} <span style={{ fontSize: 10, opacity: 0.3, marginLeft: 4 }}>✎</span>
          </div>
        )}

        {/* 签名 */}
        {editingBio ? (
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6, marginTop: 2 }}>
            <input
              ref={bioRef}
              className="auth-input"
              style={{ width: 200, textAlign: 'center', fontSize: 11, margin: 0 }}
              value={bioDraft}
              maxLength={200}
              onChange={e => setBioDraft(e.target.value)}
              onKeyDown={e => { if (e.key === 'Enter') saveBio(); if (e.key === 'Escape') cancelEditBio(); }}
              onBlur={saveBio}
              disabled={saving}
            />
          </div>
        ) : (
          <div className="profile-bio" onClick={startEditBio} style={{ cursor: 'pointer' }}>
            {bioText} <span style={{ fontSize: 9, opacity: 0.3 }}>✎</span>
          </div>
        )}
      </div>

      {/* 统计 */}
      <div className="profile-stats">
        <div className="stat-box" style={{ cursor: 'pointer' }} onClick={() => openList('caught')}>
          <div className="num">{stats.caughtCount}</div>
          <div className="label">捞到的流星</div>
        </div>
        <div className="stat-box" style={{ cursor: 'pointer' }} onClick={() => openList('published')}>
          <div className="num">{stats.publishedCount}</div>
          <div className="label">发射的流星</div>
        </div>
        <div className="stat-box" style={{ cursor: 'pointer' }} onClick={() => openList('wishes')}>
          <div className="num">{stats.wishCount}</div>
          <div className="label">留下的回复</div>
        </div>
      </div>

      {/* 设置 */}
      <div className="settings-item">
        <span className="left">用户名</span>
        <span className="right" style={{ opacity: 0.5 }}>{user?.username || '—'}</span>
      </div>
      <div className="settings-item" onClick={startEditNickname} style={{ cursor: 'pointer' }}>
        <span className="left">修改名字</span>
        <span className="right">{nickname}</span>
      </div>
      <div className="settings-item" onClick={startEditBio} style={{ cursor: 'pointer' }}>
        <span className="left">个人签名</span>
        <span className="right">{bioText.length > 12 ? bioText.substring(0, 12) + '...' : bioText}</span>
      </div>
      <div className="settings-item" onClick={() => { setPasswordForm({ oldPassword: '', newPassword: '', confirmPassword: '' }); setPasswordError(''); setShowPasswordModal(true); }} style={{ cursor: 'pointer' }}>
        <span className="left">修改密码</span>
        <span className="right" />
      </div>
      <div className="settings-item">
        <span className="left">深空模式</span>
        <span className="right">已开启</span>
      </div>
      <div className="settings-item" onClick={() => onShowPolicy && onShowPolicy('agreement')} style={{ cursor: 'pointer' }}>
        <span className="left">用户协议</span>
        <span className="right" />
      </div>
      <div className="settings-item" onClick={() => onShowPolicy && onShowPolicy('policy')} style={{ cursor: 'pointer' }}>
        <span className="left">隐私政策</span>
        <span className="right" />
      </div>

      <button className="btn-logout" onClick={onLogout}>退出登录</button>

      {/* 修改密码弹窗 */}
      {showPasswordModal && (
        <div className="modal-overlay show" onClick={() => setShowPasswordModal(false)}>
          <div className="modal-sheet" onClick={e => e.stopPropagation()}>
            <div className="modal-handle" />
            <div className="modal-title">修改密码</div>
            <input
              className="auth-input"
              type="password"
              placeholder="旧密码"
              value={passwordForm.oldPassword}
              onChange={e => setPasswordForm(f => ({ ...f, oldPassword: e.target.value }))}
            />
            <input
              className="auth-input"
              type="password"
              placeholder="新密码（至少 6 位）"
              value={passwordForm.newPassword}
              onChange={e => setPasswordForm(f => ({ ...f, newPassword: e.target.value }))}
            />
            <input
              className="auth-input"
              type="password"
              placeholder="确认新密码"
              value={passwordForm.confirmPassword}
              onChange={e => setPasswordForm(f => ({ ...f, confirmPassword: e.target.value }))}
            />
            {passwordError && (
              <div style={{ fontSize: 11, color: '#ff6b6b', marginBottom: 8, textAlign: 'center' }}>
                {passwordError}
              </div>
            )}
            <button
              className="auth-btn"
              onClick={handleChangePassword}
              disabled={changingPassword}
            >
              {changingPassword ? '修改中...' : '确认修改'}
            </button>
          </div>
        </div>
      )}

      {/* 赞助星海 — 暂隐藏，待与平台调试好后显示 */}

      {/* 统计列表详情 */}
      {viewList && (
        <div className="profile-list-overlay">
          <div className="profile-list-header">
            <button className="profile-list-back" onClick={closeList}>← 返回</button>
            <span className="profile-list-title">{LIST_TITLES[viewList]}</span>
            <span style={{ width: 48 }} />
          </div>

          <div className="profile-list-body">
            {listLoading ? (
              <div className="profile-list-empty">加载中...</div>
            ) : (
              <div className="profile-list-scroll">
                <Pagination
                  items={listData}
                  emptyText="暂无数据"
                  renderItem={(item, idx) => (
                    <div className="profile-list-item" key={item.id || idx}>
                      {viewList === 'caught' && (
                        <>
                          <div className="pli-preview">{preview(item.content, 80)}</div>
                          {item.healingMessage && (
                            <div className="pli-sub">💜 {preview(item.healingMessage, 60)}</div>
                          )}
                          <div className="pli-meta">
                            <span>{item.healerNickname ? `捞取人：${item.healerNickname}` : '漂流中'}</span>
                            <span>{fmtTime(item.caughtAt || item.createdAt)}</span>
                          </div>
                        </>
                      )}
                      {viewList === 'published' && (
                        <>
                          <div className="pli-header">
                            <span className="pli-status" data-status={item.status}>
                              {item.status === 'approved' ? '✦ 已发射'
                                : item.status === 'rejected' ? '✧ 未通过'
                                : '⋯ 审核中'}
                            </span>
                            <span className="pli-time">{fmtTime(item.createdAt)}</span>
                          </div>
                          <div className="pli-preview">{preview(item.content, 120)}</div>
                          {item.healingMessage && (
                            <div className="pli-sub">💜 {preview(item.healingMessage, 60)}</div>
                          )}
                        </>
                      )}
                      {viewList === 'wishes' && (
                        <>
                          <div className="pli-header">
                            <span className="pli-wisher">我</span>
                            <span className="pli-time">{fmtTime(item.createdAt)}</span>
                          </div>
                          <div className="pli-preview">{preview(item.content, 120)}</div>
                          <div className="pli-sub" style={{ color: 'rgba(139,233,253,0.35)', fontSize: 9 }}>
                            回应了：{preview(item.meteorContent, 40)}
                          </div>
                        </>
                      )}
                    </div>
                  )}
                />
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

function fmtTime(t) {
  if (!t) return '';
  const d = new Date(t);
  const m = (d.getMonth() + 1).toString().padStart(2, '0');
  const day = d.getDate().toString().padStart(2, '0');
  const h = d.getHours().toString().padStart(2, '0');
  const mi = d.getMinutes().toString().padStart(2, '0');
  return `${m}-${day} ${h}:${mi}`;
}

function preview(text, len = 60) {
  if (!text) return '';
  return text.length > len ? text.substring(0, len) + '...' : text;
}
