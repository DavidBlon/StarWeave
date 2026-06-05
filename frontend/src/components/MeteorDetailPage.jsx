import { useState, useEffect, useCallback } from 'react';
import { getMeteor, getWishes, deleteMeteor, deleteWish, makeWish } from '../api';
import Pagination from './Pagination';

export default function MeteorDetailPage({ meteorId, user, onBack, onShowToast, onDeleted }) {
  const [meteor, setMeteor] = useState(null);
  const [wishes, setWishes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [deleting, setDeleting] = useState(false);
  const [deletingWishId, setDeletingWishId] = useState(null);
  const [replyText, setReplyText] = useState('');
  const [sendingReply, setSendingReply] = useState(false);

  const loadData = useCallback(() => {
    if (!meteorId) return;
    setLoading(true);
    Promise.all([
      getMeteor(meteorId),
      getWishes(meteorId),
    ]).then(([meteorRes, wishesRes]) => {
      if (meteorRes.code === 200) setMeteor(meteorRes.data);
      if (wishesRes.code === 200) setWishes(wishesRes.data || []);
    }).catch(() => {
      if (onShowToast) onShowToast('加载失败');
    }).finally(() => setLoading(false));
  }, [meteorId, onShowToast]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleDelete = useCallback(async () => {
    if (!window.confirm('确定要删除这颗流星吗？删除后不可恢复。')) return;
    setDeleting(true);
    try {
      const res = await deleteMeteor(meteorId, user.id);
      if (res.code === 200) {
        if (onShowToast) onShowToast('流星已消逝在星河中');
        if (onDeleted) onDeleted(meteorId);
        if (onBack) onBack();
      } else {
        if (onShowToast) onShowToast(res.message || '删除失败');
      }
    } catch (e) {
      if (onShowToast) onShowToast('删除失败');
    } finally {
      setDeleting(false);
    }
  }, [meteorId, user, onBack, onShowToast, onDeleted]);

  const handleDeleteWish = useCallback(async (wishId) => {
    if (!window.confirm('确定要删除这条回复吗？')) return;
    setDeletingWishId(wishId);
    try {
      const res = await deleteWish(wishId, user.id);
      if (res.code === 200) {
        if (onShowToast) onShowToast('回复已删除');
        loadData();
      } else {
        if (onShowToast) onShowToast(res.message || '删除失败');
      }
    } catch (_) {
      if (onShowToast) onShowToast('删除失败');
    } finally {
      setDeletingWishId(null);
    }
  }, [user, onShowToast, loadData]);

  const handleReply = useCallback(async () => {
    const text = replyText.trim();
    if (!text || !meteor || sendingReply) return;
    setSendingReply(true);
    try {
      await makeWish(meteorId, user.id, text);
      setReplyText('');
      if (onShowToast) onShowToast('回复已送达流星');
      loadData(); // 刷新回复列表
    } catch (_) {
      if (onShowToast) onShowToast('发送失败，请重试');
    } finally {
      setSendingReply(false);
    }
  }, [replyText, meteor, meteorId, user, sendingReply, onShowToast, loadData]);

  const fmtTime = (t) => {
    if (!t) return '';
    const d = new Date(t);
    const y = d.getFullYear();
    const m = (d.getMonth() + 1).toString().padStart(2, '0');
    const day = d.getDate().toString().padStart(2, '0');
    const h = d.getHours().toString().padStart(2, '0');
    const mi = d.getMinutes().toString().padStart(2, '0');
    return `${y}-${m}-${day} ${h}:${mi}`;
  };

  if (loading) {
    return (
      <div className="page active" id="pageMeteorDetail">
        <div className="detail-header">
          <button className="detail-back" onClick={onBack}>← 返回</button>
        </div>
        <div style={{ textAlign: 'center', padding: 40, fontSize: 12, color: 'rgba(255,255,255,0.2)' }}>
          加载中...
        </div>
      </div>
    );
  }

  if (!meteor) {
    return (
      <div className="page active" id="pageMeteorDetail">
        <div className="detail-header">
          <button className="detail-back" onClick={onBack}>← 返回</button>
        </div>
        <div style={{ textAlign: 'center', padding: 40, fontSize: 12, color: 'rgba(255,255,255,0.2)' }}>
          流星不存在或已消逝
        </div>
      </div>
    );
  }

  return (
    <div className="page active" id="pageMeteorDetail">
      <div className="detail-header">
        <button className="detail-back" onClick={onBack}>← 返回</button>
        <span className="detail-title">流星详情</span>
        {meteor && user && meteor.userId === user.id && (
          <button
            className="detail-delete"
            onClick={handleDelete}
            disabled={deleting}
          >
            {deleting ? '删除中...' : '删除'}
          </button>
        )}
      </div>

      <div className="detail-scroll-content">
        <div className="detail-card">
          <div className="detail-status" data-status={meteor.status}>
            {meteor.status === 'approved' ? '✦ 已发射'
              : meteor.status === 'rejected' ? '✧ 被打回'
              : '⋯ 审核中'}
          </div>

          {meteor.reviewReason && (
            <div className="detail-review-reason" data-status={meteor.status}>
              <span className="review-reason-label">审核意见：</span>
              {meteor.reviewReason}
            </div>
          )}

          {meteor.healTag && (
            <div className="detail-heal-tag">{meteor.healTag}</div>
          )}

          <div className="detail-content">{meteor.content}</div>

          {meteor.healingMessage && (
            <div className="detail-healing">
              <div className="healing-label">✦ AI 的回信</div>
              {meteor.healingMessage}
            </div>
          )}

          <div className="detail-meta">
            <div className="detail-meta-item">
              <span className="meta-label">发布时间</span>
              <span className="meta-value">{fmtTime(meteor.createdAt)}</span>
          </div>
          {meteor.caughtAt && (
            <div className="detail-meta-item">
              <span className="meta-label">捞取时间</span>
              <span className="meta-value">{fmtTime(meteor.caughtAt)}</span>
            </div>
          )}
          <div className="detail-meta-item">
            <span className="meta-label">状态</span>
            <span className="meta-value">
              {meteor.isCaught ? '已被捞走' : '漂流中'}
            </span>
          </div>
          <div className="detail-meta-item">
            <span className="meta-label">回复数</span>
            <span className="meta-value">{wishes.length}</span>
          </div>
        </div>
      </div>

        {/* 回复列表 */}
        <div className="detail-section-title">回复 ({wishes.length})</div>
        <div className="detail-wishes-list">
          <Pagination
            items={wishes}
            emptyText="还没有人回复这颗流星"
            renderItem={(w) => (
              <div className="detail-wish-item" key={w.id}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div className="detail-wish-author">{w.replierNickname || '匿名旅人'}</div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <div className="detail-wish-time">{fmtTime(w.createdAt)}</div>
                    {w.userId === user.id && (
                      <button
                        onClick={() => handleDeleteWish(w.id)}
                        disabled={deletingWishId === w.id}
                        style={{
                          background: 'none', border: 'none', cursor: 'pointer',
                          color: 'rgba(255,107,107,0.3)', fontSize: 11, padding: '1px 4px',
                          transition: 'color 0.2s',
                        }}
                        onMouseEnter={(e) => e.target.style.color = '#ff6b6b'}
                        onMouseLeave={(e) => e.target.style.color = 'rgba(255,107,107,0.3)'}
                        title="删除回复"
                      >{deletingWishId === w.id ? '...' : '🗑'}</button>
                    )}
                  </div>
                </div>
                <div className="detail-wish-text">{w.content}</div>
              </div>
            )}
          />
        </div>
      </div>

      {/* 回复输入 */}
      <div className="detail-reply-area">
        <div style={{ display: 'flex', gap: 8 }}>
          <input
            type="text"
            placeholder="写下你想对 TA 说的话…"
            maxLength={100}
            value={replyText}
            onChange={e => setReplyText(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && !sendingReply && handleReply()}
            disabled={sendingReply}
            style={{ flex: 1, padding: '8px 12px', borderRadius: 8, border: '1px solid rgba(255,255,255,0.12)', background: 'rgba(255,255,255,0.05)', color: '#e0e0e0', fontSize: 12, outline: 'none' }}
          />
          <button
            onClick={handleReply}
            disabled={sendingReply || !replyText.trim()}
            style={{ padding: '8px 16px', borderRadius: 8, border: 'none', background: 'linear-gradient(135deg, #8be9fd, #c9a7ff)', color: '#1a1a2e', fontSize: 12, fontWeight: 600, cursor: 'pointer', opacity: sendingReply || !replyText.trim() ? 0.5 : 1 }}
          >
            {sendingReply ? '发送中…' : '回复'}
          </button>
        </div>
      </div>
    </div>
  );
}
