import { useState, useRef, useCallback, useEffect } from 'react';
import { publishMeteor, getUserMeteors } from '../api';
import { fmtTime, preview } from '../utils';
import Pagination from './Pagination';
import Skeleton from './Skeleton';

const HEALING_EMOJIS = {
  '会好的': '🌱',
  '加油': '💪',
  '抱抱你': '🤗',
  '放下了': '🍃',
  '想开点': '☀️',
  '慢慢来': '🐢',
  '我懂': '💜',
  '没关系': '🌈',
};

export default function LaunchPage({ user, onShowToast, onHideToast, onViewMeteor }) {
  const [text, setText] = useState('');
  const [gathering, setGathering] = useState(false);
  const [healing, setHealing] = useState(null);
  const [healingFading, setHealingFading] = useState(false);
  const [myMeteors, setMyMeteors] = useState([]);
  const [myMeteorsLoading, setMyMeteorsLoading] = useState(true);
  const [showMyMeteors, setShowMyMeteors] = useState(false);
  const healingTimerRef = useRef(null);
  const healingLiveRef = useRef(false);
  const cardRef = useRef(null);
  const btnRef = useRef(null);

  // 加载我的流星
  useEffect(() => {
    if (!user?.id) return;
    setMyMeteorsLoading(true);
    getUserMeteors(user.id).then(res => {
      if (res.code === 200) setMyMeteors(res.data || []);
    }).catch(() => {}).finally(() => setMyMeteorsLoading(false));
  }, [user?.id]);

  // 治愈回响：3 秒后渐变消失（只响应「有无」，更新内容不重置计时）
  useEffect(() => {
    if (healing) {
      healingLiveRef.current = true;
      setHealingFading(false);
      healingTimerRef.current = setTimeout(() => {
        setHealingFading(true);
        setTimeout(() => {
          setHealing(null);
          healingLiveRef.current = false;
        }, 600);
      }, 3000);
      return () => clearTimeout(healingTimerRef.current);
    }
  }, [healing ? 'active' : null]);

  const dismissHealing = useCallback(() => {
    clearTimeout(healingTimerRef.current);
    setHealingFading(true);
    setTimeout(() => {
      setHealing(null);
      healingLiveRef.current = false;
    }, 600);
  }, []);

  const launch = useCallback(async () => {
    const content = text.trim();
    if (!content) return;
    if (btnRef.current) btnRef.current.disabled = true;
    if (btnRef.current) btnRef.current.textContent = '化作流星...';
    setGathering(true);

    // 获取卡片位置 -> 流星汇聚动画
    const card = cardRef.current;
    if (!card) return;
    const rect = card.getBoundingClientRect();
    const cx = rect.left + rect.width / 2;
    const cy = rect.top + rect.height / 2;

    // 创建汇聚光团
    const cluster = document.createElement('div');
    cluster.className = 'meteor-fly';
    cluster.style.left = cx + 'px';
    cluster.style.top = cy + 'px';
    cluster.innerHTML = `<div class="core"></div><div class="text-tail">${content.substring(0, 20)}${content.length > 20 ? '…' : ''}</div>`;
    cluster.style.transform = 'scale(0.3)';
    cluster.style.opacity = '0';
    document.body.appendChild(cluster);

    requestAnimationFrame(() => {
      cluster.style.transition = 'all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94)';
      cluster.style.transform = 'scale(1)';
      cluster.style.opacity = '1';
    });

    // 发射流星弧线飞行
    setTimeout(async () => {
      const endX = window.innerWidth * (0.3 + Math.random() * 0.5);
      const endY = window.innerHeight * (0.5 + Math.random() * 0.3);
      const midY = cy - 180 - Math.random() * 100;

      // 拖尾粒子
      const pi = setInterval(() => {
        const p = document.createElement('div');
        p.className = 'trail-particle';
        const s = 2 + Math.random() * 4;
        p.style.width = s + 'px';
        p.style.height = s + 'px';
        p.style.left = (cx + (Math.random() - 0.5) * 12) + 'px';
        p.style.top = (cy + (Math.random() - 0.5) * 12) + 'px';
        document.body.appendChild(p);
        setTimeout(() => p.remove(), 600);
      }, 30);

      const dur = 1300;
      const st = performance.now();

      function fly(t) {
        const e = Math.min((t - st) / dur, 1);
        const ez = e < 0.5 ? 2 * e * e : 1 - Math.pow(-2 * e + 2, 2) / 2;
        const x = cx + (endX - cx) * Math.pow(ez, 1.3);
        const y = cy + (midY - cy) * ez + (endY - midY) * Math.pow(ez, 2);
        cluster.style.transform = `translate(${x - cx}px, ${y - cy}px) scale(${Math.max(1 - ez * 0.85, 0.1)})`;
        cluster.style.opacity = 1 - Math.pow(ez, 1.5);
        if (e < 1) {
          requestAnimationFrame(fly);
        } else {
          clearInterval(pi);
          cluster.remove();
          // 小黑框：正在温柔审核
          onShowToast('正在温柔审核');

          publishMeteor(user.id, content, null)
            .then(res => {
              // 刷新我的流星列表
              getUserMeteors(user.id).then(r => {
                if (r.code === 200) setMyMeteors(r.data || []);
              }).catch(() => {});
              // AI 返回后弹出治愈回响（先关 toast，保证不重叠）
              if (res.data && res.data.status === 'approved' && res.data.healingMessage) {
                if (onHideToast) onHideToast();
                setHealing({
                  tag: res.data.healTag || '会好的',
                  message: res.data.healingMessage,
                });
              } else {
                onShowToast(res.message || '审核通过了，已飞向星空');
              }
            })
            .catch(() => {
              onShowToast('发射失败，请稍后重试');
            })
            .finally(() => {
              setGathering(false);
              setText('');
              if (card) {
                card.classList.remove('gathering');
                card.style.transform = '';
                card.style.opacity = '';
              }
              if (btnRef.current) {
                btnRef.current.disabled = false;
                btnRef.current.textContent = '让它飞向星空';
              }
            });
        }
      }
      requestAnimationFrame(fly);
    }, 300);
  }, [text, user, onShowToast]);

  return (
    <div className="page active" id="pageLaunch" style={showMyMeteors ? { display: 'flex', flexDirection: 'column', height: 'auto', minHeight: '100%' } : undefined}>
      <div className={`launch-card ${gathering ? 'gathering' : ''}`} ref={cardRef}>
        <textarea
          placeholder="写下你的烦恼&#10;它会变成一颗流星划过夜空"
          aria-label="写下你的烦恼"
          maxLength={200}
          rows={4}
          value={text}
          onChange={e => setText(e.target.value)}
          disabled={gathering}
        />
        <div className="launch-footer">
          <span className="char-count">{text.length}/200</span>
          <button className="btn-primary" onClick={launch} disabled={gathering || !text.trim()} ref={btnRef}>
            让它飞向星空
          </button>
        </div>
      </div>

      {/* 我的流星 */}
      <div className="section-title" style={{ cursor: 'pointer' }} onClick={() => {
        const expanding = !showMyMeteors;
        setShowMyMeteors(expanding);
        if (expanding && user?.id) {
          setMyMeteorsLoading(true);
          getUserMeteors(user.id).then(res => {
            if (res.code === 200) setMyMeteors(res.data || []);
          }).catch(() => {}).finally(() => setMyMeteorsLoading(false));
        }
      }}>
        我的流星 ({myMeteors.length})
        <svg className={`section-arrow ${showMyMeteors ? 'open' : ''}`} viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M6 9l6 6 6-6"/>
        </svg>
      </div>
      {showMyMeteors && (
        <div className="my-meteors-list" style={{ marginTop: 8, flex: 1, overflow: 'auto', display: 'flex', flexDirection: 'column' }}>
          {myMeteorsLoading ? (
            <div style={{ padding: '12px 0' }}>
              <Skeleton lines={3} />
            </div>
          ) : (
            <Pagination
              items={myMeteors}
              emptyIcon="🌌"
              emptyText="还没有发射过流星"
              renderItem={(m) => (
                <div
                  className="my-meteor-item clickable"
                  key={m.id}
                  onClick={() => onViewMeteor && onViewMeteor(m.id)}
                  style={{ cursor: 'pointer', borderBottom: '1px solid rgba(255,255,255,0.04)' }}
                >
                  <div className="my-meteor-header">
                    <span className="my-meteor-status" data-status={m.status}>
                      {m.status === 'approved' ? '✦ 已发射' : m.status === 'rejected' ? '✧ 未通过' : '⋯ 审核中'}
                    </span>
                    <span className="my-meteor-time">{fmtTime(m.createdAt)}</span>
                  </div>
                  <div className="my-meteor-content">{preview(m.content, 200)}</div>
                  <div className="my-meteor-click-hint">点击查看详情</div>
                </div>
              )}
            />
          )}
        </div>
      )}

      {/* 治愈回响 */}
      {healing && (
        <div className={`healing-overlay ${healingFading ? 'fading' : ''}`} onClick={dismissHealing}>
          <div className="healing-modal" onClick={e => e.stopPropagation()}>
            <div className="healing-emoji">{HEALING_EMOJIS[healing.tag] || '✨'}</div>
            <div className="healing-tag">{healing.tag}</div>
            <div className="healing-message">{healing.message}</div>
            <div className="healing-sub">你的心事已化作流星，会有人在星海中接住它</div>
          </div>
        </div>
      )}
    </div>
  );
}
