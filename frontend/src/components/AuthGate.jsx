import { useState, useEffect, useCallback, useRef } from 'react';
import { loginWithPassword, register, getCaptcha } from '../api';

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

  // iOS Safari 受控输入框兼容：跟踪组合输入状态，避免重复输入
  const composingRef = useRef(false);
  const onCompositionStart = () => { composingRef.current = true; };
  const onCompositionEnd = (e, setter, sanitizer) => {
    composingRef.current = false;
    // 组合结束时手动触发一次值更新（因为组合期间 onChange 被跳过了）
    setter(sanitizer ? sanitizer(e.target.value) : e.target.value);
  };

  // 验证码状态
  const [captchaId, setCaptchaId] = useState('');
  const [captchaImage, setCaptchaImage] = useState('');
  const [captchaInput, setCaptchaInput] = useState('');

  const fetchCaptcha = useCallback(async () => {
    try {
      const res = await getCaptcha();
      if (res.code === 200 && res.data) {
        setCaptchaId(res.data.captchaId);
        setCaptchaImage(res.data.image);
        setCaptchaInput('');
      }
    } catch {
      // 验证码加载失败不阻塞用户
    }
  }, []);

  // 挂载时获取验证码
  useEffect(() => { fetchCaptcha(); }, [fetchCaptcha]);

  // 切换 tab 时刷新验证码
  const switchTab = (t) => {
    setTab(t);
    setError('');
    fetchCaptcha();
  };

  const handleLogin = async () => {
    if (!username || !password) { setError('请填写用户名和密码'); return; }
    if (!captchaInput) { setError('请填写验证码'); return; }
    if (!agreed) { setError('请先阅读并同意用户协议和隐私政策'); return; }
    setError('');
    setLoading(true);
    try {
      const res = await loginWithPassword(username, password, captchaId, captchaInput);
      if (res.code === 200) {
        onLogin(res.data);
      } else {
        setError(res.message || '登录失败');
        fetchCaptcha();
      }
    } catch (e) {
      setError(e.response?.data?.message || '连接星海失败');
      fetchCaptcha();
    }
    setLoading(false);
  };

  const handleRegister = async () => {
    const nickname = regNick.trim();
    if (!regUser || !regPass) { setError('请填写用户名和密码'); return; }
    if (!/^[a-zA-Z0-9]{1,20}$/.test(regUser)) { setError('用户名只能由英文字母和数字组成，最多20位'); return; }
    if (regPass.length < 6) { setError('密码至少需要6位'); return; }
    if (!nickname) { setError('请填写昵称'); return; }
    if (!captchaInput) { setError('请填写验证码'); return; }
    if (!agreed) { setError('请先阅读并同意用户协议和隐私政策'); return; }
    setError('');
    setLoading(true);
    try {
      const res = await register(regUser, nickname, regPass, captchaId, captchaInput);
      if (res.code === 200) {
        onLogin(res.data);
      } else {
        setError(res.message || '注册失败');
        fetchCaptcha();
      }
    } catch (e) {
      const msg = e.response?.data?.message || '连接星海失败';
      setError(msg);
      fetchCaptcha();
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
          <button className={`auth-tab ${tab === 'login' ? 'active' : ''}`} onClick={() => switchTab('login')}>登录</button>
          <button className={`auth-tab ${tab === 'register' ? 'active' : ''}`} onClick={() => switchTab('register')}>注册</button>
        </div>

        {error && <div style={{ color: '#ff6b6b', fontSize: 11, textAlign: 'center', marginBottom: 8 }}>{error}</div>}

        <div className="auth-forms">
          <div className={`auth-form ${tab === 'login' ? 'active' : ''}`}>
            <input className="auth-input" placeholder="用户名" aria-label="用户名" value={username} onChange={e => { if (!composingRef.current) setUsername(e.target.value.replace(/[^a-zA-Z0-9]/g, '').slice(0, 20)); }} onCompositionStart={onCompositionStart} onCompositionEnd={e => onCompositionEnd(e, setUsername, v => v.replace(/[^a-zA-Z0-9]/g, '').slice(0, 20))} onKeyDown={e => handleKeyDown(e, handleLogin)} maxLength={20} autoComplete="off" />
            <input className="auth-input" type="password" placeholder="密码" aria-label="密码" value={password} onChange={e => { if (!composingRef.current) setPassword(e.target.value); }} onCompositionStart={onCompositionStart} onCompositionEnd={e => onCompositionEnd(e, setPassword)} onKeyDown={e => handleKeyDown(e, handleLogin)} autoComplete="current-password" />
            <div className="captcha-row">
              <input className="auth-input captcha-input" placeholder="验证码" aria-label="验证码" value={captchaInput} onChange={e => { if (!composingRef.current) setCaptchaInput(e.target.value); }} onCompositionStart={onCompositionStart} onCompositionEnd={e => onCompositionEnd(e, setCaptchaInput)} onKeyDown={e => handleKeyDown(e, handleLogin)} maxLength={6} />
              {captchaImage && (
                <img className="captcha-img" src={captchaImage} alt="验证码" onClick={fetchCaptcha} title="点击刷新验证码" />
              )}
            </div>
            <button className="auth-btn" onClick={handleLogin} disabled={loading || !agreed}>{loading ? '进入中...' : '进入星空'}</button>
          </div>
          <div className={`auth-form ${tab === 'register' ? 'active' : ''}`}>
            <input className="auth-input" placeholder="昵称（显示名称，支持中文）" aria-label="昵称" value={regNick} onChange={e => { if (!composingRef.current) setRegNick(e.target.value.slice(0, 20)); }} onCompositionStart={onCompositionStart} onCompositionEnd={e => onCompositionEnd(e, setRegNick, v => v.slice(0, 20))} maxLength={20} autoComplete="off" />
            <input className="auth-input" placeholder="用户名（仅限英文和数字）" aria-label="用户名" value={regUser} onChange={e => { if (!composingRef.current) setRegUser(e.target.value.replace(/[^a-zA-Z0-9]/g, '').slice(0, 20)); }} onCompositionStart={onCompositionStart} onCompositionEnd={e => onCompositionEnd(e, setRegUser, v => v.replace(/[^a-zA-Z0-9]/g, '').slice(0, 20))} maxLength={20} autoComplete="off" />
            <input className="auth-input" type="password" placeholder="密码（至少6位）" aria-label="密码" value={regPass} onChange={e => { if (!composingRef.current) setRegPass(e.target.value); }} onCompositionStart={onCompositionStart} onCompositionEnd={e => onCompositionEnd(e, setRegPass)} onKeyDown={e => handleKeyDown(e, handleRegister)} autoComplete="new-password" />
            <div className="captcha-row">
              <input className="auth-input captcha-input" placeholder="验证码" aria-label="验证码" value={captchaInput} onChange={e => { if (!composingRef.current) setCaptchaInput(e.target.value); }} onCompositionStart={onCompositionStart} onCompositionEnd={e => onCompositionEnd(e, setCaptchaInput)} onKeyDown={e => handleKeyDown(e, handleRegister)} maxLength={6} />
              {captchaImage && (
                <img className="captcha-img" src={captchaImage} alt="验证码" onClick={fetchCaptcha} title="点击刷新验证码" />
              )}
            </div>
            <button className="auth-btn" onClick={handleRegister} disabled={loading || !agreed}>{loading ? '注册中...' : '加入星空'}</button>
          </div>
        </div>

        <label className="auth-agree-label">
          <input
            type="checkbox"
            checked={agreed}
            onChange={e => setAgreed(e.target.checked)}
            style={{ accentColor: '#b4a0fa', cursor: 'pointer', width: 14, height: 14, flexShrink: 0 }}
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
