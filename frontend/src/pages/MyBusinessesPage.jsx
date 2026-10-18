import { useState } from 'react';
import { Link } from 'react-router-dom';
import { businessApi } from '../api/endpoints';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { useLoader } from '../hooks/useLoader';
import BusinessForm from '../components/BusinessForm';
import { BusinessAvatar } from '../components/BusinessCard';
import Icon from '../components/Icon';
import { Alert, Button, ConfirmDialog, EmptyState, Modal, Spinner } from '../components/ui';
import { errorMessage } from '../utils/errors';

export default function MyBusinessesPage() {
  const { isOwner, refreshUser } = useAuth();
  const toast = useToast();
  const [creating, setCreating] = useState(false);
  const [editing, setEditing] = useState(null);
  const [deleting, setDeleting] = useState(null);
  const [busy, setBusy] = useState(false);

  const { data, loading, error, reload, setData } = useLoader(
    () => (isOwner ? businessApi.mine() : Promise.resolve([])),
    [isOwner],
  );
  const businesses = data || [];

  const handleCreate = async (payload) => {
    const created = await businessApi.create(payload);
    setCreating(false);
    toast.success(`«${created.name}» publicado`);
    if (!isOwner) {
      await refreshUser().catch(() => {});
    } else {
      setData((list) => [...(list || []), created].sort((a, b) => a.name.localeCompare(b.name)));
    }
  };

  const handleUpdate = async (payload) => {
    const updated = await businessApi.update(editing.uuid, payload);
    setData((list) => (list || []).map((b) => (b.uuid === updated.uuid ? updated : b)));
    setEditing(null);
    toast.success('Cambios guardados');
  };

  const handleDelete = async () => {
    setBusy(true);
    try {
      await businessApi.remove(deleting.uuid);
      setData((list) => (list || []).filter((b) => b.uuid !== deleting.uuid));
      toast.success('Negocio eliminado');
      setDeleting(null);
    } catch (err) {
      toast.error(errorMessage(err));
      setDeleting(null);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <h1>Mis negocios</h1>
          <p className="muted">Publica tus negocios y gestiona las reservas que recibes.</p>
        </div>
        {businesses.length > 0 && (
          <Button icon="plus" onClick={() => setCreating(true)}>
            Nuevo negocio
          </Button>
        )}
      </header>

      <Alert onRetry={reload}>{error && errorMessage(error)}</Alert>

      {loading && !data ? (
        <Spinner label="Cargando negocios..." />
      ) : businesses.length === 0 && !error ? (
        <EmptyState
          icon="store"
          title="Publica tu primer negocio"
          action={
            <Button icon="plus" onClick={() => setCreating(true)}>
              Crear negocio
            </Button>
          }
        >
          Tus clientes podrán encontrarlo y reservar. Tú confirmarás cada reserva.
        </EmptyState>
      ) : (
        <div className="grid grid-cards">
          {businesses.map((business) => (
            <article key={business.uuid} className="card owner-card">
              <div className="owner-card-head">
                <BusinessAvatar name={business.name} />
                <div>
                  <h3>{business.name}</h3>
                  <p className="meta-line muted">CIF {business.cif}</p>
                </div>
              </div>
              <p className="meta-line">
                <Icon name="pin" size={14} /> {business.address}
              </p>
              <div className="owner-card-actions">
                <Link to={`/mis-negocios/${business.uuid}/reservas`} className="btn btn-primary btn-sm">
                  <Icon name="calendar" size={16} /> <span>Reservas</span>
                </Link>
                <Button variant="secondary" size="sm" icon="edit" onClick={() => setEditing(business)}>
                  Editar
                </Button>
                <Button variant="danger-ghost" size="sm" icon="trash" onClick={() => setDeleting(business)} aria-label={`Eliminar ${business.name}`} />
              </div>
            </article>
          ))}
        </div>
      )}

      {creating && (
        <Modal title="Nuevo negocio" onClose={() => setCreating(false)}>
          <BusinessForm submitLabel="Publicar negocio" onSubmit={handleCreate} onCancel={() => setCreating(false)} />
        </Modal>
      )}

      {editing && (
        <Modal title="Editar negocio" onClose={() => setEditing(null)}>
          <BusinessForm initial={editing} submitLabel="Guardar cambios" onSubmit={handleUpdate} onCancel={() => setEditing(null)} />
        </Modal>
      )}

      {deleting && (
        <ConfirmDialog
          title={`¿Eliminar «${deleting.name}»?`}
          message="Dejará de aparecer en el listado y no podrá recibir reservas. Solo se puede eliminar si no tiene reservas pendientes ni confirmadas."
          confirmLabel="Eliminar"
          danger
          loading={busy}
          onConfirm={handleDelete}
          onCancel={() => setDeleting(null)}
        />
      )}
    </div>
  );
}
