import { useEffect, useRef } from 'react';

export default function LogoutEffect({ show, onComplete }) {
  const canvasRef = useRef(null);

  useEffect(() => {
    if (!show) return;

    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const cw = canvas.width = window.innerWidth;
    const ch = canvas.height = window.innerHeight;

    const authBox = document.querySelector('.auth-box');
    const boxRect = authBox?.getBoundingClientRect();
    const targetX = boxRect ? boxRect.left + boxRect.width / 2 : cw / 2;
    const targetY = boxRect ? boxRect.top + boxRect.height / 2 : ch / 2;

    // 逆向粒子：从边缘向中心汇聚
    const P_COLORS = ['139,233,253', '201,167,255', '255,255,255', '255,217,61', '85,239,196'];
    const particles = [];
    for (let i = 0; i < 240; i++) {
      const edge = Math.floor(Math.random() * 4);
      let sx, sy;
      if (edge === 0) { sx = Math.random() * cw; sy = -20 - Math.random() * 80; }
      else if (edge === 1) { sx = Math.random() * cw; sy = ch + 20 + Math.random() * 80; }
      else if (edge === 2) { sx = -20 - Math.random() * 80; sy = Math.random() * ch; }
      else { sx = cw + 20 + Math.random() * 80; sy = Math.random() * ch; }
      particles.push({
        sx, sy, tx: targetX, ty: targetY,
        size: 0.8 + Math.random() * 2.8,
        color: P_COLORS[Math.floor(Math.random() * P_COLORS.length)],
        alpha: 0.3 + Math.random() * 0.5,
        delay: Math.random() * 700 + 200,
        speed: 0.8 + Math.random() * 0.6,
        twinkle: 2 + Math.random() * 5,
      });
    }

    let startTime = performance.now();
    let farewellShown = false;
    let farewellHidden = false;
    let flashDone = false;
    let completed = false;

    const pageContent = document.getElementById('pageContent');
    const topBar = document.getElementById('topBar');
    const bottomTabs = document.getElementById('bottomTabs');
    const farewellText = document.getElementById('farewellText');
    const authGate = document.getElementById('authGate');
    const overlay = document.getElementById('loginEffect');

    function animateLogout(now) {
      const elapsed = now - startTime;
      ctx.clearRect(0, 0, cw, ch);

      // Phase 1: 页面淡出 (0~800ms)
      if (elapsed < 800) {
        const p = Math.min(elapsed / 600, 1);
        const e = 1 - Math.pow(1 - p, 3);
        if (pageContent) {
          pageContent.style.opacity = 1 - e;
          pageContent.style.transform = `translateY(${-e * 25}px)`;
        }
        if (topBar) {
          topBar.style.opacity = 1 - e * 0.8;
          topBar.style.transform = `translateY(${-e * 15}px)`;
        }
        if (bottomTabs) {
          bottomTabs.style.opacity = 1 - e;
          bottomTabs.style.transform = `translateY(${e * 60}px)`;
        }
      }

      // Phase 2+3+4: 汇聚粒子
      const convergeStart = 400;
      const convergeDur = 1400;

      particles.forEach(p => {
        const lt = elapsed - p.delay - convergeStart;
        if (lt < 0) return;
        const rawT = lt / convergeDur;
        const t = Math.min(rawT, 1);
        const adjustedT = Math.min(rawT * p.speed, 1);
        const ease = 1 - Math.pow(1 - adjustedT, 4);
        const x = p.sx + (p.tx - p.sx) * ease;
        const y = p.sy + (p.ty - p.sy) * ease;
        const sizeScale = 1 - ease * 0.4;
        const s = p.size * sizeScale;
        let alpha = p.alpha;
        if (adjustedT < 0.6) {
          alpha *= 0.2 + 0.8 * (adjustedT / 0.6);
        } else {
          alpha *= Math.max(0, 1 - (adjustedT - 0.6) / 0.4);
        }
        const tw = 0.5 + 0.5 * Math.sin(lt * 0.003 * p.twinkle + p.delay);
        alpha *= tw;
        if (alpha <= 0.01) return;
        ctx.beginPath();
        ctx.arc(x, y, s, 0, Math.PI * 2);
        ctx.fillStyle = `rgba(${p.color},${alpha})`;
        ctx.fill();
        ctx.beginPath();
        ctx.arc(x, y, s * 3, 0, Math.PI * 2);
        ctx.fillStyle = `rgba(${p.color},${alpha * 0.05})`;
        ctx.fill();
      });

      // Phase 3: 告别文字 (1400~2700ms)
      if (elapsed >= 1400 && elapsed < 2700) {
        if (!farewellShown) {
          farewellShown = true;
          farewellText?.classList.add('show');
        }
        if (elapsed >= 2600 && !farewellHidden) {
          farewellHidden = true;
          farewellText?.classList.remove('show');
        }
      }

      // Phase 4: 闪光爆发 (2700~3500ms)
      if (elapsed >= 2700 && !flashDone) {
        flashDone = true;
        const burstGrad = ctx.createRadialGradient(targetX, targetY, 0, targetX, targetY, 250);
        burstGrad.addColorStop(0, 'rgba(255,255,255,0.7)');
        burstGrad.addColorStop(0.2, 'rgba(139,233,253,0.35)');
        burstGrad.addColorStop(0.5, 'rgba(201,167,255,0.12)');
        burstGrad.addColorStop(1, 'transparent');
        ctx.beginPath();
        ctx.arc(targetX, targetY, 250, 0, Math.PI * 2);
        ctx.fillStyle = burstGrad;
        ctx.fill();
        for (let i = 0; i < 30; i++) {
          const a = Math.random() * Math.PI * 2;
          const d = 20 + Math.random() * 120;
          ctx.beginPath();
          ctx.arc(targetX + Math.cos(a) * d, targetY + Math.sin(a) * d, 1 + Math.random() * 2, 0, Math.PI * 2);
          ctx.fillStyle = `rgba(255,255,255,${0.2 + Math.random() * 0.5})`;
          ctx.fill();
        }
        // 显示授权大门
        if (authGate) authGate.style.display = 'flex';
        if (bottomTabs) bottomTabs.classList.add('locked');
      }

      // 余辉 (2700~3500ms)
      if (elapsed >= 2700 && elapsed < 3500) {
        const fadeP = (elapsed - 2700) / 800;
        const fadeAlpha = Math.max(0, 1 - fadeP);
        const afterGlow = ctx.createRadialGradient(targetX, targetY, 0, targetX, targetY, 180);
        afterGlow.addColorStop(0, `rgba(255,255,255,${fadeAlpha * 0.2})`);
        afterGlow.addColorStop(0.5, `rgba(139,233,253,${fadeAlpha * 0.08})`);
        afterGlow.addColorStop(1, 'transparent');
        ctx.beginPath();
        ctx.arc(targetX, targetY, 180, 0, Math.PI * 2);
        ctx.fillStyle = afterGlow;
        ctx.fill();
        for (let i = 0; i < 10; i++) {
          const s = Math.sin(elapsed * 0.005 + i * 2) * 0.3 + 0.7;
          const a = Math.random() * Math.PI * 2 + elapsed * 0.001;
          const d = 20 + Math.random() * 100;
          ctx.beginPath();
          ctx.arc(targetX + Math.cos(a) * d, targetY + Math.sin(a) * d, 0.5 + Math.random(), 0, Math.PI * 2);
          ctx.fillStyle = `rgba(201,167,255,${fadeAlpha * s * 0.15})`;
          ctx.fill();
        }
      }

      if (elapsed < 3500) {
        requestAnimationFrame(animateLogout);
      } else if (!completed) {
        completed = true;
        ctx.clearRect(0, 0, cw, ch);
        if (overlay) overlay.style.display = 'none';
        farewellText?.classList.remove('show');
        if (pageContent) {
          pageContent.style.opacity = '';
          pageContent.style.transform = '';
          pageContent.style.transition = '';
        }
        if (topBar) {
          topBar.style.opacity = '';
          topBar.style.transform = '';
        }
        if (bottomTabs) {
          bottomTabs.style.opacity = '';
          bottomTabs.style.transform = '';
        }
        onComplete?.();
      }
    }

    setTimeout(() => requestAnimationFrame(animateLogout), 60);
  }, [show, onComplete]);

  if (!show) return null;

  return (
    <div className="login-effect show" id="loginEffect">
      <canvas ref={canvasRef} className="effect-canvas" />
    </div>
  );
}
