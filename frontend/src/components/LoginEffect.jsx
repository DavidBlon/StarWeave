import { useEffect, useRef } from 'react';

export default function LoginEffect({ show, onComplete }) {
  const canvasRef = useRef(null);

  useEffect(() => {
    if (!show) return;

    // reduced-motion: 跳过动画，直接完成
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      onComplete?.();
      return;
    }

    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const cw = canvas.width = window.innerWidth;
    const ch = canvas.height = window.innerHeight;

    // 获取登录卡片位置
    const authBox = document.querySelector('.auth-box');
    const boxRect = authBox?.getBoundingClientRect();
    const boxCX = boxRect ? boxRect.left + boxRect.width / 2 : cw / 2;
    const boxCY = boxRect ? boxRect.top + boxRect.height / 2 : ch / 2;
    const boxArea = boxRect ? Math.sqrt(boxRect.width * boxRect.height) * 0.45 : 100;

    // Phase 1: 卡片解构粒子
    const P_COLORS = ['103,232,249', '180,160,250', '255,255,255', '255,217,61', '85,239,196'];
    const particles = [];
    for (let i = 0; i < 200; i++) {
      const angle = Math.random() * Math.PI * 2;
      const dist = Math.random() * boxArea;
      particles.push({
        x: boxCX + Math.cos(angle) * dist,
        y: boxCY + Math.sin(angle) * dist,
        vx: (Math.random() - 0.5) * 0.12,
        vy: -(0.25 + Math.random() * 1.0),
        size: 0.5 + Math.random() * 2.5,
        color: P_COLORS[Math.floor(Math.random() * P_COLORS.length)],
        alpha: 0.5 + Math.random() * 0.5,
        delay: Math.random() * 600,
        drift: (Math.random() - 0.5) * 0.4,
        twinkle: 2 + Math.random() * 4,
      });
    }

    // Phase 2: 大流星
    const meteors = [
      { sx: -200, sy: 50, ex: cw + 120, ey: ch * 0.52, delay: 150, dur: 1000, len: 220, wid: 2.2, alp: 0.7 },
      { sx: cw + 120, sy: -80, ex: -120, ey: ch * 0.42, delay: 350, dur: 1200, len: 180, wid: 1.8, alp: 0.6 },
      { sx: -100, sy: 180, ex: cw * 0.7, ey: ch * 0.58, delay: 550, dur: 900, len: 200, wid: 2.0, alp: 0.55 },
      { sx: cw * 0.75, sy: -120, ex: -100, ey: ch * 0.35, delay: 750, dur: 1100, len: 240, wid: 2.5, alp: 0.65 },
      { sx: -180, sy: ch * 0.25, ex: cw + 80, ey: ch * 0.68, delay: 950, dur: 800, len: 150, wid: 1.5, alp: 0.45 },
    ];

    let startTime = performance.now();
    let welcomeShown = false;
    let welcomeFading = false;
    let phase4Started = false;
    let completed = false;

    const gateEls = {
      authBox: document.querySelector('.auth-box'),
      gateTitle: document.querySelector('.gate-title'),
      gateDesc: document.querySelector('.gate-desc'),
      gateIcon: document.querySelector('.gate-icon'),
    };

    const welcomeText = document.getElementById('welcomeText');
    const pageContent = document.getElementById('pageContent');
    const authGate = document.getElementById('authGate');
    const bottomTabs = document.getElementById('bottomTabs');
    const topBar = document.getElementById('topBar');

    function animateLogin(now) {
      const elapsed = now - startTime;
      ctx.clearRect(0, 0, cw, ch);

      // Phase 1: 卡片解构 (0~800ms)
      if (elapsed < 800) {
        const fp = Math.min(elapsed / 600, 1);
        const ef = 1 - Math.pow(1 - fp, 3);
        if (gateEls.authBox) {
          gateEls.authBox.style.opacity = 1 - ef;
          gateEls.authBox.style.transform = `scale(${1 - ef * 0.12})`;
        }
        if (gateEls.gateTitle) gateEls.gateTitle.style.opacity = 1 - ef * 0.6;
        if (gateEls.gateDesc) gateEls.gateDesc.style.opacity = 1 - ef * 0.8;
        if (gateEls.gateIcon) gateEls.gateIcon.style.opacity = 1 - ef * 0.5;
      }

      // 绘制所有粒子
      particles.forEach(p => {
        const lt = elapsed - p.delay;
        if (lt < 0) return;
        const t = lt / 1000;
        const x = p.x + p.vx * t * 50 + p.drift * t * 30 + Math.sin(t * p.twinkle + p.delay * 0.1) * 12;
        const y = p.y + p.vy * t * 60 + t * t * 10;
        const tw = 0.6 + 0.4 * Math.sin(t * p.twinkle * 2 + p.delay);
        const alpha = p.alpha * Math.max(0, 1 - t * 0.5) * tw;
        if (alpha <= 0.01) return;
        const s = p.size * (1 + t * 0.3);
        ctx.beginPath();
        ctx.arc(x, y, s, 0, Math.PI * 2);
        ctx.fillStyle = `rgba(${p.color},${alpha})`;
        ctx.fill();
        ctx.beginPath();
        ctx.arc(x, y, s * 3, 0, Math.PI * 2);
        ctx.fillStyle = `rgba(${p.color},${alpha * 0.06})`;
        ctx.fill();
      });

      // Phase 2: 流星 (200~2000ms)
      meteors.forEach(m => {
        const lt = elapsed - m.delay;
        if (lt < 0 || lt > m.dur) return;
        const p = Math.min(lt / m.dur, 1);
        const ease = p < 0.5 ? 2 * p * p : 1 - Math.pow(-2 * p + 2, 2) / 2;
        const x = m.sx + (m.ex - m.sx) * ease;
        const y = m.sy + (m.ey - m.sy) * ease;
        const tail = m.len * (0.5 + p * 0.5);
        const dx = m.ex - m.sx, dy = m.ey - m.sy;
        const L = Math.sqrt(dx * dx + dy * dy) || 1;
        const nx = dx / L, ny = dy / L;
        const tx = x - nx * tail, ty = y - ny * tail;
        const alpha = m.alp * (1 - p * 0.6);
        const grad = ctx.createLinearGradient(x, y, tx, ty);
        grad.addColorStop(0, `rgba(255,255,255,${alpha})`);
        grad.addColorStop(0.15, `rgba(103,232,249,${alpha * 0.35})`);
        grad.addColorStop(0.5, `rgba(103,232,249,${alpha * 0.1})`);
        grad.addColorStop(1, 'transparent');
        ctx.beginPath();
        ctx.moveTo(x, y);
        ctx.lineTo(tx, ty);
        ctx.strokeStyle = grad;
        ctx.lineWidth = m.wid * (1 + p * 0.3);
        ctx.lineCap = 'round';
        ctx.stroke();
        const gs = m.wid * 5;
        const hg = ctx.createRadialGradient(x, y, 0, x, y, gs);
        hg.addColorStop(0, `rgba(255,255,255,${alpha * 0.25})`);
        hg.addColorStop(1, 'transparent');
        ctx.beginPath();
        ctx.arc(x, y, gs, 0, Math.PI * 2);
        ctx.fillStyle = hg;
        ctx.fill();
      });

      // Phase 3: 文字告白 (600~2400ms)
      if (elapsed >= 600 && elapsed < 2400) {
        if (!welcomeShown) {
          welcomeShown = true;
          welcomeText?.classList.add('show');
        }
        if (elapsed > 1800 && !welcomeFading) {
          welcomeFading = true;
          welcomeText?.classList.remove('show');
        }
      }

      // Phase 4: 水波纹展开 (2200~3200ms)
      if (elapsed >= 2200 && !phase4Started) {
        phase4Started = true;
        if (gateEls.authBox) {
          gateEls.authBox.style.opacity = '';
          gateEls.authBox.style.transform = '';
        }
        if (gateEls.gateTitle) gateEls.gateTitle.style.opacity = '';
        if (gateEls.gateDesc) gateEls.gateDesc.style.opacity = '';
        if (gateEls.gateIcon) gateEls.gateIcon.style.opacity = '';

        if (authGate) authGate.style.display = 'none';
        bottomTabs?.classList.remove('locked');
        if (topBar) topBar.style.opacity = '1';

        if (pageContent) {
          pageContent.style.transition = 'none';
          pageContent.style.transform = 'scale(0.92)';
          pageContent.style.opacity = '0';
          void pageContent.offsetWidth;
          pageContent.style.transition = 'transform 0.8s cubic-bezier(0.34, 1.56, 0.64, 1), opacity 0.6s ease';
          pageContent.style.transform = 'scale(1)';
          pageContent.style.opacity = '1';
        }
      }

      if (elapsed < 3200) {
        requestAnimationFrame(animateLogin);
      } else if (!completed) {
        completed = true;
        ctx.clearRect(0, 0, cw, ch);
        const overlay = document.getElementById('loginEffect');
        if (overlay) overlay.style.display = 'none';
        welcomeText?.classList.remove('show');
        setTimeout(() => {
          if (pageContent) {
            pageContent.style.transition = '';
            pageContent.style.transform = '';
            pageContent.style.opacity = '';
          }
        }, 900);
        onComplete?.();
      }
    }

    requestAnimationFrame(animateLogin);
  }, [show, onComplete]);

  if (!show) return null;

  return (
    <div className="login-effect show" id="loginEffect">
      <canvas ref={canvasRef} className="effect-canvas" />
    </div>
  );
}
