import { useState, useRef, useCallback, useEffect } from 'react';
import StarMapCanvas from './StarMapCanvas';

const PRESETS = [
  '今天天气很好，我在想你',
  '考研二战又失败了，感觉自己好没用',
  '和异地恋的女朋友吵架了',
  '妈妈最近身体不好，我在外地工作',
  '朋友好像都在渐行渐远',
  '转行学编程好难',
  '在星河中漂流，捡拾别人的故事',
  '愿你三冬暖，愿你春不寒',
  '人生就像公交车，每一站都会有人上下',
  '月亮很圆，像你的笑脸',
];

const HEALING_QUOTES = [
  '你已经很努力了',
  '星星会替我拥抱你',
  '今天也辛苦了',
  '一切都会好起来的',
  '你值得被温柔以待',
  '慢慢来，不着急',
  '放下也是一种勇敢',
  '你不是一个人',
  '深呼吸，没关系的',
  '明天又是新的一天',
];

// ===== 浮动星尘粒子 =====
function DustParticles({ containerRef }) {
  const canvasRef = useRef(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    const container = containerRef.current;
    if (!canvas || !container) return;
    const ctx = canvas.getContext('2d');
    let animId;
    let particles = [];

    function resize() {
      canvas.width = container.offsetWidth;
      canvas.height = container.offsetHeight;
    }
    resize();

    // 生成星尘
    for (let i = 0; i < 35; i++) {
      particles.push({
        x: Math.random() * canvas.width,
        y: Math.random() * canvas.height,
        r: 0.4 + Math.random() * 1.2,
        vx: (Math.random() - 0.5) * 0.15,
        vy: -0.1 - Math.random() * 0.2,
        alpha: 0.1 + Math.random() * 0.3,
        twinkleSpeed: 0.3 + Math.random() * 1,
        phase: Math.random() * Math.PI * 2,
        hue: 200 + Math.random() * 60,
      });
    }

    function draw(now) {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      particles.forEach(p => {
        p.x += p.vx;
        p.y += p.vy;
        if (p.y < -5) { p.y = canvas.height + 5; p.x = Math.random() * canvas.width; }
        if (p.x < -5) p.x = canvas.width + 5;
        if (p.x > canvas.width + 5) p.x = -5;

        const twinkle = 0.4 + 0.6 * Math.sin(now * 0.001 * p.twinkleSpeed + p.phase);
        const a = p.alpha * twinkle;

        // 光晕
        ctx.beginPath();
        ctx.arc(p.x, p.y, p.r * 3, 0, Math.PI * 2);
        ctx.fillStyle = `hsla(${p.hue},60%,70%,${a * 0.08})`;
        ctx.fill();

        // 粒子本体
        ctx.beginPath();
        ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
        ctx.fillStyle = `hsla(${p.hue},40%,80%,${a})`;
        ctx.fill();
      });
      animId = requestAnimationFrame(draw);
    }
    animId = requestAnimationFrame(draw);

    const onResize = () => resize();
    window.addEventListener('resize', onResize);
    return () => {
      cancelAnimationFrame(animId);
      window.removeEventListener('resize', onResize);
    };
  }, [containerRef]);

  return (
    <canvas
      ref={canvasRef}
      style={{
        position: 'absolute',
        inset: 0,
        pointerEvents: 'none',
        zIndex: 0,
      }}
    />
  );
}

