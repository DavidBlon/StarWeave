import { useEffect } from 'react';

export default function Toast({ message, visible, onHide }) {
  useEffect(() => {
    if (visible) {
      const t = setTimeout(onHide, 2200);
      return () => clearTimeout(t);
    }
  }, [visible, onHide]);

  return (
    <div className={`toast ${visible ? 'show' : ''}`}>
      <div className="text">{message}</div>
    </div>
  );
}
