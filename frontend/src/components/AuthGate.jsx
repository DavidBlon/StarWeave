import { useState } from 'react';
import { loginWithPassword, register } from '../api';

export default function AuthGate({ onLogin, onShowPolicy }) {
  const [tab, setTab] = useState('login');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [regNick, setRegNick] = useState('');
  const [regUser, setRegUser] = useState('');
  const [regPass, setRegPass] = useState('');
  const [agreed, setAgreed] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleLogin = async () => {
    if (!username || !password) { setError('请填写用户名和密码'); return; }
    if (!agreed) { setError('请先阅读并同意用户协议和隐私政策'); return; }
    setError('');
    setLoading(true);
    try {
      const res = await loginWithPassword(username, password);
      if (res.code === 200) {
        onLogin(res.data);
      } else {
        setError(res.message || '登录失败');
      }
    } catch (e) {
      const msg = e.response?.data?.message || '连接星海失败';
      setError(msg);
    }
    setLoading(false);
  };

  const handleRegister = async () => {
    const nickname = regNick.trim() || '';
    if (!regUser || !regPass) { setError('请填写用户名和密码'); return; }
    if (!agreed) { setError('请先阅读并同意用户协议和隐私政策'); return; }
    setError('');
    setLoading(true);
    try {
      const res = await register(regUser, nickname, regPass);
      if (res.code === 200) {
        onLogin(res.data);
      } else {
        setError(res.message || '注册失败');
      }
    } catch (e) {
      setError(e.response?.data?.message || '连接星海失败');
    }
    setLoading(false);
  };

  const handleKeyDown = (e, action) => {
    if (e.key === 'Enter') action();
  };

  return (
    <div className="auth-gate">
      <div className="gate-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1" strokeLinecap="round" strokeLinejoin="round">
          <path d="M12 2a10 10 0 1 0 10 10"/><path d="M12 12 3 3"/><path d="M16 6h4v4"/><path d="M12 6a6 6 0 1 1-6 6"/><circle cx="12" cy="12" r="1" fill="currentColor" stroke="none"/>
        </svg>
      </div>
      <div className="gate-title">流星树洞</div>

      <div className="auth-box">
        <div className="auth-tabs">
          <button className={`auth-tab ${tab === 'login' ? 'active' : ''}`} onClick={() => setTab('login')}>登录</button>
          <button className={`auth-tab ${tab === 'register' ? 'active' : ''}`} onClick={() => setTab('register')}>注册</button>
        </div>

        {error && <div style={{ color: '#ff6b6b', fontSize: 11, textAlign: 'center', marginBottom: 8 }}>{error}</div>}

        <div className="auth-forms">
          <div className={`auth-form ${tab === 'login' ? 'active' : ''}`}>
            <input className="auth-input" placeholder="用户名" value={username} onChange={e => setUsername(e.target.value)} onKeyDown={e => handleKeyDown(e, handleLogin)} autoComplete="off" />
            <input className="auth-input" type="password" placeholder="密码" value={password} onChange={e => setPassword(e.target.value)} onKeyDown={e => handleKeyDown(e, handleLogin)} autoComplete="off" />
            <button className="auth-btn" onClick={handleLogin} disabled={loading || !agreed}>{loading ? '进入中...' : '进入星空'}</button>
          </div>
          <div className={`auth-form ${tab === 'register' ? 'active' : ''}`}>
            <input className="auth-input" placeholder="名字（显示用，可留空）" value={regNick} onChange={e => setRegNick(e.target.value)} autoComplete="off" />
            <input className="auth-input" placeholder="用户名（登录用）" value={regUser} onChange={e => setRegUser(e.target.value)} autoComplete="off" />
            <input className="auth-input" type="password" placeholder="密码" value={regPass} onChange={e => setRegPass(e.target.value)} onKeyDown={e => handleKeyDown(e, handleRegister)} autoComplete="off" />
            <button className="auth-btn" onClick={handleRegister} disabled={loading || !agreed}>{loading ? '注册中...' : '加入星空'}</button>
          </div>
        </div>

        <label className="auth-agree-label">
          <input
            type="checkbox"
            checked={agreed}
            onChange={e => setAgreed(e.target.checked)}
            style={{ accentColor: '#c9a7ff', cursor: 'pointer', width: 14, height: 14, flexShrink: 0 }}
          />
          <span className="auth-agree-text">我已阅读并同意</span>
          <span
            className="auth-agree-link"
            onClick={e => { e.preventDefault(); e.stopPropagation(); onShowPolicy && onShowPolicy('agreement'); }}
          >《用户协议》</span>
          <span className="auth-agree-text">和</span>
          <span
            className="auth-agree-link"
            onClick={e => { e.preventDefault(); e.stopPropagation(); onShowPolicy && onShowPolicy('policy'); }}
          >《隐私政策》</span>
        </label>
      </div>
    </div>
  );
}
