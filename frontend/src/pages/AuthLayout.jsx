import Icon from '../components/Icon';

export default function AuthLayout({ title, subtitle, children }) {
  return (
    <div className="auth">
      <aside className="auth-hero">
        <div className="brand brand-light">
          <span className="brand-mark">
            <Icon name="calendar" size={18} />
          </span>
          Reservas
        </div>
        <div>
          <h1>Reserva en tus negocios favoritos en segundos.</h1>
          <ul className="auth-points">
            <li>
              <Icon name="search" /> Encuentra negocios y reserva al momento
            </li>
            <li>
              <Icon name="check" /> Sigue el estado de cada reserva
            </li>
            <li>
              <Icon name="store" /> Publica tu negocio y gestiona tus clientes
            </li>
          </ul>
        </div>
        <small>© Reservas</small>
      </aside>
      <section className="auth-panel">
        <div className="auth-card">
          <h2>{title}</h2>
          {subtitle && <p className="muted">{subtitle}</p>}
          {children}
        </div>
      </section>
    </div>
  );
}
