const BASE_URL = (import.meta.env.VITE_API_URL || '/api').replace(/\/$/, '');
const TOKEN_KEY = 'reservas.token';

export class ApiError extends Error {
  constructor(status, error, details = []) {
    super(details[0] || error || `Error ${status}`);
    this.name = 'ApiError';
    this.status = status;
    this.error = error;
    this.details = details;
  }

  get fieldErrors() {
    const result = {};
    for (const detail of this.details) {
      const match = /^([a-zA-Z]+): (.+)$/.exec(detail);
      if (match) result[match[1]] = match[2];
    }
    return result;
  }
}

let unauthorizedHandler = null;

export function onUnauthorized(handler) {
  unauthorizedHandler = handler;
}

export function getToken() {
  try {
    return localStorage.getItem(TOKEN_KEY);
  } catch {
    return null;
  }
}

export function setToken(token) {
  try {
    if (token) localStorage.setItem(TOKEN_KEY, token);
    else localStorage.removeItem(TOKEN_KEY);
  } catch {}
}

function buildQuery(params) {
  if (!params) return '';
  const search = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== null && value !== '') search.append(key, value);
  }
  const query = search.toString();
  return query ? `?${query}` : '';
}

export async function request(path, { method = 'GET', body, params, auth = true } = {}) {
  const headers = { Accept: 'application/json' };
  const token = auth ? getToken() : null;
  if (token) headers.Authorization = `Bearer ${token}`;
  if (body !== undefined) headers['Content-Type'] = 'application/json';

  let response;
  try {
    response = await fetch(`${BASE_URL}${path}${buildQuery(params)}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new ApiError(0, 'Network error', ['No se puede conectar con el servidor. ¿Está arrancada la API?']);
  }

  if (response.status === 204) return null;

  const text = await response.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = null;
    }
  }

  if (!response.ok) {
    if (response.status === 401 && token && unauthorizedHandler) unauthorizedHandler();
    const details = Array.isArray(data?.details) ? data.details : [];
    throw new ApiError(response.status, data?.error || response.statusText, details);
  }
  return data;
}