// ===== 生成时的星光爆发粒子 =====
function SparkleBurst({ active, originX, originY }) {
  const canvasRef = useRef(null);

  useEffect(() => {
    if (!active) return;
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    canvas.width = canvas.offsetWidth;
    canvas.height = canvas.offsetHeight;

    const cx = originX || canvas.width / 2;
    const cy = originY || canvas.height / 2;
    const sparks = [];
    for (let i = 0; i < 30; i++) {
      const angle = Math.random() * Math.PI * 2;
      const speed = 1 + Math.random() * 3;
      sparks.push({
        x: cx, y: cy,
        vx: Math.cos(angle) * speed,
        vy: Math.sin(angle) * speed,
        r: 0.5 + Math.random() * 1.5,
        life: 1,
        decay: 0.008 + Math.random() * 0.015,
        hue: 200 + Math.random() * 60,
      });
    }

    let animId;
    function draw() {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      let alive = false;
      sparks.forEach(s => {
        if (s.life <= 0) return;
        alive = true;
        s.x += s.vx;
        s.y += s.vy;
        s.vx *= 0.98;
        s.vy *= 0.98;
        s.life -= s.decay;

        const a = s.life;
        ctx.beginPath();
        ctx.arc(s.x, s.y, s.r * 2, 0, Math.PI * 2);
        ctx.fillStyle = `hsla(${s.hue},60%,70%,${a * 0.1})`;
        ctx.fill();

        ctx.beginPath();
        ctx.arc(s.x, s.y, s.r, 0, Math.PI * 2);
        ctx.fillStyle = `hsla(${s.hue},50%,85%,${a * 0.8})`;
        ctx.fill();
      });
      if (alive) animId = requestAnimationFrame(draw);
    }
    animId = requestAnimationFrame(draw);
    return () => cancelAnimationFrame(animId);
  }, [active, originX, originY]);

  if (!active) return null;

  return (
    <canvas
      ref={canvasRef}
      style={{
        position: 'absolute',
        inset: 0,
        pointerEvents: 'none',
        zIndex: 2,
      }}
    />
  );
}

