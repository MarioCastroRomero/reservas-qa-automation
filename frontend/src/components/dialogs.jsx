import { useState } from 'react';
import { Button, Field, Modal } from './ui';

export function ReasonDialog({ title, message, confirmLabel, required = false, danger = true, loading, onConfirm, onCancel }) {
  const [reason, setReason] = useState('');
  const [error, setError] = useState('');

  const submit = () => {
    if (required && !reason.trim()) {
      setError('El motivo es obligatorio');
      return;
    }
    onConfirm(reason.trim());
  };

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
          <Button variant={danger ? 'danger' : 'primary'} onClick={submit} loading={loading} data-testid="reason-confirm">
            {confirmLabel}
          </Button>
        </>
      }
    >
      <p className="muted">{message}</p>
      <Field label={required ? 'Motivo' : 'Motivo (opcional)'} error={error}>
        {(p) => (
          <textarea
            {...p}
            rows={3}
            maxLength={500}
            value={reason}
            data-testid="reason-input"
            onChange={(e) => {
              setReason(e.target.value);
              setError('');
            }}
          />
        )}
      </Field>
    </Modal>
  );
}

export function StarRating({ value, onChange, size = 22 }) {
  return (
    <div className="stars" role={onChange ? 'radiogroup' : 'img'} aria-label={`Valoración: ${value} de 5`}>
      {[1, 2, 3, 4, 5].map((n) =>
        onChange ? (
          <button
            key={n}
            type="button"
            role="radio"
            aria-checked={value === n}
            aria-label={`${n} ${n === 1 ? 'estrella' : 'estrellas'}`}
            className={`star ${n <= value ? 'star-on' : ''}`}
            style={{ fontSize: size }}
            data-testid={`star-${n}`}
            onClick={() => onChange(n)}
          >
            ★
          </button>
        ) : (
          <span key={n} className={`star ${n <= Math.round(value) ? 'star-on' : ''}`} style={{ fontSize: size }}>
            ★
          </span>
        ),
      )}
    </div>
  );
}

export function ReviewDialog({ booking, loading, error, onSubmit, onCancel }) {
  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState('');
  const [ratingError, setRatingError] = useState('');

  const submit = () => {
    if (rating < 1) {
      setRatingError('Elige una puntuación de 1 a 5');
      return;
    }
    onSubmit({ rating, comment: comment.trim() || undefined });
  };

  return (
    <Modal
      title={`Valorar ${booking.businessName || 'el negocio'}`}
      onClose={onCancel}
      size="sm"
      footer={
        <>
          <Button variant="ghost" onClick={onCancel} disabled={loading}>
            Volver
          </Button>
          <Button onClick={submit} loading={loading} data-testid="review-submit">
            Enviar valoración
          </Button>
        </>
      }
    >
      <Field label="Puntuación" error={ratingError || error}>
        {() => <StarRating value={rating} onChange={(n) => { setRating(n); setRatingError(''); }} />}
      </Field>
      <Field label="Comentario (opcional)">
        {(p) => (
          <textarea
            {...p}
            rows={3}
            maxLength={500}
            value={comment}
            data-testid="review-comment"
            onChange={(e) => setComment(e.target.value)}
          />
        )}
      </Field>
    </Modal>
  );
}
