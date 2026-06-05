import { useState, useEffect, useCallback } from 'react';
import { getRandomMeteor, catchMeteor as apiCatch, makeWish as apiWish, getWishes, getCaughtMeteors } from '../api';
import Pagination from './Pagination';

const EMPTY_REPLY = '还没有留言，来做第一个吧';

export default function CatchPage({ user, onShowToast, onViewMeteor }) {
  const [meteor, setMeteor] = useState(null);
  const [visible, setVisible] = useState(false);
  const [loading, setLoading] = useState(false);
  const [replyText, setReplyText] = useState('');
  const [wishCount, setWishCount] = useState(0);
  const [replies, setReplies] = useState([]);
  const [history, setHistory] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(true);
  const [showHistory, setShowHistory] = useState(false);

  // 加载捞取记录
  useEffect(() => {
    if (!user?.id) return;
    setHistoryLoading(true);
    getCaughtMeteors(user.id).then(res => {
      if (res.code === 200) setHistory(res.data || []);
    }).catch(() => {}).finally(() => setHistoryLoading(false));
  }, [user?.id]);

  const doCatch = useCallback(async () => {
    setLoading(true);
    setVisible(false);
    try {
      const res = await getRandomMeteor(user?.id);
      if (res.code === 200 && res.data) {
        const m = res.data;
        setMeteor(m);
        setWishCount(m.wishCount || 0);
        setReplies([]);
        setTimeout(() => setVisible(true), 400);
        // 尝试捞取
        apiCatch(m.id, user.id).then(res => {
          if (res.code !== 200 && onShowToast) onShowToast(res.message || '未能捞起这颗流星');
        }).catch(() => {});
        // 加载真实回复
        try {
          const wishesRes = await getWishes(m.id);
          if (wishesRes.code === 200 && wishesRes.data) {
            setReplies(wishesRes.data.map(w => ({
              text: w.content,
              pending: false,
              nickname: w.replierNickname || '匿名旅人',
            })));
            setWishCount(wishesRes.data.length);
          }
        } catch (_) {}
      } else {
        onShowToast('星海暂无漂流中的流星');
      }
    } catch (e) {
      onShowToast('星海暂无漂流中的流星');
    }
    // 刷新捞取记录
    getCaughtMeteors(user.id).then(res => {
      if (res.code === 200) setHistory(res.data || []);
    }).catch(() => {});
    setLoading(false);
  }, [user, onShowToast]);

  const sendReply = useCallback(async () => {
    const text = replyText.trim();
    if (!text || !meteor) return;
    // 先本地乐观更新
    const newReply = { text, pending: true, nickname: user?.nickname || '匿名旅人' };
    setReplies(prev => [...prev, newReply]);
    setReplyText('');
    onShowToast('回复已送达流星');
    // 调用真实 API
    try {
      await apiWish(meteor.id, user.id, text);
      setReplies(prev => prev.map(r => r.text === text && r.pending ? { ...r, pending: false } : r));
    } catch (_) {
      // API 失败但保留本地显示
      setReplies(prev => prev.map(r => r.text === text && r.pending ? { ...r, pending: false } : r));
    }
  }, [replyText, meteor, user, onShowToast]);

  const doWish = useCallback(async () => {
    if (!meteor) return;
    if (meteor.id) {
      apiWish(meteor.id, user.id, '愿一切安好').catch(() => {});
    }
    setWishCount(prev => prev + 1);
    onShowToast('愿望已送达星河');
  }, [meteor, user, onShowToast]);

  return (
    <div className="page active" id="pageCatch">
      <div className="catch-header">
        <p>每一次捞取，都会遇见别人的一段故事</p>
      </div>
      <button className="btn-catch" onClick={doCatch} disabled={loading}>
        {loading ? '正在捞取...' : '捞一颗流星'}
      </button>
      <div className={`meteor-card ${visible ? 'visible' : ''}`}>
        <div
          className="card-header"
          style={visible && meteor?.id ? { cursor: 'pointer' } : {}}
          onClick={() => visible && meteor?.id && onViewMeteor && onViewMeteor(meteor.id)}
          title={visible ? '点击查看详情' : ''}
        >
          <span className="heal-badge">{meteor?.healTag || meteor?.tag || '会好的'}</span>
        </div>
        <div
          className="card-content"
          style={visible && meteor?.id ? { cursor: 'pointer' } : {}}
          onClick={() => visible && meteor?.id && onViewMeteor && onViewMeteor(meteor.id)}
        >{meteor?.content || '最近工作压力好大，每天加班到很晚。'}</div>
        {meteor?.healingMessage && (
          <div className="card-healing">{meteor.healingMessage}</div>
        )}
        <div className="card-meta">
          <span>{wishCount} 人许过愿</span>
          <span>
            <span style={{ marginRight: 8 }}>{replies.length} 条回复</span>
            <button className="btn-wish" onClick={doWish}>许个愿</button>
            {visible && meteor?.id && (
              <button className="btn-detail" onClick={() => onViewMeteor && onViewMeteor(meteor.id)} style={{ marginLeft: 6, fontSize: 10, padding: '2px 6px', background: 'rgba(255,255,255,0.08)', border: '1px solid rgba(255,255,255,0.15)', borderRadius: 4, color: 'rgba(255,255,255,0.6)', cursor: 'pointer' }}>
                详情
              </button>
            )}
          </span>
        </div>
        <div className="reply-section">
          <div className="reply-list">
            {replies.length === 0 ? (
              <div style={{ textAlign: 'center', padding: 8, fontSize: 10, color: 'rgba(255,255,255,0.06)' }}>
                {EMPTY_REPLY}
              </div>
            ) : (
              replies.map((r, i) => (
                <div key={i} className={`reply-item ${r.pending ? 'pending' : ''}`}>
                  <div className="avatar">·</div>
                  <div className="body">
                    <div className="name">
                      {r.nickname || '匿名旅人'}
                      {r.pending && <span className="pend">审核中</span>}
                    </div>
                    <div className="text">{r.text}</div>
                  </div>
                </div>
              ))
            )}
          </div>
          <div className="reply-input-area">
            <input
              type="text"
              placeholder="写下你想对 TA 说的话"
              maxLength={100}
              value={replyText}
              onChange={e => setReplyText(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && sendReply()}
            />
            <button className="btn-reply" onClick={sendReply}>送出去</button>
          </div>
        </div>
      </div>

      {/* 捞取记录 */}
      <div className="section-title" style={{ cursor: 'pointer' }} onClick={() => {
        const expanding = !showHistory;
        setShowHistory(expanding);
        if (expanding && user?.id) {
          setHistoryLoading(true);
          getCaughtMeteors(user.id).then(res => {
            if (res.code === 200) setHistory(res.data || []);
          }).catch(() => {}).finally(() => setHistoryLoading(false));
        }
      }}>
        捞取记录 ({history.length})
        <svg className={`section-arrow ${showHistory ? 'open' : ''}`} viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M6 9l6 6 6-6"/>
        </svg>
      </div>
      {showHistory && (
        <div className="history-list">
          {historyLoading ? (
            <div style={{ textAlign: 'center', padding: 12, fontSize: 10, color: 'rgba(255,255,255,0.12)' }}>
              加载中...
            </div>
          ) : (
            <Pagination
              items={history}
              emptyIcon="📭"
              emptyText="还没有捞取记录"
              renderItem={(m) => (
                <div
                  className="history-item clickable"
                  key={m.id}
                  onClick={() => onViewMeteor && onViewMeteor(m.id)}
                  style={{ cursor: 'pointer' }}
                >
                  <span className="tag">✦ 流星</span>
                  <span className="preview">{preview(m.content, 120)}</span>
                  <span className="time">{fmtTime(m.caughtAt || m.createdAt)}</span>
                </div>
              )}
            />
          )}
        </div>
      )}
    </div>
  );
}

// 格式化时间
function fmtTime(t) {
  if (!t) return '';
  const d = new Date(t);
  const m = (d.getMonth() + 1).toString().padStart(2, '0');
  const day = d.getDate().toString().padStart(2, '0');
  const h = d.getHours().toString().padStart(2, '0');
  const mi = d.getMinutes().toString().padStart(2, '0');
  return `${m}-${day} ${h}:${mi}`;
}

function preview(text, len = 30) {
  if (!text) return '';
  return text.length > len ? text.substring(0, len) + '...' : text;
}
