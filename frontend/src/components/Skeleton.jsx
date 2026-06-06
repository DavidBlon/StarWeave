/**
 * 通用骨架屏组件
 * 用法：<Skeleton variant="card" /> 或 <Skeleton lines={3} />
 */

const VARIANT_STYLES = {
  text: { height: 14, borderRadius: 6 },
  title: { height: 20, borderRadius: 6, width: '60%' },
  circle: { width: 48, height: 48, borderRadius: '50%' },
  avatar: { width: 64, height: 64, borderRadius: '50%' },
  card: { height: 120, borderRadius: 18 },
  button: { height: 38, borderRadius: 50, width: 100 },
};

export default function Skeleton({ variant = 'text', lines = 1, style, className = '' }) {
  const base = VARIANT_STYLES[variant] || VARIANT_STYLES.text;

  if (lines > 1) {
    return (
      <div className={`skeleton-group ${className}`} style={style}>
        {Array.from({ length: lines }, (_, i) => (
          <div
            key={i}
            className="skeleton-block"
            style={{
              ...base,
              width: i === lines - 1 ? '70%' : (base.width || '100%'),
            }}
          />
        ))}
      </div>
    );
  }

  return (
    <div
      className={`skeleton-block ${className}`}
      style={{ ...base, ...style }}
    />
  );
}
