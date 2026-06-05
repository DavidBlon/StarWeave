import { useEffect, useRef } from 'react';

const STAR_COUNT = 100;

class Star {
  constructor(canvas) {
    this.canvas = canvas;
    this.reset(true);
  }
  reset(init) {
    this.x = Math.random() * this.canvas.width;
    this.y = Math.random() * this.canvas.height;
    this.size = Math.random() * 1.6 + 0.3;
    this.o = Math.random() * 0.45 + 0.1;
    this.sx = (Math.random() - 0.5) * 0.08;
    this.sy = (Math.random() - 0.5) * 0.08;
    this.tw = Math.random() * 0.005 + 0.002;
    this.ph = Math.random() * Math.PI * 2;
    if (!init) {
      this.x = Math.random() * this.canvas.width;
      this.y = this.canvas.height + 2;
    }
  }
  update(t) {
    this.x += this.sx;
    this.y += this.sy;
    this.ph += this.tw;
    if (this.x < -5 || this.x > this.canvas.width + 5 ||
        this.y < -5 || this.y > this.canvas.height + 5) this.reset(false);
    return Math.sin(this.ph) * 0.2 + 0.8;
  }
  draw(ctx, a) {
    const o = this.o * a;
    ctx.beginPath();
    ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
    ctx.fillStyle = `rgba(200,210,255,${o})`;
    ctx.fill();
    if (this.size > 0.8) {
      ctx.beginPath();
      ctx.arc(this.x, this.y, this.size * 3, 0, Math.PI * 2);
      ctx.fillStyle = `rgba(139,233,253,${o * 0.05})`;
      ctx.fill();
    }
  }
}

export default function StarField({ paused = false }) {
  const canvasRef = useRef(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    let animId;

    const resize = () => {
      canvas.width = window.innerWidth;
      canvas.height = window.innerHeight;
    };
    resize();
    window.addEventListener('resize', resize);

    const stars = [];
    for (let i = 0; i < STAR_COUNT; i++) {
      stars.push(new Star(canvas));
    }

    function animate(t) {
      if (!ctx) return;
      if (!paused) {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        stars.forEach(s => s.draw(ctx, s.update(t)));
      }
      animId = requestAnimationFrame(animate);
    }
    animId = requestAnimationFrame(animate);

    return () => {
      cancelAnimationFrame(animId);
      window.removeEventListener('resize', resize);
    };
  }, [paused]);

  return <canvas ref={canvasRef} id="star-canvas" />;
}
