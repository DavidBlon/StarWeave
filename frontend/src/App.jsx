import { useState, useRef, useCallback, useEffect } from 'react';
import StarField from './components/StarField';
import AuthGate from './components/AuthGate';
import LaunchPage from './components/LaunchPage';
import CatchPage from './components/CatchPage';
import ProfilePage from './components/ProfilePage';
import MeteorDetailPage from './components/MeteorDetailPage';
import AdminPage from './components/AdminPage';
import StarMapPage from './components/StarMapPage';
import AvatarPicker from './components/AvatarPicker';
import LoginEffect from './components/LoginEffect';
import LogoutEffect from './components/LogoutEffect';
import Toast from './components/Toast';
import LegalPage from './components/LegalPage';
import MusicPlayer from './components/MusicPlayer';
import { getUser, setToken, clearToken, logout as logoutApi } from './api';

export default function App() {
  // 从 localStorage 恢复登录状态
  const [user, setUser] = useState(() => {
    try {
      const saved = localStorage.getItem('starweave_user');
      return saved ? JSON.parse(saved) : null;
    } catch { return null; }
  });
  const [loggedIn, setLoggedIn] = useState(() => !!localStorage.getItem('starweave_user'));
  const [activeTab, setActiveTab] = useState('launch');
  const [toastMsg, setToastMsg] = useState('');
  const [toastVisible, setToastVisible] = useState(false);
  const [showLoginEffect, setShowLoginEffect] = useState(false);
  const [showLogoutEffect, setShowLogoutEffect] = useState(false);
  const [showAvatarPicker, setShowAvatarPicker] = useState(false);
  const [pendingAvatarData, setPendingAvatarData] = useState(null);
  const [viewingMeteorId, setViewingMeteorId] = useState(null);
  const [starPaused, setStarPaused] = useState(false);
  const [showLegalType, setShowLegalType] = useState(null);
  const [errorMsg, setErrorMsg] = useState('');

  const loginEffectPendingRef = useRef(false);
  const logoutEffectPendingRef = useRef(false);
  // 标记：挂载状态校验是否已完结（用于阻止 mount 时期 stale 401 覆盖后续登录）
  const mountCheckDoneRef = useRef(false);
  // 标记：用户是否在当前 App 生命周期内重新登录过
  const hasFreshLoginRef = useRef(false);

  // 登录态校验：恢复登录时验证 token 是否有效
  // 注意：异步响应可能晚于用户手动登录到达，必须用 ref 保护避免覆盖
  useEffect(() => {
    if (user?.id) {
      getUser(user.id).then(res => {
        mountCheckDoneRef.current = true;
        if (res.code === 200) {
          if (!hasFreshLoginRef.current) {
            setUser(res.data);
            localStorage.setItem('starweave_user', JSON.stringify(res.data));
          }
        } else if (!hasFreshLoginRef.current) {
          setUser(null);
          setLoggedIn(false);
          localStorage.removeItem('starweave_user');
        }
      }).catch(e => {
        mountCheckDoneRef.current = true;
        console.error('验证登录态失败', e);
        if (!hasFreshLoginRef.current) {
          setUser(null);
          setLoggedIn(false);
          localStorage.removeItem('starweave_user');
        }
      });
    } else {
      mountCheckDoneRef.current = true;
    }
  }, []); // 仅在挂载时执行一次

  // 监听 auth:expired 事件（被踢下线）
  // 注意：mount 时期的 getUser 401 也会触发此事件，用 mountCheckDoneRef 保护
  useEffect(() => {
    const handler = (e) => {
      // mount 校验未完成时，401 来自旧 session 校验而非真正的踢下线
      if (!mountCheckDoneRef.current) return;
      setUser(null);
      setLoggedIn(false);
      localStorage.removeItem('starweave_user');
      clearToken();
      const msg = e.detail?.message || '账号已在其他设备登录，请重新登录';
      setErrorMsg(msg);
      setTimeout(() => setErrorMsg(''), 5000);
    };
    window.addEventListener('auth:expired', handler);
    return () => window.removeEventListener('auth:expired', handler);
  }, []);

  // Toast
  const showToast = useCallback((msg) => {
    setToastMsg(msg);
    setToastVisible(true);
  }, []);

  const hideToast = useCallback(() => {
    setToastVisible(false);
  }, []);

  // Switch tab
  const switchTab = useCallback((name) => {
    setActiveTab(name);
  }, []);

  // Login handler — userData 可以是 User 对象（旧格式）或 {user, token}（新格式）
  const handleLogin = useCallback((userData) => {
    // 兼容新旧格式：新格式有 .user 和 .token，旧格式直接是 user
    const user = userData.user || userData;
    const token = userData.token;
    console.log('[Login] userData:', userData, 'user:', user, 'nickname:', user?.nickname);
    setErrorMsg('');
    setUser(user);
    localStorage.setItem('starweave_user', JSON.stringify(user));
    if (token) setToken(token);
    setShowLoginEffect(true);
    loginEffectPendingRef.current = true;
    hasFreshLoginRef.current = true;
  }, []);

  const handleLoginEffectComplete = useCallback(() => {
    setLoggedIn(true);
    setShowLoginEffect(false);
    setActiveTab('launch');
  }, []);

  // Logout handler
  const handleLogout = useCallback(() => {
    // 先通知服务端使当前 token 失效（fire-and-forget，不阻塞退出动画）
    logoutApi().catch(e => console.error('退出登录通知失败', e));
    setStarPaused(true);
    setShowLogoutEffect(true);
    logoutEffectPendingRef.current = true;
  }, []);

  const handleLogoutEffectComplete = useCallback(() => {
    setLoggedIn(false);
    setUser(null);
    localStorage.removeItem('starweave_user');
    clearToken();
    setStarPaused(false);
    setShowLogoutEffect(false);
    setActiveTab('launch');
  }, []);

  // Profile update — refresh user state from server
  const handleUserUpdate = useCallback((updatedUser) => {
    setUser(updatedUser);
    localStorage.setItem('starweave_user', JSON.stringify(updatedUser));
    if (updatedUser?.id) {
      getUser(updatedUser.id).then(res => {
        if (res.code === 200) {
          setUser(res.data);
          localStorage.setItem('starweave_user', JSON.stringify(res.data));
        }
      }).catch(e => { console.error('刷新用户信息失败', e); });
    }
  }, []);

  // Avatar: 当 AvatarPicker 选中后，把数据暂存，由 ProfilePage 处理
  const handleAvatarSelect = useCallback((data) => {
    setShowAvatarPicker(false);
    setPendingAvatarData(data);
  }, []);

  // 清除待处理头像数据
  const clearPendingAvatar = useCallback(() => {
    setPendingAvatarData(null);
  }, []);

  // 协议/政策页面
  const handleShowPolicy = useCallback((type) => {
    setShowLegalType(type);
  }, []);

  // 查看流星详情
  const handleViewMeteor = useCallback((meteorId) => {
    setViewingMeteorId(meteorId);
  }, []);

  const handleBackFromMeteor = useCallback(() => {
    setViewingMeteorId(null);
  }, []);

  const handleMeteorDeleted = useCallback((meteorId) => {
    setViewingMeteorId(null);
  }, []);

  // Effect overlays
  const renderEffectOverlays = () => (
    <>
      {/* Welcome Text */}
      <div className="welcome-text" id="welcomeText">
        <div className="line1">欢迎来到织星海</div>
        <div className="line2">在这里放下你的行囊吧...</div>
      </div>
      {/* Farewell Text */}
      <div className="farewell-text" id="farewellText">
        <div className="line">辛苦啦，去好好生活吧，<br />群星会一直守候你。</div>
      </div>
      {/* Login Effect Canvas */}
      {showLoginEffect && (
        <LoginEffect
          show={showLoginEffect}
          onComplete={handleLoginEffectComplete}
        />
      )}
      {/* Logout Effect Canvas */}
      {showLogoutEffect && (
        <LogoutEffect
          show={showLogoutEffect}
          onComplete={handleLogoutEffectComplete}
        />
      )}
    </>
  );

  const renderPage = () => {
    if (!loggedIn) return null;
    // 流星详情页覆盖所有页面
    if (viewingMeteorId) {
      return (
        <MeteorDetailPage
          meteorId={viewingMeteorId}
          user={user}
          onBack={handleBackFromMeteor}
          onShowToast={showToast}
          onDeleted={handleMeteorDeleted}
        />
      );
    }
    switch (activeTab) {
      case 'launch':
        return <LaunchPage user={user} onShowToast={showToast} onHideToast={hideToast} onViewMeteor={handleViewMeteor} />;
      case 'catch':
        return <CatchPage user={user} onShowToast={showToast} onViewMeteor={handleViewMeteor} />;
      case 'starmap':
        return <StarMapPage onShowToast={showToast} />;
      case 'profile':
        return (
          <ProfilePage
            user={user}
            onLogout={handleLogout}
            onOpenAvatar={() => setShowAvatarPicker(true)}
            onUserUpdate={handleUserUpdate}
            pendingAvatarData={pendingAvatarData}
            onClearPendingAvatar={clearPendingAvatar}
            onShowToast={showToast}
            onShowPolicy={handleShowPolicy}
          />
        );
      case 'admin':
        return <AdminPage user={user} onShowToast={showToast} />;
      default:
        return null;
    }
  };

  return (
    <>
      <StarField paused={starPaused} />
      <Toast message={toastMsg} visible={toastVisible} onHide={hideToast} />
      <MusicPlayer />

      {/* 红色错误提示（被踢下线等） */}
      {errorMsg && (
        <div className="error-toast" role="alert">
          <span className="error-toast-icon">⚠</span>
          <span>{errorMsg}</span>
        </div>
      )}

      <div className="app-container">
        {/* Top Bar — 登录后隐藏 */}
        {!loggedIn && (
          <div className="top-bar" id="topBar">
            <h1>流星树洞</h1>
            <p>让烦恼化作流星，消失在星河里</p>
          </div>
        )}

        <div className="page-content" id="pageContent" style={loggedIn ? { paddingTop: 50 } : undefined}>
          {/* Auth Gate */}
          {!loggedIn && (
            <div id="authGate" style={{ height: '100%' }}>
              <AuthGate onLogin={handleLogin} onShowPolicy={handleShowPolicy} />
            </div>
          )}

          {/* Pages */}
          {renderPage()}
        </div>

        {/* Bottom Tabs */}
        <nav className={`bottom-tabs ${!loggedIn ? 'locked' : ''}`} id="bottomTabs" role="navigation" aria-label="主导航">
          <button className={`tab-item ${activeTab === 'launch' ? 'active' : ''}`} data-tab="launch" onClick={() => switchTab('launch')}>
            <span className="tab-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 1 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>
              </svg>
            </span>
            <span className="tab-label">写流星</span>
          </button>
          <button className={`tab-item ${activeTab === 'catch' ? 'active' : ''}`} data-tab="catch" onClick={() => switchTab('catch')}>
            <span className="tab-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8z"/><path d="M12 2v6"/><path d="M12 16v6"/><path d="M2 12h6"/><path d="M16 12h6"/>
              </svg>
            </span>
            <span className="tab-label">捞流星</span>
          </button>
          <button className={`tab-item ${activeTab === 'starmap' ? 'active' : ''}`} data-tab="starmap" onClick={() => switchTab('starmap')}>
            <span className="tab-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
              </svg>
            </span>
            <span className="tab-label">星图</span>
          </button>
          <button className={`tab-item ${activeTab === 'profile' ? 'active' : ''}`} data-tab="profile" onClick={() => switchTab('profile')}>
            <span className="tab-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 3.6-7 8-7s8 3 8 7"/>
              </svg>
            </span>
            <span className="tab-label">我的</span>
          </button>
          {/* 管理员审核入口 — 仅对管理员可见 */}
          {user?.isAdmin && (
            <button className={`tab-item ${activeTab === 'admin' ? 'active' : ''}`} data-tab="admin" onClick={() => switchTab('admin')}>
              <span className="tab-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                  <path d="M9 12l2 2 4-4"/>
                </svg>
              </span>
              <span className="tab-label">审核</span>
            </button>
          )}
        </nav>
      </div>

      {/* Effect overlays */}
      {renderEffectOverlays()}

      {/* 用户协议 / 隐私政策 */}
      {showLegalType && (
        <LegalPage
          defaultTab={showLegalType}
          onClose={() => setShowLegalType(null)}
        />
      )}

      {/* Avatar Picker Modal */}
      <AvatarPicker
        show={showAvatarPicker}
        onClose={() => setShowAvatarPicker(false)}
        onSelect={handleAvatarSelect}
      />
    </>
  );
}
