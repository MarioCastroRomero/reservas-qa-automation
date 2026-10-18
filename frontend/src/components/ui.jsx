import { useEffect, useId, useRef } from 'react';
import Icon from './Icon';
import { STATUS_LABELS } from '../utils/bookingStatus';

export function Button({ variant = 'primary', size, loading = false, icon, children, className = '', ...rest }) {
  const classes = ['btn', `btn-${variant}`, size ? `btn-${size}` : '', className].filter(Boolean).join(' ');
  return (
    <button type="button" className={classes} disabled={loading || rest.disabled} {...rest}>
      {loading ? <span className="spinner spinner-sm" aria-hidden="true" /> : icon && <Icon name={icon} size={16} />}
      {children && <span>{children}</span>}
    </button>
  );
}

export function Field({ label, error, hint, children, className = '' }) {
  const id = useId();
  const describedBy = error ? `${id}-error` : hint ? `${id}-hint` : undefined;
  const control = typeof children === 'function' ? children({ id, 'aria-invalid': Boolean(error), 'aria-describedby': describedBy }) : children;
  return (
    <div className={`field ${error ? 'field-invalid' : ''} ${className}`}>
      {label && <label htmlFor={id}>{label}</label>}
      {control}
      {error ? (
        <p className="field-error" id={`${id}-error`}>
          {error}
        </p>
      ) : (
        hint && (
          <p className="field-hint" id={`${id}-hint`}>
            {hint}
          </p>
        )
      )}
    </div>
  );
}

export function Spinner({ label = 'Cargando...' }) {
  return (
    <div className="loader" role="status">
      <span className="spinner" aria-hidden="true" />
      <span className="loader-label">{label}</span>
    </div>
  );
}

export function EmptyState({ icon = 'inbox', title, children, action }) {
  return (
    <div className="empty">
      <div className="empty-icon">
        <Icon name={icon} size={28} />
      </div>
      <h3>{title}</h3>
      {children && <p>{children}</p>}
      {action}
    </div>
  );
}

export function Alert({ type = 'error', children, onRetry }) {
  if (!children) return null;
  return (
    <div className={`alert alert-${type}`} role={type === 'error' ? 'alert' : 'status'}>
      <span>{children}</span>
      {onRetry && (
        <button type="button" className="link-button" onClick={onRetry}>
          Reintentar
        </button>
      )}
    </div>
  );
}

export function StatusBadge({ status }) {
  return <span className={`badge badge-${status?.toLowerCase()}`}>{STATUS_LABELS[status] || status}</span>;
}

export function Modal({ title, onClose, children, footer, size }) {
  const dialogRef = useRef(null);
  const titleId = useId();

  useEffect(() => {
    const previous = document.activeElement;
    const first = dialogRef.current?.querySelector('input, textarea, select, button:not(.modal-close)');
    first?.focus();
    const onKey = (event) => {
      if (event.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', onKey);
    document.body.classList.add('no-scroll');
    return () => {
      document.removeEventListener('keydown', onKey);
      document.body.classList.remove('no-scroll');
      previous?.focus?.();
    };
  }, [onClose]);

  return (
    <div className="modal-backdrop" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <div className={`modal ${size ? `modal-${size}` : ''}`} role="dialog" aria-modal="true" aria-labelledby={titleId} ref={dialogRef}>
        <header className="modal-header">
          <h2 id={titleId}>{title}</h2>
          <button type="button" className="icon-button modal-close" aria-label="Cerrar" onClick={onClose}>
            <Icon name="x" />
          </button>
        </header>
        <div className="modal-body">{children}</div>
        {footer && <footer className="modal-footer">{footer}</footer>}
      </div>
    </div>
  );
}

export function ConfirmDialog({ title, message, confirmLabel = 'Confirmar', danger = false, loading, onConfirm, onCancel }) {
  return (
    <Modal
      title={title}
      onClose={onCancel}
      size="sm"
      footer={
        <>
          <Button variant="ghost" onClick={onCancel} disabled={loading}>
            Volver
          </Button>
          <Button variant={danger ? 'danger' : 'primary'} onClick={onConfirm} loading={loading}>
            {confirmLabel}
          </Button>
        </>
      }
    >
      <p className="muted">{message}</p>
    </Modal>
  );
}

export function Pagination({ page, hasNext, onChange }) {
  if (page === 0 && !hasNext) return null;
  return (
    <nav className="pagination" aria-label="Paginación">
      <Button variant="ghost" size="sm" icon="arrowLeft" disabled={page === 0} onClick={() => onChange(page - 1)}>
        Anterior
      </Button>
      <span className="muted">Página {page + 1}</span>
      <Button variant="ghost" size="sm" disabled={!hasNext} onClick={() => onChange(page + 1)}>
        Siguiente
      </Button>
    </nav>
  );
}

export function Tabs({ options, value, onChange, label }) {
  return (
    <div className="tabs" role="tablist" aria-label={label}>
      {options.map((option) => (
        <button
          key={option.value}
          type="button"
          role="tab"
          aria-selected={value === option.value}
          className={`tab ${value === option.value ? 'tab-active' : ''}`}
          onClick={() => onChange(option.value)}
        >
          {option.label}
        </button>
      ))}
    </div>
  );
}
