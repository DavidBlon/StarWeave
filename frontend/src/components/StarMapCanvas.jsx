import { useRef, useEffect, useImperativeHandle, forwardRef, useCallback } from 'react';

// ===== 简单的 hash 函数 =====
function hashCode(str) {
  let h = 0;
  for (let i = 0; i < str.length; i++) {
    const c = str.charCodeAt(i);
    h = ((h << 5) - h) + c;
    h = h & h;
  }
  return Math.abs(h);
}

// ===== 基于种子的伪随机（Mulberry32） =====
function mulberry32(a) {
  return function () {
    a |= 0; a = a + 0x6D2B79F5 | 0;
    let t = Math.imul(a ^ a >>> 15, 1 | a);
    t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t;
    return ((t ^ t >>> 14) >>> 0) / 4294967296;
  };
}

// ===== HSLA 工具 =====
function hsla(h, s, l, a) {
  return `hsla(${h},${s}%,${l}%,${a})`;
}

// ===== 预计算星图数据 =====
function computeStarMap(text, W, H) {
  const seed = hashCode(text);
  const rand = mulberry32(seed);

  // 星云
  const nebulaHue = 220 + rand() * 60;
  const nebulaCount = 2 + Math.floor(rand() * 3);
  const nebulae = [];
  for (let i = 0; i < nebulaCount; i++) {
    nebulae.push({
      x: rand() * W,
      y: rand() * H * 0.6,
      r: 150 + rand() * 250,
      hue: nebulaHue + rand() * 30,
      sat: 40 + rand() * 30,
      lit: 40 + rand() * 20,
      alpha: 0.03 + rand() * 0.04,
    });
  }

  // 主星星
  const starCount = 100 + Math.floor(rand() * 100);
  const stars = [];
  for (let i = 0; i < starCount; i++) {
    stars.push({
      x: rand() * W,
      y: rand() * H,
      size: 0.3 + rand() * 2.2,
      brightness: 0.15 + rand() * 0.5,
      hue: 200 + rand() * 60,
      phase: rand() * Math.PI * 2,
      twinkleSpeed: 0.5 + rand() * 1.5,
    });
  }

  // 星座主星
  const brightStars = stars
    .map((s, i) => ({ ...s, idx: i }))
    .sort((a, b) => b.size - a.size)
    .slice(0, 12 + Math.floor(rand() * 8));

  // 文字参数
  const displayText = text.length > 20 ? text.slice(0, 20) + '…' : text;
  const textHue = 200 + (seed % 60);
  const textHue2 = (textHue + 30 + (seed % 40)) % 360;
  const textY = 0.28 + (seed % 13) * 0.006;
  const glowIntensity = 0.08 + (seed % 7) * 0.01;

  // 专属星阵
  const constSeed = seed + 9999;
  const constRand = mulberry32(constSeed);
  const constCount = 8 + (seed % 12);
  const constStars = [];
  for (let i = 0; i < constCount; i++) {
    const angle = constRand() * Math.PI * 2;
    const dist = 90 + constRand() * 140;
    constStars.push({
      x: W / 2 + Math.cos(angle) * dist,
      y: H * textY + Math.sin(angle) * dist * 0.7,
      r: 0.6 + constRand() * 1.8,
      phase: constRand() * Math.PI * 2,
      speed: 0.5 + constRand() * 1.5,
    });
  }

  const fingerprint = seed.toString(16).toUpperCase().padStart(8, '0');

  return { seed, nebulaHue, nebulaCount, nebulae, stars, brightStars, displayText, textHue, textHue2, textY, glowIntensity, constSeed, constStars, fingerprint };
}

