import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

// ========== 用户 ==========
export const loginAnonymous = (nickname) =>
  api.post('/user/login', { nickname }).then(r => r.data);

export const register = (username, nickname, password) =>
  api.post('/user/register', { username, nickname, password }).then(r => r.data);

export const loginWithPassword = (username, password) =>
  api.post('/user/login/password', { username, password }).then(r => r.data);

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

export default api;
