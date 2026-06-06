import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

// ========== 验证码 ==========
export const getCaptcha = () =>
  api.get('/captcha').then(r => r.data);

// ========== 用户 ==========
export const register = (username, nickname, password, captchaId, captcha) =>
  api.post('/user/register', { username, nickname, password, captchaId, captcha }).then(r => r.data);

export const loginWithPassword = (username, password, captchaId, captcha) =>
  api.post('/user/login/password', { username, password, captchaId, captcha }).then(r => r.data);

export const getUser = (id) =>
  api.get(`/user/${id}`).then(r => r.data);

export const updateProfile = (id, data) =>
  api.put(`/user/${id}`, data).then(r => r.data);

export const uploadAvatar = (id, avatarUrl) =>
  api.post(`/user/${id}/avatar`, { avatarUrl }).then(r => r.data);

export const uploadAvatarFile = (id, file) => {
  const formData = new FormData();
  formData.append('file', file);
  return api.post(`/user/${id}/avatar/upload`, formData).then(r => r.data);
};

export const changePassword = (id, oldPassword, newPassword) =>
  api.post(`/user/${id}/password`, { oldPassword, newPassword }).then(r => r.data);

export const getUserStats = (id) =>
  api.get(`/user/${id}/stats`).then(r => r.data);

// ========== 流星 ==========
export const publishMeteor = (userId, content, color) =>
  api.post('/meteors', { userId, content, color }).then(r => r.data);

export const getRandomMeteor = (userId) =>
  api.get('/meteors/random', { params: { userId } }).then(r => r.data);

export const getMeteor = (id) =>
  api.get(`/meteors/${id}`).then(r => r.data);

export const catchMeteor = (id, userId) =>
  api.post(`/meteors/${id}/catch`, { userId }).then(r => r.data);

export const makeWish = (meteorId, userId, content) =>
  api.post(`/meteors/${meteorId}/wish`, { userId, content }).then(r => r.data);

export const deleteMeteor = (id, userId) =>
  api.delete(`/meteors/${id}`, { data: { userId } }).then(r => r.data);

export const deleteWish = (wishId, userId) =>
  api.delete(`/meteors/wishes/${wishId}`, { data: { userId } }).then(r => r.data);

export const getWishes = (meteorId) =>
  api.get(`/meteors/${meteorId}/wishes`).then(r => r.data);

export const getUserMeteors = (userId) =>
  api.get(`/meteors/user/${userId}`).then(r => r.data);

export const getCaughtMeteors = (userId) =>
  api.get(`/meteors/caught/${userId}`).then(r => r.data);

export const getUserMeteorsWithWishes = (userId) =>
  api.get(`/meteors/user/${userId}/with-wishes`).then(r => r.data);

export const getUserWishes = (userId) =>
  api.get(`/meteors/wishes/user/${userId}`).then(r => r.data);

// ========== 管理员 ==========
export const getPendingReviews = (adminId) =>
  api.get(`/admin/pending?adminId=${adminId}`).then(r => r.data);

export const reviewMessage = (messageId, adminId, status, reason) =>
  api.post(`/admin/review/${messageId}?adminId=${adminId}`, { status, reason }).then(r => r.data);

export const getAdminStats = (adminId) =>
  api.get(`/admin/stats?adminId=${adminId}`).then(r => r.data);

export const getAllMessages = (adminId, status) =>
  api.get(`/admin/messages?adminId=${adminId}${status ? `&status=${status}` : ''}`).then(r => r.data);

// ========== 管理员 - 回复审核 ==========
export const getPendingWishes = (adminId) =>
  api.get(`/admin/wishes/pending?adminId=${adminId}`).then(r => r.data);

export const getAllWishes = (adminId, status) =>
  api.get(`/admin/wishes?adminId=${adminId}${status ? `&status=${status}` : ''}`).then(r => r.data);

export const reviewWish = (wishId, adminId, status, reason) =>
  api.post(`/admin/wishes/${wishId}/review?adminId=${adminId}`, { status, reason }).then(r => r.data);

export const deleteWishAdmin = (wishId, adminId) =>
  api.delete(`/admin/wishes/${wishId}?adminId=${adminId}`).then(r => r.data);

export const deleteMeteorAdmin = (messageId, adminId) =>
  api.delete(`/admin/meteors/${messageId}?adminId=${adminId}`).then(r => r.data);

export const getWishStats = (adminId) =>
  api.get(`/admin/wishes/stats?adminId=${adminId}`).then(r => r.data);

// ========== 管理员 - 用户管理 ==========
export const getAdminUsers = (adminId) =>
  api.get(`/admin/users?adminId=${adminId}`).then(r => r.data);

export const deleteUserAdmin = (userId, adminId) =>
  api.delete(`/admin/users/${userId}?adminId=${adminId}`).then(r => r.data);

export default api;
