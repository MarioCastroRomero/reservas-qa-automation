import { useState } from 'react';
import { Link } from 'react-router-dom';
import { bookingApi } from '../api/endpoints';
import { useToast } from '../context/ToastContext';
import { useLoader } from '../hooks/useLoader';
import BookingCard from '../components/BookingCard';
import BookingForm from '../components/BookingForm';
import { Alert, Button, ConfirmDialog, EmptyState, Modal, Pagination, Spinner, Tabs } from '../components/ui';
import { STATUSES, STATUS_LABELS, holderActions } from '../utils/bookingStatus';
import { errorMessage } from '../utils/errors';
import { formatDateTime } from '../utils/format';

const PAGE_SIZE = 20;
const TAB_OPTIONS = [{ value: '', label: 'Todas' }, ...STATUSES.map((s) => ({ value: s, label: STATUS_LABELS[s] + 's' }))];

export default function MyBookingsPage() {
  const toast = useToast();
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(0);
  const [editing, setEditing] = useState(null);
  const [cancelling, setCancelling] = useState(null);
  const [busy, setBusy] = useState(false);

  const { data, loading, error, reload, setData } = useLoader(
    () => bookingApi.mine({ status: status || undefined, page, size: PAGE_SIZE }),
    [status, page],
  );
  const bookings = data || [];

  const changeTab = (value) => {
    setStatus(value);
    setPage(0);
  };

  const replaceBooking = (updated) => {
    setData((list) =>
      (list || []).map((b) => (b.uuid === updated.uuid ? updated : b)).filter((b) => !status || b.status === status),
    );
  };

  const handleUpdate = async (payload) => {
    const updated = await bookingApi.update(editing.uuid, payload);
    replaceBooking(updated);
    setEditing(null);
    toast.success('Reserva actualizada');
  };

  const handleCancel = async () => {
    setBusy(true);
    try {
      const updated = await bookingApi.cancel(cancelling.uuid);
      replaceBooking(updated);
      toast.success('Reserva cancelada');
      setCancelling(null);
    } catch (err) {
      toast.error(errorMessage(err));
      reload();
      setCancelling(null);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <h1>Mis reservas</h1>
          <p className="muted">Consulta, modifica o cancela tus reservas.</p>
        </div>
        <Link to="/negocios" className="btn btn-primary">
          Nueva reserva
        </Link>
      </header>

      <Tabs options={TAB_OPTIONS} value={status} onChange={changeTab} label="Filtrar por estado" />
      <Alert onRetry={reload}>{error && errorMessage(error)}</Alert>

      {loading && !data ? (
        <Spinner label="Cargando reservas..." />
      ) : bookings.length === 0 && !error ? (
        <EmptyState
          icon="calendar"
          title={status ? `No tienes reservas ${STATUS_LABELS[status].toLowerCase()}s` : 'Aún no tienes reservas'}
          action={
            !status && (
              <Link to="/negocios" className="btn btn-primary">
                Explorar negocios
              </Link>
            )
          }
        >
          {!status && 'Busca un negocio y haz tu primera reserva.'}
        </EmptyState>
      ) : (
        <div className={`list ${loading ? 'is-loading' : ''}`}>
          {bookings.map((booking) => {
            const actions = holderActions(booking.status);
            return (
              <BookingCard
                key={booking.uuid}
                booking={booking}
                actions={
                  (actions.canEdit || actions.canCancel) && (
                    <>
                      {actions.canEdit && (
                        <Button variant="secondary" size="sm" icon="edit" onClick={() => setEditing(booking)}>
                          Modificar
                        </Button>
                      )}
                      {actions.canCancel && (
                        <Button variant="danger-ghost" size="sm" icon="x" onClick={() => setCancelling(booking)}>
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

      {editing && (
        <Modal title={`Modificar reserva - ${editing.businessName || ''}`} onClose={() => setEditing(null)}>
          <BookingForm initial={editing} showPrice={false} submitLabel="Guardar cambios" onSubmit={handleUpdate} onCancel={() => setEditing(null)} />
        </Modal>
      )}

      {cancelling && (
        <ConfirmDialog
          title="¿Cancelar la reserva?"
          message={`Vas a cancelar tu reserva en ${cancelling.businessName || 'el negocio'} del ${formatDateTime(cancelling.bookingTime)}. Esta acción no se puede deshacer.`}
          confirmLabel="Sí, cancelar"
          danger
          loading={busy}
          onConfirm={handleCancel}
          onCancel={() => setCancelling(null)}
        />
      )}
    </div>
  );
}
