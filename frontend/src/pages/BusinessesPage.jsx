import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { businessApi } from '../api/endpoints';
import { useAuth } from '../context/AuthContext';
import { useDebounced, useLoader } from '../hooks/useLoader';
import BusinessCard from '../components/BusinessCard';
import Icon from '../components/Icon';
import { Alert, EmptyState, Pagination, Spinner } from '../components/ui';
import { errorMessage } from '../utils/errors';

const PAGE_SIZE = 12;

export default function BusinessesPage() {
  const { user } = useAuth();
  const [params, setParams] = useSearchParams();
  const [query, setQuery] = useState(params.get('q') || '');
  const page = Number(params.get('page') || 0);
  const debounced = useDebounced(query.trim(), 300);

  useEffect(() => {
    if (debounced === (params.get('q') || '')) return;
    const next = new URLSearchParams();
    if (debounced) next.set('q', debounced);
    setParams(next, { replace: true });
  }, [debounced, params, setParams]);

  const q = params.get('q') || '';
  const { data, loading, error, reload } = useLoader(() => businessApi.search({ q, page, size: PAGE_SIZE }), [q, page]);
  const businesses = data || [];

  const goToPage = (next) => {
    const nextParams = new URLSearchParams(params);
    if (next > 0) nextParams.set('page', next);
    else nextParams.delete('page');
    setParams(nextParams);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <div className="page">
      <section className="hero">
        <div>
          <h1>Hola, {user?.name} 👋</h1>
          <p className="muted">Encuentra un negocio y reserva tu próxima cita.</p>
        </div>
        <label className="search">
          <Icon name="search" />
          <input
            type="search"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Buscar por nombre o dirección..."
            aria-label="Buscar negocios"
          />
        </label>
      </section>

      <Alert onRetry={reload}>{error && errorMessage(error)}</Alert>

      {loading && !data ? (
        <Spinner label="Cargando negocios..." />
      ) : businesses.length === 0 && !error ? (
        <EmptyState icon="store" title={q ? 'Sin resultados' : 'Todavía no hay negocios'}>
          {q ? `No hay negocios que coincidan con «${q}».` : 'Cuando alguien publique su negocio aparecerá aquí.'}
        </EmptyState>
      ) : (
        <div className={`grid grid-cards ${loading ? 'is-loading' : ''}`}>
          {businesses.map((business) => (
            <BusinessCard key={business.uuid} business={business} own={business.ownerId === user?.uuid} />
          ))}
        </div>
      )}

      <Pagination page={page} hasNext={businesses.length === PAGE_SIZE} onChange={goToPage} />
    </div>
  );
}
