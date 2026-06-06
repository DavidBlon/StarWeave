/**
 * 全局错误边界组件
 * 防止组件渲染错误导致白屏
 */
import { Component } from 'react';

export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('ErrorBoundary caught an error:', error, errorInfo);
  }

  handleRetry = () => {
    this.setState({ hasError: false, error: null });
  };

  handleReload = () => {
    window.location.reload();
  };

  render() {
    if (this.state.hasError) {
      return (
        <div style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          height: '100vh',
          padding: '20px',
          textAlign: 'center',
          background: '#0a0a1a',
          color: 'rgba(255,255,255,0.7)',
        }}>
          <div style={{ fontSize: 48, marginBottom: 20 }}>✦</div>
          <h2 style={{
            fontSize: 18,
            fontWeight: 300,
            marginBottom: 12,
            color: 'rgba(255,255,255,0.9)',
          }}>
            星空暂时黯淡了
          </h2>
          <p style={{
            fontSize: 13,
            marginBottom: 24,
            lineHeight: 1.6,
            maxWidth: 300,
          }}>
            页面遇到了一些问题，请尝试刷新或重新开始
          </p>
          <div style={{ display: 'flex', gap: 12 }}>
            <button
              onClick={this.handleRetry}
              style={{
                padding: '10px 24px',
                borderRadius: 50,
                border: '1px solid rgba(255,255,255,0.15)',
                background: 'transparent',
                color: 'rgba(255,255,255,0.7)',
                fontSize: 13,
                cursor: 'pointer',
                fontFamily: 'inherit',
              }}
            >
              重试
            </button>
            <button
              onClick={this.handleReload}
              style={{
                padding: '10px 24px',
                borderRadius: 50,
                border: 'none',
                background: 'linear-gradient(135deg, #67e8f9, #b4a0fa)',
                color: '#1a1a2e',
                fontSize: 13,
                fontWeight: 600,
                cursor: 'pointer',
                fontFamily: 'inherit',
              }}
            >
              刷新页面
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
