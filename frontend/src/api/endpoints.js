import { request } from './client';

export const authApi = {
  login: (email, password) => request('/auth/login', { method: 'POST', body: { email, password }, auth: false }),
  register: (data) => request('/auth/register', { method: 'POST', body: data, auth: false }),
};

export const userApi = {
  me: () => request('/user/me'),
  updateMe: (data) => request('/user/me', { method: 'PUT', body: data }),
  changePassword: (currentPassword, newPassword) =>
    request('/user/me/password', { method: 'PATCH', body: { currentPassword, newPassword } }),
};

export const businessApi = {
  search: ({ q, page = 0, size = 12 } = {}) => request('/business', { params: { q, page, size } }),
  get: (id) => request(`/business/${id}`),
  mine: () => request('/business/mine'),
  create: (data) => request('/business', { method: 'POST', body: data }),
  update: (id, data) => request(`/business/${id}`, { method: 'PUT', body: data }),
  remove: (id) => request(`/business/${id}`, { method: 'DELETE' }),
  bookings: (id, { status, page = 0, size = 20 } = {}) =>
    request(`/business/${id}/bookings`, { params: { status, page, size } }),
};

export const bookingApi = {
  mine: ({ status, page = 0, size = 20 } = {}) => request('/booking/mine', { params: { status, page, size } }),
  get: (id) => request(`/booking/${id}`),
  create: (data) => request('/booking', { method: 'POST', body: data }),
  update: (id, data) => request(`/booking/${id}`, { method: 'PUT', body: data }),
  confirm: (id) => request(`/booking/${id}/confirm`, { method: 'PATCH' }),
  complete: (id) => request(`/booking/${id}/complete`, { method: 'PATCH' }),
  cancel: (id) => request(`/booking/${id}/cancel`, { method: 'PATCH' }),
};
