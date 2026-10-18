import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { getToken, onUnauthorized, setToken } from '../api/client';
import { authApi, userApi } from '../api/endpoints';

const USER_KEY = 'reservas.user';
const AuthContext = createContext(null);

function readStoredUser() {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

function storeUser(user) {
  try {
    if (user) localStorage.setItem(USER_KEY, JSON.stringify(user));
    else localStorage.removeItem(USER_KEY);
  } catch {}
}

function userFromAuth(response) {
  const { uuid, email, name, lastName, role } = response;
  return { uuid, email, name, lastName, role };
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => (getToken() ? readStoredUser() : null));
  const [sessionExpired, setSessionExpired] = useState(false);

  const applyUser = useCallback((next) => {
    setUser(next);
    storeUser(next);
  }, []);

  const logout = useCallback(() => {
    setToken(null);
    applyUser(null);
  }, [applyUser]);

  useEffect(() => {
    onUnauthorized(() => {
      setToken(null);
      applyUser(null);
      setSessionExpired(true);
    });
    return () => onUnauthorized(null);
  }, [applyUser]);

  const startSession = useCallback(
    (response) => {
      setToken(response.token);
      setSessionExpired(false);
      applyUser(userFromAuth(response));
    },
    [applyUser],
  );

  const login = useCallback(
    async (email, password) => startSession(await authApi.login(email.trim(), password)),
    [startSession],
  );

  const register = useCallback(async (data) => startSession(await authApi.register(data)), [startSession]);

  const refreshUser = useCallback(async () => {
    const me = await userApi.me();
    applyUser(userFromAuth(me));
    return me;
  }, [applyUser]);

  useEffect(() => {
    if (getToken()) refreshUser().catch(() => {});
  }, [refreshUser]);

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: Boolean(user),
      isOwner: user?.role === 'BUSINESS_OWNER' || user?.role === 'ADMIN',
      sessionExpired,
      login,
      register,
      logout,
      refreshUser,
      setUser: applyUser,
    }),
    [user, sessionExpired, login, register, logout, refreshUser, applyUser],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used inside AuthProvider');
  return context;
}
