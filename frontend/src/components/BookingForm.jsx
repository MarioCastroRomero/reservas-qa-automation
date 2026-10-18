import { useMemo, useState } from 'react';
import { Alert, Button, Field } from './ui';
import { hasErrors, validateBooking } from '../utils/validation';
import { apiDateTimeToInput, inputToApiDateTime, toDateTimeInput } from '../utils/format';
import { errorMessage } from '../utils/errors';

function defaultTime() {
  const date = new Date();
  date.setDate(date.getDate() + 1);
  date.setHours(12, 0, 0, 0);
  return toDateTimeInput(date);
}

export default function BookingForm({ initial, showPrice = true, submitLabel = 'Reservar', onSubmit, onCancel }) {
  const [values, setValues] = useState(() => ({
    bookingTime: initial?.bookingTime ? apiDateTimeToInput(initial.bookingTime) : defaultTime(),
    partySize: initial?.partySize ?? 2,
    note: initial?.note ?? '',
    price: initial?.price ?? '',
  }));
  const [errors, setErrors] = useState({});
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);
  const minTime = useMemo(() => toDateTimeInput(new Date()), []);

  const set = (field) => (event) => {
    setValues((v) => ({ ...v, [field]: event.target.value }));
    setErrors((e) => ({ ...e, [field]: undefined }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const found = validateBooking(values);
    setErrors(found);
    setFormError('');
    if (hasErrors(found)) return;

    const payload = {
      bookingTime: inputToApiDateTime(values.bookingTime),
      partySize: Number(values.partySize),
      note: values.note.trim() || null,
    };
    if (showPrice && values.price !== '') payload.price = Number(values.price);

    setSaving(true);
    try {
      await onSubmit(payload);
      setSaving(false);
    } catch (error) {
      const fieldErrors = error.fieldErrors || {};
      if (hasErrors(fieldErrors)) setErrors(fieldErrors);
      else setFormError(errorMessage(error));
      setSaving(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} noValidate className="form">
      <Alert>{formError}</Alert>
      <div className="form-row">
        <Field label="Fecha y hora" error={errors.bookingTime}>
          {(p) => <input {...p} type="datetime-local" min={minTime} value={values.bookingTime} onChange={set('bookingTime')} />}
        </Field>
        <Field label="Personas" error={errors.partySize} className="field-narrow">
          {(p) => <input {...p} type="number" min={1} step={1} value={values.partySize} onChange={set('partySize')} />}
        </Field>
      </div>
      {showPrice && (
        <Field label="Precio acordado (opcional)" error={errors.price} hint="En euros. Déjalo vacío si no aplica.">
          {(p) => <input {...p} type="number" min={0} step="0.01" value={values.price} onChange={set('price')} placeholder="0,00" />}
        </Field>
      )}
      <Field label="Nota para el negocio (opcional)" error={errors.note} hint={`${values.note.length}/500`}>
        {(p) => <textarea {...p} rows={3} maxLength={500} value={values.note} onChange={set('note')} placeholder="Alergias, preferencias..." />}
      </Field>
      <div className="form-actions">
        {onCancel && (
          <Button variant="ghost" onClick={onCancel} disabled={saving}>
            Cancelar
          </Button>
        )}
        <Button type="submit" loading={saving}>
          {submitLabel}
        </Button>
      </div>
    </form>
  );
}
