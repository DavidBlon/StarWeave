/**
 * 自定义确认弹窗 — 替代 window.confirm / window.alert
 * 用法：
 *   <ConfirmModal show={show} title="确认删除" message="确定要删除吗？" onConfirm={fn} onCancel={fn} />
 *   <ConfirmModal show={show} message="提示信息" />  // 仅提示，无取消按钮
 */
import { useEffect, useRef } from 'react';

export default function ConfirmModal({
  show,
  title = '确认',
  message,
  confirmText = '确定',
  cancelText = '取消',
  danger = false,
  onConfirm,
  onCancel,
}) {
  const confirmRef = useRef(null);

  useEffect(() => {
    if (show && confirmRef.current) {
      confirmRef.current.focus();
    }
  }, [show]);

  useEffect(() => {
    if (!show) return;
    const handleKey = (e) => {
      if (e.key === 'Escape') onCancel?.();
      if (e.key === 'Enter') onConfirm?.();
    };
    window.addEventListener('keydown', handleKey);
    return () => window.removeEventListener('keydown', handleKey);
  }, [show, onConfirm, onCancel]);

  if (!show) return null;

  return (
    <div className="confirm-overlay" onClick={onCancel}>
      <div className="confirm-modal" role="dialog" aria-modal="true" aria-labelledby={title ? 'confirm-title' : undefined} onClick={(e) => e.stopPropagation()}>
        {title && <div className="confirm-title" id="confirm-title">{title}</div>}
        <div className="confirm-message">{message}</div>
        <div className="confirm-actions">
          {onCancel && (
            <button className="confirm-btn confirm-btn-cancel" onClick={onCancel}>
              {cancelText}
            </button>
          )}
          <button
            ref={confirmRef}
            className={`confirm-btn ${danger ? 'confirm-btn-danger' : 'confirm-btn-primary'}`}
            onClick={onConfirm}
          >
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
}
