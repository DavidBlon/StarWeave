import { useState, useEffect, useCallback } from 'react';
import {
  getPendingReviews, reviewMessage, getAdminStats, getAllMessages,
  getPendingWishes, getAllWishes, reviewWish, deleteWishAdmin,
  deleteMeteorAdmin, getWishStats, getAdminUsers, deleteUserAdmin,
} from '../api';
import { fmtTime } from '../utils';
import Pagination from './Pagination';
import ConfirmModal from './ConfirmModal';
import Skeleton from './Skeleton';

const TABS = [
  { key: 'pending', label: '待审核流星', icon: '⋯' },
  { key: 'pendingWish', label: '待审核回复', icon: '💜' },
  { key: 'all', label: '全部流星', icon: '✦' },
  { key: 'allWish', label: '全部回复', icon: '💫' },
  { key: 'users', label: '用户管理', icon: '👤' },
];

export default function AdminPage({ user, onShowToast }) {
  const [tab, setTab] = useState('pending');
  const [pendingList, setPendingList] = useState([]);
  const [pendingWishList, setPendingWishList] = useState([]);
  const [allList, setAllList] = useState([]);
  const [allWishList, setAllWishList] = useState([]);
  const [userList, setUserList] = useState([]);
  const [stats, setStats] = useState({ pendingCount: 0, approvedCount: 0, rejectedCount: 0, totalCount: 0 });
  const [wishStats, setWishStats] = useState({ pending: 0, approved: 0, rejected: 0, total: 0 });
  const [loading, setLoading] = useState(true);
  const [operatingId, setOperatingId] = useState(null);
  const [confirmDelete, setConfirmDelete] = useState(null);

  const toast = useCallback((msg) => { if (onShowToast) onShowToast(msg); }, [onShowToast]);

  // ===== 数据加载 =====
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

  const loadPendingWishes = useCallback(async () => {
    if (!user?.id) return;
    try {
      const [pendingRes, statsRes] = await Promise.all([
        getPendingWishes(user.id),
        getWishStats(user.id),
      ]);
      if (pendingRes.code === 200) setPendingWishList(pendingRes.data || []);
      if (statsRes.code === 200) setWishStats(statsRes.data);
    } catch (_) {}
  }, [user?.id]);

  const loadAll = useCallback(async () => {
    if (!user?.id) return;
    try {
      const res = await getAllMessages(user.id, null);
      if (res.code === 200) setAllList(res.data || []);
    } catch (_) {}
  }, [user?.id]);

  const loadAllWishes = useCallback(async () => {
    if (!user?.id) return;
    try {
      const res = await getAllWishes(user.id, null);
      if (res.code === 200) setAllWishList(res.data || []);
    } catch (_) {}
  }, [user?.id]);

  const loadUsers = useCallback(async () => {
    if (!user?.id) return;
    try {
      const res = await getAdminUsers(user.id);
      if (res.code === 200) setUserList(res.data || []);
    } catch (_) {}
  }, [user?.id]);

  useEffect(() => {
    setLoading(true);
    const loadFn = {
      pending: loadPending,
      pendingWishes: loadPendingWishes,
      all: loadAll,
      allWishes: loadAllWishes,
      users: loadUsers,
    }[tab];
    if (loadFn) {
      loadFn().finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, [tab, loadPending, loadPendingWishes, loadAll, loadAllWishes, loadUsers]);

  // ===== 操作 =====
  const handleReview = async (messageId, status) => {
    setOperatingId(messageId);
    try {
      const res = await reviewMessage(messageId, user.id, status, '');
      if (res.code === 200) {
        toast(status === 'approved' ? '✦ 流星已通过' : '✧ 流星已拒绝');
        loadPending(); loadAll();
      } else { toast(res.message || '操作失败'); }
    } catch (_) { toast('操作失败'); }
    finally { setOperatingId(null); }
  };

  const handleReviewWish = async (wishId, status) => {
    setOperatingId(wishId);
    try {
      const res = await reviewWish(wishId, user.id, status, '');
      if (res.code === 200) {
        toast(status === 'approved' ? '💜 回复已通过' : '✧ 回复已拒绝');
        loadPendingWishes(); loadAllWishes();
      } else { toast(res.message || '操作失败'); }
    } catch (_) { toast('操作失败'); }
    finally { setOperatingId(null); }
  };

  const handleDeleteMeteor = async (messageId) => {
    setOperatingId(messageId);
    try {
      const res = await deleteMeteorAdmin(messageId, user.id);
      if (res.code === 200) {
        toast('🗑 流星已删除');
        loadPending(); loadAll();
      } else { toast(res.message || '删除失败'); }
    } catch (_) { toast('删除失败'); }
    finally { setOperatingId(null); setConfirmDelete(null); }
  };

  const handleDeleteWish = async (wishId) => {
    setOperatingId(wishId);
    try {
      const res = await deleteWishAdmin(wishId, user.id);
      if (res.code === 200) {
        toast('🗑 回复已删除');
        loadPendingWishes(); loadAllWishes();
      } else { toast(res.message || '删除失败'); }
    } catch (_) { toast('删除失败'); }
    finally { setOperatingId(null); setConfirmDelete(null); }
  };

  const handleDeleteUser = async (userId) => {
    setOperatingId(userId);
    try {
      const res = await deleteUserAdmin(userId, user.id);
      if (res.code === 200) {
        toast('🗑 用户已删除');
        loadUsers(); loadAll(); loadAllWishes(); loadPending(); loadPendingWishes();
      } else { toast(res.message || '删除失败'); }
    } catch (_) { toast('删除失败'); }
    finally { setOperatingId(null); setConfirmDelete(null); }
  };

  // ===== 工具函数 =====
  const statusLabel = (s) => {
    switch (s) {
      case 'approved': return { text: '✦ 已通过', color: 'rgba(103,232,249,0.7)' };
      case 'rejected': return { text: '✧ 已拒绝', color: 'rgba(255,107,107,0.6)' };
      default: return { text: '⋯ 待审核', color: 'rgba(255,217,61,0.6)' };
    }
  };

  const StatusBadge = ({ status }) => (
    <span style={{ fontSize: 10, color: statusLabel(status).color }}>
      {statusLabel(status).text}
    </span>
  );

  // 删除按钮（带二次确认）
  const DeleteBtn = ({ id, type, onConfirm, disabled }) => {
    const key = `${type}-${id}`;
    if (confirmDelete === key) {
      return (
        <span style={{ display: 'inline-flex', gap: 4, alignItems: 'center' }}>
          <button
            className="admin-btn"
            style={{ background: 'rgba(255,107,107,0.15)', color: '#ff6b6b', border: '1px solid rgba(255,107,107,0.3)', fontSize: 10, padding: '3px 8px' }}
            onClick={onConfirm}
            disabled={disabled}
          >{disabled ? '...' : '确认删除'}</button>
          <button
            className="admin-btn"
            style={{ fontSize: 10, padding: '3px 8px' }}
            onClick={() => setConfirmDelete(null)}
          >取消</button>
        </span>
      );
    }
    return (
      <button
        style={{
          background: 'none', border: 'none', cursor: 'pointer',
          color: 'rgba(255,107,107,0.4)', fontSize: 12, padding: '2px 6px',
          transition: 'color 0.2s',
        }}
        onMouseEnter={(e) => e.target.style.color = '#ff6b6b'}
        onMouseLeave={(e) => e.target.style.color = 'rgba(255,107,107,0.4)'}
        onClick={() => setConfirmDelete(key)}
        title="删除"
        aria-label="删除"
      >🗑</button>
    );
  };

  // ===== 渲染流星卡片 =====
  const renderMeteorCard = (msg, showActions = true) => (
    <div className="admin-item" key={`m-${msg.id}`} style={{
      borderLeftColor: msg.status === 'approved' ? 'rgba(103,232,249,0.2)'
        : msg.status === 'rejected' ? 'rgba(255,107,107,0.15)' : 'rgba(255,184,108,0.15)',
    }}>
      <div className="admin-item-header">
        <span className="admin-item-user">用户 #{msg.userId}</span>
        <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          {msg._type && <span style={{ fontSize: 9, color: 'rgba(180,160,250,0.5)', background: 'rgba(180,160,250,0.08)', padding: '1px 5px', borderRadius: 4 }}>{msg._type}</span>}
          <StatusBadge status={msg.status} />
          <span className="admin-item-time">{fmtTime(msg.createdAt)}</span>
          {showActions && <DeleteBtn id={msg.id} type="meteor" disabled={operatingId === msg.id} onConfirm={() => handleDeleteMeteor(msg.id)} />}
        </span>
      </div>
      <div className="admin-item-content">{msg.content}</div>
      {msg.reviewReason && (
        <div className="admin-item-reason"><span className="reason-label">AI 判断：</span>{msg.reviewReason}</div>
      )}
      {msg.healTag && <div style={{ fontSize: 10, color: 'rgba(180,160,250,0.6)', marginBottom: 4, padding: '0 8px' }}>💫 {msg.healTag}</div>}
      {msg.healingMessage && <div style={{ fontSize: 10, color: 'rgba(103,232,249,0.5)', marginBottom: 4, padding: '0 8px', lineHeight: 1.5 }}>✨ {msg.healingMessage}</div>}
      {showActions && msg.status === 'pending' && (
        <div className="admin-item-actions">
          <button className="admin-btn admin-btn-approve" onClick={() => handleReview(msg.id, 'approved')} disabled={operatingId === msg.id}>
            {operatingId === msg.id ? '...' : '✦ 通过'}
          </button>
          <button className="admin-btn admin-btn-reject" onClick={() => handleReview(msg.id, 'rejected')} disabled={operatingId === msg.id}>
            {operatingId === msg.id ? '...' : '✧ 拒绝'}
          </button>
        </div>
      )}
    </div>
  );

  // ===== 渲染回复卡片 =====
  const renderWishCard = (wish, showActions = true) => (
    <div className="admin-item" key={`w-${wish.id}`} style={{
      borderLeftColor: wish.status === 'approved' ? 'rgba(103,232,249,0.2)'
        : wish.status === 'rejected' ? 'rgba(255,107,107,0.15)' : 'rgba(255,184,108,0.15)',
    }}>
      <div className="admin-item-header">
        <span className="admin-item-user">
          {wish.replierNickname || `用户 #${wish.userId}`}
          <span style={{ fontSize: 9, color: 'rgba(255,255,255,0.2)', marginLeft: 4 }}>回复流星 #{wish.meteorId}</span>
        </span>
        <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          {wish._type && <span style={{ fontSize: 9, color: 'rgba(180,160,250,0.5)', background: 'rgba(180,160,250,0.08)', padding: '1px 5px', borderRadius: 4 }}>{wish._type}</span>}
          <StatusBadge status={wish.status} />
          <span className="admin-item-time">{fmtTime(wish.createdAt)}</span>
          {showActions && <DeleteBtn id={wish.id} type="wish" disabled={operatingId === wish.id} onConfirm={() => handleDeleteWish(wish.id)} />}
        </span>
      </div>
      <div className="admin-item-content">{wish.content}</div>
      {wish.reviewReason && (
        <div className="admin-item-reason"><span className="reason-label">AI 判断：</span>{wish.reviewReason}</div>
      )}
      {showActions && wish.status === 'pending' && (
        <div className="admin-item-actions">
          <button className="admin-btn admin-btn-approve" onClick={() => handleReviewWish(wish.id, 'approved')} disabled={operatingId === wish.id}>
            {operatingId === wish.id ? '...' : '💜 通过'}
          </button>
          <button className="admin-btn admin-btn-reject" onClick={() => handleReviewWish(wish.id, 'rejected')} disabled={operatingId === wish.id}>
            {operatingId === wish.id ? '...' : '✧ 拒绝'}
          </button>
        </div>
      )}
    </div>
  );

  return (
    <div className="page active" id="pageAdmin">
      {/* Stats */}
      <div className="admin-header">
        <div className="admin-header-top">
          <span className="admin-icon">✦</span>
          <span className="admin-title">星海管理</span>
        </div>
        <div className="admin-stats">
          <div className="admin-stat">
            <span className="admin-stat-num">{stats.totalCount}</span>
            <span className="admin-stat-label">流星</span>
          </div>
          <div className="admin-stat">
            <span className="admin-stat-num" style={{ color: '#ffb86c' }}>{stats.pendingCount}</span>
            <span className="admin-stat-label">待审流星</span>
          </div>
          <div className="admin-stat">
            <span className="admin-stat-num" style={{ color: '#b4a0fa' }}>{wishStats.total || 0}</span>
            <span className="admin-stat-label">回复</span>
          </div>
          <div className="admin-stat">
            <span className="admin-stat-num" style={{ color: '#ffb86c' }}>{wishStats.pending || 0}</span>
            <span className="admin-stat-label">待审回复</span>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="auth-tabs" style={{ marginBottom: 8, flexWrap: 'wrap' }}>
        {TABS.map(t => (
          <button key={t.key} className={`auth-tab ${tab === t.key ? 'active' : ''}`} onClick={() => setTab(t.key)}>
            {t.icon} {t.label}
          </button>
        ))}
      </div>

      {/* Content */}
      {loading ? (
        <div style={{ padding: '20px 16px' }}>
          <Skeleton lines={5} />
        </div>
      ) : (
        <div className="admin-list">
          {tab === 'pending' && (
            <Pagination
              items={pendingList}
              emptyIcon="🌌"
              emptyText="暂无待审核流星，星海一片宁静"
              renderItem={(msg) => renderMeteorCard(msg)}
            />
          )}

          {tab === 'pendingWish' && (
            <Pagination
              items={pendingWishList}
              emptyIcon="💜"
              emptyText="暂无待审核回复"
              renderItem={(w) => renderWishCard(w)}
            />
          )}

          {tab === 'all' && (
            <Pagination
              items={allList}
              emptyIcon="🌠"
              emptyText="星海还没有流星"
              renderItem={(msg) => renderMeteorCard(msg)}
            />
          )}

          {tab === 'allWish' && (
            <Pagination
              items={allWishList}
              emptyIcon="💫"
              emptyText="还没有回复"
              renderItem={(w) => renderWishCard(w)}
            />
          )}

          {tab === 'users' && (
            <Pagination
              items={userList}
              emptyIcon="👤"
              emptyText="暂无用户"
              renderItem={(u) => (
                <div className="admin-item" key={`u-${u.id}`} style={{ borderLeftColor: u.isAdmin ? 'rgba(103,232,249,0.3)' : 'rgba(255,255,255,0.05)' }}>
                  <div className="admin-item-header">
                    <span className="admin-item-user">
                      {u.nickname || u.username}
                      {u.isAdmin && <span style={{ fontSize: 9, color: '#67e8f9', marginLeft: 4, background: 'rgba(103,232,249,0.1)', padding: '1px 5px', borderRadius: 4 }}>管理员</span>}
                      {u.isSponsor && <span style={{ fontSize: 9, color: '#ffb86c', marginLeft: 4, background: 'rgba(255,184,108,0.1)', padding: '1px 5px', borderRadius: 4 }}>赞助者</span>}
                    </span>
                    <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                      <span className="admin-item-time">{fmtTime(u.createdAt)}</span>
                      {!u.isAdmin && (
                        <DeleteBtn id={u.id} type="user" disabled={operatingId === u.id} onConfirm={() => handleDeleteUser(u.id)} />
                      )}
                    </span>
                  </div>
                  <div style={{ fontSize: 11, color: 'rgba(255,255,255,0.35)', padding: '2px 8px', display: 'flex', gap: 12, flexWrap: 'wrap' }}>
                    <span>ID: {u.id}</span>
                    <span>用户名: {u.username}</span>
                    {u.bio && <span>签名: {u.bio}</span>}
                  </div>
                </div>
              )}
            />
          )}
        </div>
      )}

      <div className="admin-footer">
        以 {user?.nickname || '管理员'} 身份操作
      </div>
    </div>
  );
}
