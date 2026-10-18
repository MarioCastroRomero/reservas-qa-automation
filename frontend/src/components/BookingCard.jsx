import { Link } from 'react-router-dom';
import Icon from './Icon';
import { StatusBadge } from './ui';
import { formatDateTime, formatPrice } from '../utils/format';

export default function BookingCard({ booking, perspective = 'holder', actions }) {
  const date = new Date(booking.bookingTime);
  const valid = !Number.isNaN(date.getTime());
  return (
    <article className={`booking-card booking-${booking.status?.toLowerCase()}`}>
      <div className="booking-date" aria-hidden="true">
        <span className="booking-day">{valid ? date.getDate() : '-'}</span>
        <span className="booking-month">{valid ? date.toLocaleDateString('es-ES', { month: 'short' }) : ''}</span>
      </div>
      <div className="booking-main">
        <div className="booking-title">
          {perspective === 'holder' ? (
            <Link to={`/negocios/${booking.businessId}`} className="booking-name">
              {booking.businessName || 'Negocio'}
            </Link>
          ) : (
            <span className="booking-name">{booking.holderName || 'Cliente'}</span>
          )}
          <StatusBadge status={booking.status} />
        </div>
        <ul className="meta">
          <li>
            <Icon name="clock" size={14} /> {formatDateTime(booking.bookingTime)}
          </li>
          <li>
            <Icon name="users" size={14} /> {booking.partySize} {booking.partySize === 1 ? 'persona' : 'personas'}
          </li>
          {perspective === 'holder' && booking.businessAddress && (
            <li>
              <Icon name="pin" size={14} /> {booking.businessAddress}
            </li>
          )}
          {perspective === 'owner' && booking.holderEmail && (
            <li>
              <Icon name="mail" size={14} /> <a href={`mailto:${booking.holderEmail}`}>{booking.holderEmail}</a>
            </li>
          )}
          {booking.price !== null && booking.price !== undefined && (
            <li>
              <Icon name="euro" size={14} /> {formatPrice(booking.price)}
            </li>
          )}
        </ul>
        {booking.note && (
          <p className="booking-note">
            <Icon name="note" size={14} /> {booking.note}
          </p>
        )}
      </div>
      {actions && <div className="booking-actions">{actions}</div>}
    </article>
  );
}