// ===== 绘制一帧 =====
function drawFrame(ctx, data, W, H, time) {
  const { seed, nebulaHue, nebulaCount, nebulae, stars, brightStars, displayText, textHue, textHue2, textY, glowIntensity, constSeed, constStars, fingerprint } = data;
  const t = time || 0;

  ctx.clearRect(0, 0, W, H);

  // 背景
  const bg = ctx.createRadialGradient(W / 2, H / 2, 50, W / 2, H / 2, W * 0.625);
  bg.addColorStop(0, '#0d0d24');
  bg.addColorStop(0.5, '#0a0a1a');
  bg.addColorStop(1, '#060610');
  ctx.fillStyle = bg;
  ctx.fillRect(0, 0, W, H);

  // 星云
  nebulae.forEach(n => {
    const ng = ctx.createRadialGradient(n.x, n.y, 0, n.x, n.y, n.r);
    ng.addColorStop(0, hsla(n.hue, n.sat, n.lit, n.alpha));
    ng.addColorStop(1, 'transparent');
    ctx.fillStyle = ng;
    ctx.fillRect(0, 0, W, H);
  });

  // 星座连线
  ctx.lineWidth = 0.5;
  for (let i = 0; i < brightStars.length; i++) {
    for (let j = i + 1; j < brightStars.length; j++) {
      const dx = brightStars[i].x - brightStars[j].x;
      const dy = brightStars[i].y - brightStars[j].y;
      const dist = Math.sqrt(dx * dx + dy * dy);
      const pairSeed = (seed + i * 31 + j * 17) % 100;
      if (dist < 160 && pairSeed > 30) {
        const alpha = (1 - dist / 160) * 0.15;
        ctx.beginPath();
        ctx.moveTo(brightStars[i].x, brightStars[i].y);
        ctx.lineTo(brightStars[j].x, brightStars[j].y);
        ctx.strokeStyle = `rgba(103,232,249,${alpha})`;
        ctx.stroke();
      }
    }
  }

  // 星星
  stars.forEach(s => {
    const twinkle = 0.6 + 0.4 * Math.sin(t * 0.001 * s.twinkleSpeed + s.phase);
    const alpha = s.brightness * twinkle;
    const r = s.size;

    if (r > 1.0) {
      ctx.beginPath();
      ctx.arc(s.x, s.y, r * 4, 0, Math.PI * 2);
      ctx.fillStyle = `rgba(200,220,255,${alpha * 0.04})`;
      ctx.fill();
    }

    ctx.beginPath();
    ctx.arc(s.x, s.y, r, 0, Math.PI * 2);
    ctx.fillStyle = hsla(s.hue, 30, 70 + 20 * twinkle, alpha);
    ctx.fill();

    if (r > 1.8) {
      ctx.strokeStyle = `rgba(255,255,255,${alpha * 0.15})`;
      ctx.lineWidth = 0.5;
      for (let a = 0; a < 4; a++) {
        const angle = a * Math.PI / 4 + Math.PI / 8;
        ctx.beginPath();
        ctx.moveTo(s.x - Math.cos(angle) * r * 4, s.y - Math.sin(angle) * r * 4);
        ctx.lineTo(s.x + Math.cos(angle) * r * 4, s.y + Math.sin(angle) * r * 4);
        ctx.stroke();
      }
    }
  });

  // 文字光晕
  function drawGlowText(c, txt, cx, cy, size) {
    c.textAlign = 'center';
    c.textBaseline = 'middle';
    c.font = `200 ${size}px -apple-system, "PingFang SC", "Noto Sans SC", sans-serif`;
    c.fillStyle = `hsla(${textHue2},60%,60%,${glowIntensity * 0.5})`;
    c.fillText(txt, cx, cy + 3);
    c.fillStyle = `hsla(${textHue},70%,70%,${glowIntensity * 1.2})`;
    c.fillText(txt, cx, cy + 1);
    c.fillStyle = `rgba(255,255,255,${0.15 + glowIntensity * 0.5})`;
    c.fillText(txt, cx, cy);
    c.fillStyle = `rgba(255,255,255,${0.55 + glowIntensity * 0.8})`;
    c.fillText(txt, cx, cy);
  }

  drawGlowText(ctx, '「' + displayText + '」', W / 2, H * textY, 28);

  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';
  ctx.font = '200 12px -apple-system, "PingFang SC", "Noto Sans SC", sans-serif';
  ctx.fillStyle = `hsla(${textHue},50%,60%,0.12)`;
  ctx.fillText('✦ 织星海 · 一段心事一片星空 ✦', W / 2, H * 0.7);

  // 专属星阵连线
  const t2 = t * 0.0005;
  ctx.lineWidth = 0.4;
  for (let i = 0; i < constStars.length; i++) {
    for (let j = i + 1; j < constStars.length; j++) {
      const dx = constStars[i].x - constStars[j].x;
      const dy = constStars[i].y - constStars[j].y;
      const dist = Math.sqrt(dx * dx + dy * dy);
      if (dist < 100) {
        const pairKey = (constSeed + i * 7 + j * 13) % 100;
        if (pairKey > 35) {
          ctx.beginPath();
          ctx.moveTo(constStars[i].x, constStars[i].y);
          ctx.lineTo(constStars[j].x, constStars[j].y);
          ctx.strokeStyle = `rgba(103,232,249,${(1 - dist / 100) * 0.2})`;
          ctx.stroke();
        }
      }
    }
  }
  constStars.forEach(s => {
    const tw = 0.5 + 0.5 * Math.sin(t2 * s.speed + s.phase);
    ctx.beginPath();
    ctx.arc(s.x, s.y, s.r, 0, Math.PI * 2);
    ctx.fillStyle = `hsla(${textHue},50%,70%,${0.2 + tw * 0.3})`;
    ctx.fill();
    if (s.r > 1.2) {
      ctx.beginPath();
      ctx.arc(s.x, s.y, s.r * 3, 0, Math.PI * 2);
      ctx.fillStyle = `hsla(${textHue},50%,70%,${tw * 0.06})`;
      ctx.fill();
    }
  });

  // 指纹
  ctx.textAlign = 'right';
  ctx.textBaseline = 'bottom';
  ctx.font = '9px monospace';
  ctx.fillStyle = 'rgba(255,255,255,0.04)';
  ctx.fillText('#' + fingerprint, W - 10, H - 6);
}

