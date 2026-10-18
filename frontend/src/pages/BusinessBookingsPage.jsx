import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { bookingApi, businessApi } from '../api/endpoints';
import { useToast } from '../context/ToastContext';
import { useLoader } from '../hooks/useLoader';
import BookingCard from '../components/BookingCard';
import Icon from '../components/Icon';
import { Alert, Button, ConfirmDialog, EmptyState, Pagination, Spinner, Tabs } from '../components/ui';
import { STATUSES, STATUS_LABELS, ownerActions } from '../utils/bookingStatus';
import { errorMessage } from '../utils/errors';
import { formatDateTime } from '../utils/format';

const PAGE_SIZE = 20;
const TAB_OPTIONS = [{ value: '', label: 'Todas' }, ...STATUSES.map((s) => ({ value: s, label: STATUS_LABELS[s] + 's' }))];

const ACTION_TEXT = {
  confirm: { done: 'Reserva confirmada', call: bookingApi.confirm },
  complete: { done: 'Reserva marcada como completada', call: bookingApi.complete },
  cancel: { done: 'Reserva cancelada', call: bookingApi.cancel },
};

export default function BusinessBookingsPage() {
  const { id } = useParams();
  const toast = useToast();
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(0);
  const [pending, setPending] = useState(null);
  const [cancelling, setCancelling] = useState(null);

  const business = useLoader(() => businessApi.get(id), [id]);
  const { data, loading, error, reload, setData } = useLoader(
    () => businessApi.bookings(id, { status: status || undefined, page, size: PAGE_SIZE }),
    [id, status, page],
  );
  const bookings = data || [];

  const changeTab = (value) => {
    setStatus(value);
    setPage(0);
  };

  const run = async (booking, action) => {
    setPending({ uuid: booking.uuid, action });
    try {
      const updated = await ACTION_TEXT[action].call(booking.uuid);
      setData((list) =>
        (list || []).map((b) => (b.uuid === updated.uuid ? updated : b)).filter((b) => !status || b.status === status),
      );
      toast.success(ACTION_TEXT[action].done);
    } catch (err) {
      toast.error(errorMessage(err));
      reload();
    } finally {
      setPending(null);
      setCancelling(null);
    }
  };

  const isBusy = (booking, action) => pending?.uuid === booking.uuid && pending.action === action;

  if (business.error) {
    const notFound = business.error.status === 404 || business.error.status === 400;
    return notFound ? (
      <EmptyState icon="store" title="Negocio no encontrado" action={<Link to="/mis-negocios" className="btn btn-primary">Mis negocios</Link>} />
    ) : (
      <Alert onRetry={business.reload}>{errorMessage(business.error)}</Alert>
    );
  }

  const forbidden = error?.status === 403;

  return (
    <div className="page">
      <Link to="/mis-negocios" className="back-link">
        <Icon name="arrowLeft" size={16} /> Mis negocios
      </Link>
      <header className="page-header">
        <div>
          <h1>Reservas recibidas</h1>
          <p className="muted">{business.data ? `${business.data.name} - ${business.data.address}` : ' '}</p>
        </div>
      </header>

      {forbidden ? (
        <EmptyState icon="lock" title="Sin acceso">
          Solo el propietario del negocio puede ver sus reservas.
        </EmptyState>
      ) : (
        <>
          <Tabs options={TAB_OPTIONS} value={status} onChange={changeTab} label="Filtrar por estado" />
          <Alert onRetry={reload}>{error && errorMessage(error)}</Alert>

          {loading && !data ? (
            <Spinner label="Cargando reservas..." />
          ) : bookings.length === 0 && !error ? (
            <EmptyState icon="inbox" title={status ? `No hay reservas ${STATUS_LABELS[status].toLowerCase()}s` : 'Todavía no hay reservas'}>
              {!status && 'Cuando un cliente reserve aparecerá aquí para que la confirmes.'}
            </EmptyState>
          ) : (
            <div className={`list ${loading ? 'is-loading' : ''}`}>
              {bookings.map((booking) => {
                const actions = ownerActions(booking.status);
                const disabled = Boolean(pending);
                return (
                  <BookingCard
                    key={booking.uuid}
                    booking={booking}
                    perspective="owner"
                    actions={
                      (actions.canConfirm || actions.canComplete || actions.canCancel) && (
                        <>
                          {actions.canConfirm && (
                            <Button size="sm" icon="check" loading={isBusy(booking, 'confirm')} disabled={disabled} onClick={() => run(booking, 'confirm')}>
                              Confirmar
                            </Button>
                          )}
                          {actions.canComplete && (
                            <Button size="sm" variant="success" icon="flag" loading={isBusy(booking, 'complete')} disabled={disabled} onClick={() => run(booking, 'complete')}>
                              Completar
                            </Button>
                          )}
                          {actions.canCancel && (
                            <Button size="sm" variant="danger-ghost" icon="x" disabled={disabled} onClick={() => setCancelling(booking)}>
                              Cancelar
                            </Button>
                          )}
                        </>
                      )
                    }
                  />
                );
              })}
            </div>
          )}

          <Pagination page={page} hasNext={bookings.length === PAGE_SIZE} onChange={setPage} />
        </>
      )}

      {cancelling && (
        <ConfirmDialog
          title="¿Cancelar esta reserva?"
          message={`Se cancelará la reserva de ${cancelling.holderName || 'el cliente'} del ${formatDateTime(cancelling.bookingTime)}.`}
          confirmLabel="Sí, cancelar"
          danger
          loading={isBusy(cancelling, 'cancel')}
          onConfirm={() => run(cancelling, 'cancel')}
          onCancel={() => setCancelling(null)}
        />
      )}
    </div>
  );
}
