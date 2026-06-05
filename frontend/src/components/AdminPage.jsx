import { useState, useEffect, useCallback } from 'react';
import { getPendingReviews, reviewMessage, getAdminStats, getAllMessages } from '../api';

const TABS = [
  { key: 'pending', label: '待审核', icon: '⋯' },
  { key: 'all', label: '全部流星', icon: '✦' },
  { key: 'reviewed', label: '已审核', icon: '✓' },
];

export default function AdminPage({ user, onShowToast }) {
  const [tab, setTab] = useState('pending');
  const [pendingList, setPendingList] = useState([]);
  const [allList, setAllList] = useState([]);
  const [reviewedList, setReviewedList] = useState([]);
  const [stats, setStats] = useState({ pendingCount: 0, approvedCount: 0, rejectedCount: 0, totalCount: 0 });
  const [loading, setLoading] = useState(true);
  const [operatingId, setOperatingId] = useState(null);

  const loadPending = useCallback(async () => {
    if (!user?.id) return;
    try {
      const [pendingRes, statsRes] = await Promise.all([
        getPendingReviews(user.id),
        getAdminStats(user.id),
      ]);
      if (pendingRes.code === 200) setPendingList(pendingRes.data || []);
      if (statsRes.code === 200) setStats(statsRes.data);
    } catch (_) {}
  }, [user?.id]);

  const loadAll = useCallback(async () => {
    if (!user?.id) return;
    try {
      const res = await getAllMessages(user.id, null);
      if (res.code === 200) setAllList(res.data || []);
    } catch (_) {}
  }, [user?.id]);

  const loadReviewed = useCallback(async () => {
    if (!user?.id) return;
    try {
      const [approvedRes, rejectedRes] = await Promise.all([
        getAllMessages(user.id, 'approved'),
        getAllMessages(user.id, 'rejected'),
      ]);
      const approved = approvedRes.code === 200 ? (approvedRes.data || []) : [];
      const rejected = rejectedRes.code === 200 ? (rejectedRes.data || []) : [];
      setReviewedList([...approved, ...rejected].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)));
    } catch (_) {}
  }, [user?.id]);

  useEffect(() => {
    setLoading(true);
    Promise.all([loadPending(), loadAll(), loadReviewed()]).finally(() => setLoading(false));
  }, [loadPending, loadAll, loadReviewed]);

  const handleReview = async (messageId, status) => {
    setOperatingId(messageId);
    try {
      const res = await reviewMessage(messageId, user.id, status, '');
      if (res.code === 200) {
        if (onShowToast) onShowToast(status === 'approved' ? '✦ 已通过' : '✧ 已拒绝');
        Promise.all([loadPending(), loadAll(), loadReviewed()]);
      } else {
        if (onShowToast) onShowToast(res.message || '操作失败');
      }
    } catch (e) {
      if (onShowToast) onShowToast('操作失败');
    } finally {
      setOperatingId(null);
    }
  };

  const fmtTime = (t) => {
    if (!t) return '';
    const d = new Date(t);
    const m = (d.getMonth() + 1).toString().padStart(2, '0');
    const day = d.getDate().toString().padStart(2, '0');
    const h = d.getHours().toString().padStart(2, '0');
    const mi = d.getMinutes().toString().padStart(2, '0');
    return `${m}-${day} ${h}:${mi}`;
  };

  const statusLabel = (s) => {
    switch (s) {
      case 'approved': return { text: '✦ 已通过', color: 'rgba(139,233,253,0.7)' };
      case 'rejected': return { text: '✧ 已拒绝', color: 'rgba(255,107,107,0.6)' };
      default: return { text: '⋯ 待审核', color: 'rgba(255,217,61,0.6)' };
    }
  };

  return (
    <div className="page active" id="pageAdmin">
      {/* 头部统计 */}
      <div className="admin-header">
        <div className="admin-header-top">
          <span className="admin-icon">✦</span>
          <span className="admin-title">星海管理</span>
        </div>
        <div className="admin-stats">
          <div className="admin-stat">
            <span className="admin-stat-num">{stats.totalCount}</span>
            <span className="admin-stat-label">全部</span>
          </div>
          <div className="admin-stat">
            <span className="admin-stat-num" style={{ color: '#ffb86c' }}>{stats.pendingCount}</span>
            <span className="admin-stat-label">待审核</span>
          </div>
          <div className="admin-stat">
            <span className="admin-stat-num" style={{ color: '#8be9fd' }}>{stats.approvedCount}</span>
            <span className="admin-stat-label">已通过</span>
          </div>
          <div className="admin-stat">
            <span className="admin-stat-num" style={{ color: '#ff6b6b' }}>{stats.rejectedCount}</span>
            <span className="admin-stat-label">已拒绝</span>
          </div>
        </div>
      </div>

      {/* Tab 切换 */}
      <div className="auth-tabs" style={{ marginBottom: 8 }}>
        {TABS.map(t => (
          <button
            key={t.key}
            className={`auth-tab ${tab === t.key ? 'active' : ''}`}
            onClick={() => setTab(t.key)}
          >
            {t.icon} {t.label}
          </button>
        ))}
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: 24, fontSize: 10, color: 'rgba(255,255,255,0.12)' }}>
          加载中...
        </div>
      ) : tab === 'pending' ? (
        /* ===== 待审核 ===== */
        pendingList.length === 0 ? (
          <div className="admin-empty">
            <div className="admin-empty-icon">🌌</div>
            <div className="admin-empty-text">暂无待审核流星<br />星海一片宁静</div>
          </div>
        ) : (
          <div className="admin-list">
            {pendingList.map(msg => (
              <div className="admin-item" key={msg.id}>
                <div className="admin-item-header">
                  <span className="admin-item-user">用户 #{msg.userId}</span>
                  <span className="admin-item-time">{fmtTime(msg.createdAt)}</span>
                </div>
                <div className="admin-item-content">{msg.content}</div>
                {msg.reviewReason && (
                  <div className="admin-item-reason">
                    <span className="reason-label">AI 备注：</span>
                    {msg.reviewReason}
                  </div>
                )}
                <div className="admin-item-actions">
                  <button
                    className="admin-btn admin-btn-approve"
                    onClick={() => handleReview(msg.id, 'approved')}
                    disabled={operatingId === msg.id}
                  >
                    {operatingId === msg.id ? '处理中...' : '✦ 通过'}
                  </button>
                  <button
                    className="admin-btn admin-btn-reject"
                    onClick={() => handleReview(msg.id, 'rejected')}
                    disabled={operatingId === msg.id}
                  >
                    {operatingId === msg.id ? '处理中...' : '✧ 拒绝'}
                  </button>
                </div>
              </div>
            ))}
          </div>
        )
      ) : tab === 'all' ? (
        /* ===== 全部流星 ===== */
        allList.length === 0 ? (
          <div className="admin-empty">
            <div className="admin-empty-icon">🌠</div>
            <div className="admin-empty-text">星海还没有流星</div>
          </div>
        ) : (
          <div className="admin-list">
            {allList.map(msg => (
              <div className="admin-item" key={msg.id} style={{ borderLeftColor: msg.status === 'approved' ? 'rgba(139,233,253,0.2)' : msg.status === 'rejected' ? 'rgba(255,107,107,0.15)' : 'rgba(255,184,108,0.15)' }}>
                <div className="admin-item-header">
                  <span className="admin-item-user">用户 #{msg.userId}</span>
                  <span>
                    <span style={{ fontSize: 10, marginRight: 8, color: statusLabel(msg.status).color }}>
                      {statusLabel(msg.status).text}
                    </span>
                    <span className="admin-item-time">{fmtTime(msg.createdAt)}</span>
                  </span>
                </div>
                <div className="admin-item-content">{msg.content}</div>

                {/* AI 审核反馈 */}
                {msg.reviewReason && (
                  <div className="admin-item-reason">
                    <span className="reason-label">AI 判断：</span>
                    {msg.reviewReason}
                  </div>
                )}
                {msg.healTag && (
                  <div style={{ fontSize: 10, color: 'rgba(201,167,255,0.6)', marginBottom: 4, padding: '0 8px' }}>
                    💫 治愈标签：{msg.healTag}
                  </div>
                )}
                {msg.healingMessage && (
                  <div style={{ fontSize: 10, color: 'rgba(139,233,253,0.5)', marginBottom: 4, padding: '0 8px', lineHeight: 1.5 }}>
                    ✨ 治愈回响：{msg.healingMessage}
                  </div>
                )}

                {/* 待审核的才显示操作按钮 */}
                {msg.status === 'pending' && (
                  <div className="admin-item-actions">
                    <button
                      className="admin-btn admin-btn-approve"
                      onClick={() => handleReview(msg.id, 'approved')}
                      disabled={operatingId === msg.id}
                    >
                      {operatingId === msg.id ? '处理中...' : '✦ 通过'}
                    </button>
                    <button
                      className="admin-btn admin-btn-reject"
                      onClick={() => handleReview(msg.id, 'rejected')}
                      disabled={operatingId === msg.id}
                    >
                      {operatingId === msg.id ? '处理中...' : '✧ 拒绝'}
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        )
      ) : (
        /* ===== 已审核 ===== */
        reviewedList.length === 0 ? (
          <div className="admin-empty">
            <div className="admin-empty-icon">✓</div>
            <div className="admin-empty-text">还没有已审核的流星</div>
          </div>
        ) : (
          <div className="admin-list">
            {reviewedList.map(msg => (
              <div className="admin-item" key={msg.id} style={{ borderLeftColor: msg.status === 'approved' ? 'rgba(139,233,253,0.2)' : 'rgba(255,107,107,0.15)' }}>
                <div className="admin-item-header">
                  <span className="admin-item-user">用户 #{msg.userId}</span>
                  <span>
                    <span style={{ fontSize: 10, marginRight: 8, color: statusLabel(msg.status).color }}>
                      {statusLabel(msg.status).text}
                    </span>
                    <span className="admin-item-time">{fmtTime(msg.createdAt)}</span>
                  </span>
                </div>
                <div className="admin-item-content">{msg.content}</div>

                {/* AI 审核反馈 */}
                {msg.reviewReason && (
                  <div className="admin-item-reason">
                    <span className="reason-label">AI 判断：</span>
                    {msg.reviewReason}
                  </div>
                )}
                {msg.healTag && (
                  <div style={{ fontSize: 10, color: 'rgba(201,167,255,0.6)', marginBottom: 4, padding: '0 8px' }}>
                    💫 治愈标签：{msg.healTag}
                  </div>
                )}
                {msg.healingMessage && (
                  <div style={{ fontSize: 10, color: 'rgba(139,233,253,0.5)', marginBottom: 4, padding: '0 8px', lineHeight: 1.5 }}>
                    ✨ 治愈回响：{msg.healingMessage}
                  </div>
                )}
              </div>
            ))}
          </div>
        )
      )}

      <div className="admin-footer">
        以 {user?.nickname || '管理员'} 身份操作
      </div>
    </div>
  );
}