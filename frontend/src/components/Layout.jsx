import { useEffect, useRef, useState } from 'react';
import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Icon from './Icon';
import { initials } from '../utils/format';

const ROLE_LABELS = { USER: 'Cliente', BUSINESS_OWNER: 'Propietario', ADMIN: 'Administrador' };

export default function Layout() {
  const { user, isOwner, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);

  const headerRef = useRef(null);

  useEffect(() => setMenuOpen(false), [location.pathname]);

  useEffect(() => {
    if (!menuOpen) return undefined;
    const onKey = (event) => event.key === 'Escape' && setMenuOpen(false);
    const onClick = (event) => {
      if (!headerRef.current?.contains(event.target)) setMenuOpen(false);
    };
    document.addEventListener('keydown', onKey);
    document.addEventListener('mousedown', onClick);
    return () => {
      document.removeEventListener('keydown', onKey);
      document.removeEventListener('mousedown', onClick);
    };
  }, [menuOpen]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const fullName = [user?.name, user?.lastName].filter(Boolean).join(' ');

  return (
    <div className="app-shell">
      <header className="topbar" ref={headerRef}>
        <div className="topbar-inner container">
          <Link to="/negocios" className="brand">
            <span className="brand-mark">
              <Icon name="calendar" size={18} />
            </span>
            Reservas
          </Link>

          <button
            type="button"
            className="icon-button menu-toggle"
            aria-label="Abrir menú"
            aria-expanded={menuOpen}
            onClick={() => setMenuOpen((open) => !open)}
          >
            <Icon name={menuOpen ? 'x' : 'menu'} />
          </button>

          <nav className={`nav ${menuOpen ? 'nav-open' : ''}`} aria-label="Principal">
            <NavLink to="/negocios" className="nav-link">
              <Icon name="search" size={16} /> Explorar
            </NavLink>
            <NavLink to="/mis-reservas" className="nav-link">
              <Icon name="calendar" size={16} /> Mis reservas
            </NavLink>
            <NavLink to="/mis-negocios" className="nav-link">
              <Icon name="store" size={16} /> {isOwner ? 'Mis negocios' : 'Publicar negocio'}
            </NavLink>
            <div className="nav-user">
              <NavLink to="/perfil" className="nav-link nav-profile" title="Mi perfil">
                <span className="avatar">{initials(fullName) || '?'}</span>
                <span className="nav-profile-text">
                  <strong>{user?.name}</strong>
                  <small>{ROLE_LABELS[user?.role] || user?.role}</small>
                </span>
              </NavLink>
              <button type="button" className="icon-button" onClick={handleLogout} title="Cerrar sesión" aria-label="Cerrar sesión">
                <Icon name="logout" />
              </button>
            </div>
          </nav>
        </div>
      </header>

      <main className="container main">
        <Outlet />
      </main>

      <footer className="footer container muted">Reservas - Gestiona citas y reservas de tus negocios</footer>
    </div>
  );
}
