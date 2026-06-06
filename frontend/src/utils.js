/**
 * 公共工具函数
 */

/**
 * 格式化时间
 * @param {string|number|Date} t - 时间值
 * @param {boolean} withYear - 是否包含年份，默认 false
 * @returns {string}
 */
export function fmtTime(t, withYear = false) {
  if (!t) return '';
  const d = new Date(t);
  const y = d.getFullYear();
  const m = (d.getMonth() + 1).toString().padStart(2, '0');
  const day = d.getDate().toString().padStart(2, '0');
  const h = d.getHours().toString().padStart(2, '0');
  const mi = d.getMinutes().toString().padStart(2, '0');
  return withYear ? `${y}-${m}-${day} ${h}:${mi}` : `${m}-${day} ${h}:${mi}`;
}

/**
 * 文本截断预览
 * @param {string} text - 原始文本
 * @param {number} len - 最大长度，默认 60
 * @returns {string}
 */
export function preview(text, len = 60) {
  if (!text) return '';
  return text.length > len ? text.substring(0, len) + '...' : text;
}
