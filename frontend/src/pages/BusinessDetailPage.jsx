import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { bookingApi, businessApi } from '../api/endpoints';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { useLoader } from '../hooks/useLoader';
import BookingForm from '../components/BookingForm';
import { BusinessAvatar } from '../components/BusinessCard';
import Icon from '../components/Icon';
import { StarRating } from '../components/dialogs';
import { DAYS } from '../components/OpeningHoursEditor';
import { Alert, Button, EmptyState, Spinner } from '../components/ui';
import { errorMessage } from '../utils/errors';
import { formatDateTime } from '../utils/format';

export default function BusinessDetailPage() {
  const { id } = useParams();
  const { user } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const { data: business, loading, error, reload } = useLoader(() => businessApi.get(id), [id]);
  const [formKey, setFormKey] = useState(0);
  const hours = useLoader(() => businessApi.openingHours(id), [id]);
  const reviews = useLoader(() => businessApi.reviews(id), [id]);

  if (loading && !business) return <Spinner />;

  if (error) {
    if (error.status === 404 || error.status === 400) {
      return (
        <EmptyState icon="store" title="Negocio no encontrado" action={<Link to="/negocios" className="btn btn-primary">Volver al listado</Link>}>
          Puede que el negocio se haya dado de baja.
        </EmptyState>
      );
    }
    return <Alert onRetry={reload}>{errorMessage(error)}</Alert>;
  }

  const isOwn = business.ownerId === user?.uuid;

  const handleBook = async (payload) => {
    await bookingApi.create({ ...payload, businessId: business.uuid });
    toast.success('Reserva enviada. El negocio tiene que confirmarla.');
    setFormKey((k) => k + 1);
    navigate('/mis-reservas');
  };

  return (
    <div className="page">
      <Link to="/negocios" className="back-link">
        <Icon name="arrowLeft" size={16} /> Todos los negocios
      </Link>

      <div className="detail">
        <section className="card detail-info">
          <BusinessAvatar name={business.name} size="lg" />
          <h1>{business.name}</h1>
          <p className="rating-line" data-testid="rating-summary">
            {business.reviewCount > 0 ? (
              <>
                <StarRating value={business.averageRating} size={18} />{' '}
                <strong data-testid="average-rating">{business.averageRating.toFixed(1)}</strong>{' '}
                <span className="muted">
                  ({business.reviewCount} {business.reviewCount === 1 ? 'reseña' : 'reseñas'})
                </span>
              </>
            ) : (
              <span className="muted">Sin reseñas todavía</span>
            )}
          </p>
          <ul className="meta meta-column">
            <li>
              <Icon name="pin" size={16} /> {business.address}
            </li>
            <li>
              <Icon name="flag" size={16} /> CIF {business.cif}
            </li>
            {business.maxCapacityPerSlot && (
              <li data-testid="capacity">
                <Icon name="users" size={16} /> Aforo: {business.maxCapacityPerSlot} personas por hora
              </li>
            )}
            {business.ownerName && (
              <li>
                <Icon name="user" size={16} /> {isOwn ? 'Este negocio es tuyo' : `Gestionado por ${business.ownerName}`}
              </li>
            )}
          </ul>
          <h2 className="card-title">Horario</h2>
          {hours.data && hours.data.length > 0 ? (
            <ul className="hours-list" data-testid="opening-hours">
              {DAYS.map(([day, label]) => {
                const slot = hours.data.find((h) => h.dayOfWeek === day);
                return (
                  <li key={day} data-testid={`hours-${day}`}>
                    <span>{label}</span>
                    <span>{slot ? `${slot.openTime.slice(0, 5)} - ${slot.closeTime.slice(0, 5)}` : 'Cerrado'}</span>
                  </li>
                );
              })}
            </ul>
          ) : (
            <p className="muted small" data-testid="no-hours">
              Sin horario definido: acepta reservas a cualquier hora.
            </p>
          )}
        </section>

        <section className="card">
          {isOwn ? (
            <EmptyState
              icon="store"
              title="Este es tu negocio"
              action={
                <Button icon="calendar" onClick={() => navigate(`/mis-negocios/${business.uuid}/reservas`)}>
                  Ver reservas recibidas
                </Button>
              }
            >
              No puedes reservar en tu propio negocio, pero puedes gestionar las reservas que recibes.
            </EmptyState>
          ) : (
            <>
              <h2 className="card-title">Hacer una reserva</h2>
              <p className="muted small">La reserva quedará pendiente hasta que el negocio la confirme.</p>
              <BookingForm key={formKey} onSubmit={handleBook} submitLabel="Solicitar reserva" />
            </>
          )}
        </section>
      </div>

      <section className="card reviews" data-testid="reviews">
        <h2 className="card-title">Reseñas</h2>
        {reviews.data && reviews.data.length > 0 ? (
          <ul className="review-list">
            {reviews.data.map((review) => (
              <li key={review.uuid} className="review-item" data-testid="review-item">
                <div className="review-head">
                  <strong>{review.authorName}</strong> <StarRating value={review.rating} size={14} />
                </div>
                {review.comment && <p>{review.comment}</p>}
                <small className="muted">{formatDateTime(review.createdAt)}</small>
              </li>
            ))}
          </ul>
        ) : (
          <p className="muted small">Todavía no hay reseñas.</p>
        )}
      </section>
    </div>
  );
}