// ===== 导出静态 PNG（不带动画） =====
function exportStaticPNG(data, W, H) {
  const scale = 3; // 3倍分辨率，导出2K清晰度 (2400x1800)
  const offscreen = document.createElement('canvas');
  offscreen.width = W * scale;
  offscreen.height = H * scale;
  const offCtx = offscreen.getContext('2d');
  offCtx.scale(scale, scale);
  drawFrame(offCtx, data, W, H, 0);
  return offscreen.toDataURL('image/png');
}

// ===== React 组件 =====
const StarMapCanvas = forwardRef(function StarMapCanvas({ text, width = 800, height = 600, style }, ref) {
  const canvasRef = useRef(null);
  const dataRef = useRef(null);
  const animRef = useRef(null);

  const draw = useCallback(() => {
    if (!canvasRef.current || !dataRef.current) return;
    const ctx = canvasRef.current.getContext('2d');
    const start = performance.now();
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    function loop(now) {
      drawFrame(ctx, dataRef.current, width, height, now - start);
      if (!prefersReducedMotion) {
        animRef.current = requestAnimationFrame(loop);
      }
    }
    animRef.current = requestAnimationFrame(loop);
  }, [width, height]);

  // 当 text 变化时重新计算并绘制
  useEffect(() => {
    if (!text || !canvasRef.current) return;
    if (animRef.current) cancelAnimationFrame(animRef.current);
    dataRef.current = computeStarMap(text, width, height);
    draw();
    return () => {
      if (animRef.current) cancelAnimationFrame(animRef.current);
    };
  }, [text, width, height, draw]);

  // 暴露 exportPNG 给父组件
  useImperativeHandle(ref, () => ({
    exportPNG: () => {
      if (!dataRef.current) return null;
      return exportStaticPNG(dataRef.current, width, height);
    },
  }));

  return (
    <canvas
      ref={canvasRef}
      width={width}
      height={height}
      style={{
        display: 'block',
        width: '100%',
        height: 'auto',
        aspectRatio: `${width} / ${height}`,
        borderRadius: 16,
        ...style,
      }}
    />
  );
});

export default StarMapCanvas;
