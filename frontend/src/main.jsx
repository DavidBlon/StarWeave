import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'
import './styles/global.css'

// 移动端浏览器底部工具栏遮挡修复
// 100vh 不会扣除浏览器地址栏/工具栏的高度，导致底部内容被遮挡
// 通过 JS 动态设置真实视口高度来解决
function applyViewportHeight() {
  const vh = window.innerHeight * 0.01
  document.documentElement.style.setProperty('--vh', `${vh}px`)
}
window.addEventListener('resize', applyViewportHeight)
window.addEventListener('DOMContentLoaded', applyViewportHeight)
applyViewportHeight()

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
)
