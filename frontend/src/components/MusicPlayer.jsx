import { useState, useRef, useEffect } from 'react';

export default function MusicPlayer() {
  const audioRef = useRef(null);
  const [playing, setPlaying] = useState(false);

  useEffect(() => {
    const audio = audioRef.current;
    if (!audio) return;
    audio.loop = true;
    audio.volume = 0.35;
    // 尝试自动播放，浏览器可能会阻止
    audio.play().then(() => setPlaying(true)).catch(() => {});
  }, []);

  const toggle = () => {
    const audio = audioRef.current;
    if (!audio) return;
    if (playing) {
      audio.pause();
      setPlaying(false);
    } else {
      audio.play().then(() => setPlaying(true)).catch(() => {});
    }
  };

  return (
    <>
      <audio ref={audioRef} src="/背景音乐.mp3" preload="auto" />
      <div className={`music-player ${playing ? 'playing' : ''}`} onClick={toggle}>
        {/* 唱片图标 */}
        <svg viewBox="0 0 24 24" width={20} height={20}>
          <circle cx="12" cy="12" r="10" fill="none" stroke="rgba(201,167,255,0.5)" strokeWidth="0.8" />
          <circle cx="12" cy="12" r="7" fill="none" stroke="rgba(201,167,255,0.2)" strokeWidth="0.5" />
          <circle cx="12" cy="12" r="4" fill="none" stroke="rgba(201,167,255,0.35)" strokeWidth="0.6" />
          <circle cx="12" cy="12" r="1.5" fill="rgba(201,167,255,0.6)" />
          {/* 音符 */}
          {!playing && (
            <path d="M9.5 7.5v7a2 2 0 1 1-1.5-1.94V9l6-1.5v5.5a2 2 0 1 1-1.5-1.94V7.5L9.5 7.5z"
              fill="none" stroke="rgba(201,167,255,0.5)" strokeWidth="0.8" strokeLinecap="round" strokeLinejoin="round" />
          )}
        </svg>
      </div>
    </>
  );
}
