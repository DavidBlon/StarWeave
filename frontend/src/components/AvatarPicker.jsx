import { useState, useRef, useEffect } from 'react';

const AVATARS = ['✦', '★', '✧', '🌙', '☀', '🌊', '🌸', '🍃', '🌺', '🦋', '🐚', '⭐', '🌷', '🌿', '🍀', '🌈', '💫', '✨', '🕊', '🌻', '🌙'];

const AVATAR_COLORS = [
  ['#67e8f9', '#b4a0fa'],
  ['#ff9ff3', '#f368e0'],
  ['#ffd93d', '#ff9a3c'],
  ['#6bcb77', '#2d6a4f'],
  ['#ff6b6b', '#ee5a24'],
  ['#a29bfe', '#6c5ce7'],
  ['#fd79a8', '#e84393'],
  ['#55efc4', '#00b894'],
];

export default function AvatarPicker({ show, onClose, onSelect }) {
  const [selectedAvatar, setSelectedAvatar] = useState(0);
  const [selectedColor, setSelectedColor] = useState(0);
  const [uploading, setUploading] = useState(false);
  const fileRef = useRef(null);
  const imageDataRef = useRef(null); // 保留 data URL 的引用

  useEffect(() => {
    if (!show) return;
    setSelectedAvatar(0);
    setSelectedColor(0);
    imageDataRef.current = null;
    setUploading(false);
  }, [show]);

  const handleUpload = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (ev) => {
      imageDataRef.current = ev.target.result;
      // 传递 file 对象用于 multipart 上传，url 用于本地预览
      onSelect({ type: 'image', url: ev.target.result, file });
      onClose();
    };
    reader.readAsDataURL(file);
    e.target.value = '';
  };

  const applyAvatar = async () => {
    const char = AVATARS[selectedAvatar];
    const colors = AVATAR_COLORS[selectedColor];
    onSelect({
      type: 'emoji',
      url: null,
      bg: `linear-gradient(135deg, ${colors[0]}, ${colors[1]})`,
      borderColor: colors[0] + '44',
      char,
    });
    onClose();
  };

  if (!show) return null;

  return (
    <div className="modal-overlay show" onClick={e => { if (e.target === e.currentTarget) onClose(); }}>
      <div className="modal-sheet">
        <div className="modal-handle"></div>
        <div className="modal-title">选择头像</div>

        <div className="avatar-upload-area" onClick={() => fileRef.current?.click()}>
          <input type="file" ref={fileRef} accept="image/*" hidden onChange={handleUpload} />
          <div className="upload-btn">{uploading ? '上传中...' : '上传图片'}</div>
          <div className="upload-hint">支持 JPG / PNG，将上传到服务器</div>
        </div>

        <div className="avatar-divider"><span>或选择图标</span></div>

        <div className="avatar-grid">
          {AVATARS.map((a, i) => (
            <div
              key={i}
              className={`avatar-option ${i === selectedAvatar ? 'selected' : ''}`}
              onClick={() => setSelectedAvatar(i)}
            >
              {a}
            </div>
          ))}
        </div>

        <div className="color-row">
          {AVATAR_COLORS.map((c, i) => (
            <div
              key={i}
              className={`color-dot ${i === selectedColor ? 'selected' : ''}`}
              style={{ background: `linear-gradient(135deg, ${c[0]}, ${c[1]})` }}
              onClick={() => setSelectedColor(i)}
            />
          ))}
        </div>

        <div style={{ textAlign: 'center', marginTop: 14 }}>
          <button className="btn-primary" style={{ padding: '7px 28px', fontSize: 12 }} onClick={applyAvatar}>
            确认选择
          </button>
        </div>
      </div>
    </div>
  );
}
