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
    audio.play().then(() => setPlaying(true)).catch(e => {
      console.warn('背景音乐自动播放被阻止（需用户交互触发）', e);
    });
  }, []);

  const toggle = () => {
    const audio = audioRef.current;
    if (!audio) return;
    if (playing) {
      audio.pause();
      setPlaying(false);
    } else {
      audio.play().then(() => setPlaying(true)).catch(e => {
        console.warn('播放背景音乐失败', e);
      });
    }
  };

  return (
    <>
      <audio ref={audioRef} src="/背景音乐.mp3" preload="auto" />
      <button
        className={`music-player ${playing ? 'playing' : ''}`}
        onClick={toggle}
        aria-label={playing ? '暂停背景音乐' : '播放背景音乐'}
        type="button"
      >
        {/* 唱片图标 */}
        <svg viewBox="0 0 24 24" width={20} height={20} aria-hidden="true">
          <circle cx="12" cy="12" r="10" fill="none" stroke="rgba(180,160,250,0.5)" strokeWidth="0.8" />
          <circle cx="12" cy="12" r="7" fill="none" stroke="rgba(180,160,250,0.2)" strokeWidth="0.5" />
          <circle cx="12" cy="12" r="4" fill="none" stroke="rgba(180,160,250,0.35)" strokeWidth="0.6" />
          <circle cx="12" cy="12" r="1.5" fill="rgba(180,160,250,0.6)" />
          {/* 音符 */}
          {!playing && (
            <path d="M9.5 7.5v7a2 2 0 1 1-1.5-1.94V9l6-1.5v5.5a2 2 0 1 1-1.5-1.94V7.5L9.5 7.5z"
              fill="none" stroke="rgba(180,160,250,0.5)" strokeWidth="0.8" strokeLinecap="round" strokeLinejoin="round" />
          )}
        </svg>
      </button>
    </>
  );
}
