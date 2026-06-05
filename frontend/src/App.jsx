import { useState, useRef, useCallback } from 'react';
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
import { getUser } from './api';

export default function App() {
  const [user, setUser] = useState(null);
  const [loggedIn, setLoggedIn] = useState(false);
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

  const loginEffectPendingRef = useRef(false);
  const logoutEffectPendingRef = useRef(false);

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

  // Login handler
  const handleLogin = useCallback((userData) => {
    setUser(userData);
    setShowLoginEffect(true);
    loginEffectPendingRef.current = true;
  }, []);

  const handleLoginEffectComplete = useCallback(() => {
    setLoggedIn(true);
    setShowLoginEffect(false);
    setActiveTab('launch');
  }, []);

  // Logout handler
  const handleLogout = useCallback(() => {
    setStarPaused(true);
    setShowLogoutEffect(true);
    logoutEffectPendingRef.current = true;
  }, []);

  const handleLogoutEffectComplete = useCallback(() => {
    setLoggedIn(false);
    setUser(null);
    setStarPaused(false);
    setShowLogoutEffect(false);
    setActiveTab('launch');
  }, []);

  // Profile update — refresh user state from server
  const handleUserUpdate = useCallback((updatedUser) => {
    setUser(updatedUser);
    if (updatedUser?.id) {
      getUser(updatedUser.id).then(res => {
        if (res.code === 200) setUser(res.data);
      }).catch(() => {});
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

      <div className="app-container">
        {/* Top Bar */}
        <div className="top-bar" id="topBar" style={{ opacity: loggedIn ? 1 : 1 }}>
          <h1>流星树洞</h1>
          <p>让烦恼化作流星，消失在星河里</p>
        </div>

        <div className="page-content" id="pageContent">
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
        <div className={`bottom-tabs ${!loggedIn ? 'locked' : ''}`} id="bottomTabs">
          <button className={`tab-item ${activeTab === 'launch' ? 'active' : ''}`} data-tab="launch" onClick={() => switchTab('launch')}>
            <span className="tab-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 1 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>
              </svg>
            </span>
            <span className="tab-label">写流星</span>
          </button>
          <button className={`tab-item ${activeTab === 'catch' ? 'active' : ''}`} data-tab="catch" onClick={() => switchTab('catch')}>
            <span className="tab-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8z"/><path d="M12 2v6"/><path d="M12 16v6"/><path d="M2 12h6"/><path d="M16 12h6"/>
              </svg>
            </span>
            <span className="tab-label">捞流星</span>
          </button>
          <button className={`tab-item ${activeTab === 'starmap' ? 'active' : ''}`} data-tab="starmap" onClick={() => switchTab('starmap')}>
            <span className="tab-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
              </svg>
            </span>
            <span className="tab-label">星图</span>
          </button>
          <button className={`tab-item ${activeTab === 'profile' ? 'active' : ''}`} data-tab="profile" onClick={() => switchTab('profile')}>
            <span className="tab-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 3.6-7 8-7s8 3 8 7"/>
              </svg>
            </span>
            <span className="tab-label">我的</span>
          </button>
          {/* 管理员审核入口 — 仅对管理员可见 */}
          {user?.isAdmin && (
            <button className={`tab-item ${activeTab === 'admin' ? 'active' : ''}`} data-tab="admin" onClick={() => switchTab('admin')}>
              <span className="tab-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                  <path d="M9 12l2 2 4-4"/>
                </svg>
              </span>
              <span className="tab-label">审核</span>
            </button>
          )}
        </div>
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
