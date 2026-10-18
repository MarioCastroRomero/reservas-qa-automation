import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { bookingApi, businessApi } from '../api/endpoints';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { useLoader } from '../hooks/useLoader';
import BookingForm from '../components/BookingForm';
import { BusinessAvatar } from '../components/BusinessCard';
import Icon from '../components/Icon';
import { Alert, Button, EmptyState, Spinner } from '../components/ui';
import { errorMessage } from '../utils/errors';

export default function BusinessDetailPage() {
  const { id } = useParams();
  const { user } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const { data: business, loading, error, reload } = useLoader(() => businessApi.get(id), [id]);
  const [formKey, setFormKey] = useState(0);

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
          <ul className="meta meta-column">
            <li>
              <Icon name="pin" size={16} /> {business.address}
            </li>
            <li>
              <Icon name="flag" size={16} /> CIF {business.cif}
            </li>
            {business.ownerName && (
              <li>
                <Icon name="user" size={16} /> {isOwn ? 'Este negocio es tuyo' : `Gestionado por ${business.ownerName}`}
              </li>
            )}
          </ul>
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
    </div>
  );
}
