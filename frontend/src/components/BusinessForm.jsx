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
    setErrors(found);
    setFormError('');
    if (hasErrors(found)) return;

    setSaving(true);
    try {
      await onSubmit({ name: values.name.trim(), cif: values.cif.trim().toUpperCase(), address: values.address.trim() });
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
