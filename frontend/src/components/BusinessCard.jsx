import { Link } from 'react-router-dom';
import Icon from './Icon';
import { initials } from '../utils/format';

const HUES = [243, 199, 160, 32, 340, 270, 12, 180];

function hueFor(text = '') {
  let hash = 0;
  for (const char of text) hash = (hash * 31 + char.charCodeAt(0)) >>> 0;
  return HUES[hash % HUES.length];
}

export function BusinessAvatar({ name, size = 'md' }) {
  return (
    <span className={`business-avatar business-avatar-${size}`} style={{ '--hue': hueFor(name) }} aria-hidden="true">
      {initials(name)}
    </span>
  );
}

export default function BusinessCard({ business, own }) {
  return (
    <Link to={`/negocios/${business.uuid}`} className="business-card">
      <BusinessAvatar name={business.name} />
      <div className="business-card-body">
        <h3>{business.name}</h3>
        <p className="meta-line">
          <Icon name="pin" size={14} /> {business.address}
        </p>
        {business.ownerName && <p className="meta-line muted">Gestionado por {business.ownerName}</p>}
      </div>
      <span className="business-card-cta">
        {own ? 'Tu negocio' : 'Reservar'} <Icon name="arrowRight" size={14} />
      </span>
    </Link>
  );
}
