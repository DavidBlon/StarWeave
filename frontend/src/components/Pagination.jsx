import { useState, useEffect } from 'react';

const PAGE_SIZE = 8;

export default function Pagination({ items, pageSize = PAGE_SIZE, renderItem, emptyIcon, emptyText }) {
  const [page, setPage] = useState(1);
  const totalPages = Math.max(1, Math.ceil(items.length / pageSize));
  const currentPage = Math.min(page, totalPages);
  const paged = items.slice((currentPage - 1) * pageSize, currentPage * pageSize);

  // 数据变化时修正页码
  useEffect(() => {
    if (page > totalPages) setPage(totalPages);
  }, [totalPages, page]);

  if (items.length === 0) {
    return (
      <div style={{ textAlign: 'center', padding: '32px 20px', fontSize: 12, color: 'rgba(255,255,255,0.35)', letterSpacing: 0.5 }}>
        {emptyIcon && <div style={{ fontSize: 28, marginBottom: 10, opacity: 0.6 }}>{emptyIcon}</div>}
        {emptyText || '暂无数据'}
      </div>
    );
  }

  return (
    <>
      {paged.map((item, idx) => renderItem(item, (currentPage - 1) * pageSize + idx))}
      {totalPages > 1 && (
        <div className="detail-pagination">
          <button
            className="page-btn"
            disabled={currentPage <= 1}
            onClick={() => setPage(p => p - 1)}
          >‹</button>
          <span className="page-info">{currentPage} / {totalPages}</span>
          <button
            className="page-btn"
            disabled={currentPage >= totalPages}
            onClick={() => setPage(p => p + 1)}
          >›</button>
        </div>
      )}
    </>
  );
}
