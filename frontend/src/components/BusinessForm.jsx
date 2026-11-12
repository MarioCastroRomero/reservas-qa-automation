import { useState } from 'react';
import { Alert, Button, Field } from './ui';
import { hasErrors, validateBusiness } from '../utils/validation';
import { errorMessage } from '../utils/errors';

const EMPTY = { name: '', cif: '', address: '' };

export default function BusinessForm({ initial, submitLabel = 'Guardar', onSubmit, onCancel }) {
  const [values, setValues] = useState({ ...EMPTY, ...initial });
  const [errors, setErrors] = useState({});
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  const set = (field) => (event) => {
    setValues((v) => ({ ...v, [field]: event.target.value }));
    setErrors((e) => ({ ...e, [field]: undefined }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const found = validateBusiness(values);
    const capacity = String(values.maxCapacityPerSlot ?? '').trim();
    if (capacity && (!/^\d+$/.test(capacity) || Number(capacity) < 1)) {
      found.maxCapacityPerSlot = 'El aforo debe ser un número entero mayor que 0';
    }
    setErrors(found);
    setFormError('');
    if (hasErrors(found)) return;

    setSaving(true);
    try {
      const capacity = String(values.maxCapacityPerSlot ?? '').trim();
      await onSubmit({
        name: values.name.trim(),
        cif: values.cif.trim().toUpperCase(),
        address: values.address.trim(),
        maxCapacityPerSlot: capacity ? Number(capacity) : null,
      });
    } catch (error) {
      const fieldErrors = error.fieldErrors || {};
      if (error.status === 409 && /CIF/i.test(error.details?.[0] || '')) setErrors({ cif: errorMessage(error) });
      else if (hasErrors(fieldErrors)) setErrors(fieldErrors);
      else setFormError(errorMessage(error));
      setSaving(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} noValidate className="form">
      <Alert>{formError}</Alert>
      <Field label="Nombre del negocio" error={errors.name}>
        {(p) => <input {...p} value={values.name} onChange={set('name')} maxLength={100} placeholder="Ej.: Peluquería Rizos" />}
      </Field>
      <Field label="CIF" error={errors.cif} hint="Letra + 7 dígitos + dígito o letra (ej.: B12345678)">
        {(p) => (
          <input
            {...p}
            value={values.cif}
            onChange={set('cif')}
            maxLength={9}
            placeholder="B12345678"
            autoCapitalize="characters"
            className="uppercase"
          />
        )}
      </Field>
      <Field label="Dirección" error={errors.address}>
        {(p) => <input {...p} value={values.address} onChange={set('address')} maxLength={150} placeholder="Calle Mayor 10, Madrid" />}
      </Field>
      <Field label="Aforo máximo por hora" error={errors.maxCapacityPerSlot} hint="Opcional. Personas totales por franja horaria; vacío = sin límite">
        {(p) => (
          <input
            {...p}
            type="number"
            min={1}
            inputMode="numeric"
            value={values.maxCapacityPerSlot ?? ''}
            onChange={set('maxCapacityPerSlot')}
            placeholder="Sin límite"
          />
        )}
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