// ===== 治愈语句轮播 =====
function HealingQuote() {
  const [idx, setIdx] = useState(() => Math.floor(Math.random() * HEALING_QUOTES.length));
  const [visible, setVisible] = useState(true);

  useEffect(() => {
    const interval = setInterval(() => {
      setVisible(false);
      setTimeout(() => {
        setIdx(prev => (prev + 1) % HEALING_QUOTES.length);
        setVisible(true);
      }, 600);
    }, 5000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div style={{
      textAlign: 'center',
      fontSize: 12,
      color: 'rgba(201,167,255,0.4)',
      letterSpacing: 2,
      height: 18,
      transition: 'opacity 0.6s ease',
      opacity: visible ? 1 : 0,
    }}>
      💜 {HEALING_QUOTES[idx]}
    </div>
  );
}

// ===== 主页面 =====
export default function StarMapPage({ onShowToast }) {
  const [inputText, setInputText] = useState('');
  const [displayText, setDisplayText] = useState('在星河中漂流，捡拾别人的故事');
  const [inputFocused, setInputFocused] = useState(false);
  const [bursting, setBursting] = useState(false);
  const [burstKey, setBurstKey] = useState(0);
  const canvasRef = useRef(null);
  const pageRef = useRef(null);
  const btnRef = useRef(null);

  const handleGenerate = useCallback(() => {
    const text = inputText.trim();
    if (!text) {
      onShowToast && onShowToast('请先写下一段心事');
      return;
    }
    setDisplayText(text);

    // 触发星光爆发
    setBursting(false);
    setTimeout(() => {
      setBurstKey(k => k + 1);
      setBursting(true);
    }, 10);
  }, [inputText, onShowToast]);

  const handleDownload = useCallback(() => {
    if (!canvasRef.current) return;
    const dataUrl = canvasRef.current.exportPNG();
    if (!dataUrl) return;
    const link = document.createElement('a');
    link.download = 'star-map.png';
    link.href = dataUrl;
    link.click();
    onShowToast && onShowToast('星图已保存');
  }, [onShowToast]);

  const handleShuffle = useCallback(() => {
    const t = PRESETS[Math.floor(Math.random() * PRESETS.length)];
    setInputText(t);
    setDisplayText(t);
    // 随机也触发小爆发
    setBursting(false);
    setTimeout(() => {
      setBurstKey(k => k + 1);
      setBursting(true);
    }, 10);
  }, []);

  const handleKeyDown = useCallback((e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleGenerate();
    }
  }, [handleGenerate]);

  // Canvas 外框呼吸光晕
  const [glowIntensity, setGlowIntensity] = useState(0);
  useEffect(() => {
    let animId;
    function breathe(now) {
      const v = 0.4 + 0.6 * Math.sin(now * 0.0008);
      setGlowIntensity(v);
      animId = requestAnimationFrame(breathe);
    }
    animId = requestAnimationFrame(breathe);
    return () => cancelAnimationFrame(animId);
  }, []);

  return (
    <div
      ref={pageRef}
      className="page active"
      id="pageStarMap"
      style={{
        position: 'relative',
        display: 'flex',
        flexDirection: 'column',
        gap: 12,
        overflow: 'hidden',
      }}
    >
      {/* 浮动星尘 */}
      <DustParticles containerRef={pageRef} />

      {/* 内容层 */}
      <div style={{ position: 'relative', zIndex: 1, display: 'flex', flexDirection: 'column', gap: 12 }}>
        {/* 标题 */}
        <div style={{ textAlign: 'center', marginTop: 4 }}>
          <div style={{
            fontSize: 18, fontWeight: 200, letterSpacing: 4,
            background: 'linear-gradient(135deg, #c9a7ff, #8be9fd)',
            WebkitBackgroundClip: 'text', backgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
          }}>✦ 织星海</div>
          <div style={{ fontSize: 11, color: 'rgba(255,255,255,0.3)', marginTop: 4, letterSpacing: 1 }}>
            每一段心事，都有一片属于自己的星空
          </div>
        </div>

        {/* 治愈语句 */}
        <HealingQuote />

        {/* 输入区 */}
        <textarea
          value={inputText}
          onChange={e => setInputText(e.target.value)}
          onKeyDown={handleKeyDown}
          onFocus={() => setInputFocused(true)}
          onBlur={() => setInputFocused(false)}
          placeholder="写下一段心事、一句话、一个名字……&#10;你的文字会变成一片独一无二的星空"
          style={{
            width: '100%',
            background: inputFocused ? 'rgba(139,233,253,0.03)' : 'rgba(255,255,255,0.02)',
            border: `1px solid ${inputFocused ? 'rgba(139,233,253,0.15)' : 'rgba(255,255,255,0.06)'}`,
            borderRadius: 12,
            color: '#e0e0f0',
            fontSize: 14,
            lineHeight: 1.8,
            padding: '12px 14px',
            resize: 'none',
            minHeight: 70,
            maxHeight: 100,
            fontFamily: 'inherit',
            outline: 'none',
            transition: 'all 0.4s ease',
            boxShadow: inputFocused ? '0 0 20px rgba(139,233,253,0.05)' : 'none',
          }}
        />

        {/* Canvas 区域 — 呼吸光晕外框 */}
        <div style={{
          position: 'relative',
          borderRadius: 16,
          overflow: 'hidden',
          background: '#060612',
          boxShadow: `0 0 ${30 + glowIntensity * 40}px rgba(139,233,253,${0.02 + glowIntensity * 0.03}), 0 0 ${60 + glowIntensity * 60}px rgba(201,167,255,${0.01 + glowIntensity * 0.02})`,
          transition: 'box-shadow 0.1s linear',
        }}>
          <StarMapCanvas ref={canvasRef} text={displayText} />
          {/* 星光爆发层 */}
          <SparkleBurst key={burstKey} active={bursting} />
        </div>

        {/* 按钮 */}
        <div ref={btnRef} style={{ display: 'flex', gap: 10, justifyContent: 'center', flexWrap: 'wrap' }}>
          <button
            className="auth-btn"
            onClick={handleGenerate}
            style={{
              padding: '10px 28px', borderRadius: 10, fontSize: 13, letterSpacing: 1,
              background: 'linear-gradient(135deg, #8be9fd, #c9a7ff)',
              color: '#0a0a1a', border: 'none', fontWeight: 500,
              width: 'auto', margin: 0,
              position: 'relative',
              overflow: 'hidden',
            }}
          >
            ✦ 生成星图
          </button>
          <button
            onClick={handleDownload}
            style={{
              padding: '10px 28px', borderRadius: 10, fontSize: 13, letterSpacing: 1,
              background: 'transparent',
              border: '1px solid rgba(255,255,255,0.06)',
              color: 'rgba(255,255,255,0.6)', cursor: 'pointer',
              transition: 'all 0.3s', fontFamily: 'inherit',
            }}
          >↓ 保存图片</button>
          <button
            onClick={handleShuffle}
            style={{
              padding: '10px 28px', borderRadius: 10, fontSize: 13, letterSpacing: 1,
              background: 'transparent',
              border: '1px solid rgba(255,255,255,0.06)',
              color: 'rgba(255,255,255,0.6)', cursor: 'pointer',
              transition: 'all 0.3s', fontFamily: 'inherit',
            }}
          >🎲 随机</button>
        </div>

        {/* 提示 */}
        <div style={{ textAlign: 'center', fontSize: 11, color: 'rgba(255,255,255,0.15)', letterSpacing: 1 }}>
          同一段文字永远生成同一片星空 · 它是独属于你的
        </div>
      </div>
    </div>
  );
}
