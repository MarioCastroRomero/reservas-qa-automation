import { useState } from 'react';
import { userApi } from '../api/endpoints';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { useLoader } from '../hooks/useLoader';
import { Alert, Button, Field, Spinner } from '../components/ui';
import { PASSWORD_HINT, hasErrors, validatePassword, validateProfile } from '../utils/validation';
import { errorMessage } from '../utils/errors';
import { formatDate, initials, todayInput } from '../utils/format';

const ROLE_LABELS = { USER: 'Cliente', BUSINESS_OWNER: 'Propietario de negocio', ADMIN: 'Administrador' };

function ProfileForm({ profile, onSaved }) {
  const toast = useToast();
  const [values, setValues] = useState({ name: profile.name, lastName: profile.lastName || '', birthDate: profile.birthDate });
  const [errors, setErrors] = useState({});
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  const set = (field) => (event) => {
    setValues((v) => ({ ...v, [field]: event.target.value }));
    setErrors((e) => ({ ...e, [field]: undefined }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const found = validateProfile(values);
    setErrors(found);
    setFormError('');
    if (hasErrors(found)) return;
    setSaving(true);
    try {
      const updated = await userApi.updateMe({ name: values.name.trim(), lastName: values.lastName.trim(), birthDate: values.birthDate });
      onSaved(updated);
      toast.success('Perfil actualizado');
    } catch (error) {
      const fieldErrors = error.fieldErrors || {};
      if (hasErrors(fieldErrors)) setErrors(fieldErrors);
      else setFormError(errorMessage(error));
    } finally {
      setSaving(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} noValidate className="form">
      <Alert>{formError}</Alert>
      <div className="form-row">
        <Field label="Nombre" error={errors.name}>
          {(p) => <input {...p} maxLength={50} value={values.name} onChange={set('name')} />}
        </Field>
        <Field label="Apellidos" error={errors.lastName}>
          {(p) => <input {...p} maxLength={50} value={values.lastName} onChange={set('lastName')} />}
        </Field>
      </div>
      <Field label="Email" hint="El email no se puede cambiar.">
        {(p) => <input {...p} value={profile.email} disabled readOnly />}
      </Field>
      <Field label="Fecha de nacimiento" error={errors.birthDate}>
        {(p) => <input {...p} type="date" max={todayInput()} value={values.birthDate} onChange={set('birthDate')} />}
      </Field>
      <div className="form-actions">
        <Button type="submit" loading={saving}>
          Guardar cambios
        </Button>
      </div>
    </form>
  );
}

function PasswordForm() {
  const toast = useToast();
  const [values, setValues] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [errors, setErrors] = useState({});
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  const set = (field) => (event) => {
    setValues((v) => ({ ...v, [field]: event.target.value }));
    setErrors((e) => ({ ...e, [field]: undefined }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const found = {};
    if (!values.currentPassword) found.currentPassword = 'Introduce tu contraseña actual';
    const passwordError = validatePassword(values.newPassword);
    if (passwordError) found.newPassword = passwordError;
    if (values.newPassword !== values.confirmPassword) found.confirmPassword = 'Las contraseñas no coinciden';
    setErrors(found);
    setFormError('');
    if (hasErrors(found)) return;

    setSaving(true);
    try {
      await userApi.changePassword(values.currentPassword, values.newPassword);
      setValues({ currentPassword: '', newPassword: '', confirmPassword: '' });
      toast.success('Contraseña cambiada');
    } catch (error) {
      if (error.details?.[0] === 'Current password is incorrect') setErrors({ currentPassword: errorMessage(error) });
      else if (hasErrors(error.fieldErrors || {})) setErrors(error.fieldErrors);
      else setFormError(errorMessage(error));
    } finally {
      setSaving(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} noValidate className="form">
      <Alert>{formError}</Alert>
      <Field label="Contraseña actual" error={errors.currentPassword}>
        {(p) => <input {...p} type="password" autoComplete="current-password" value={values.currentPassword} onChange={set('currentPassword')} />}
      </Field>
      <Field label="Nueva contraseña" error={errors.newPassword} hint={PASSWORD_HINT}>
        {(p) => <input {...p} type="password" autoComplete="new-password" maxLength={20} value={values.newPassword} onChange={set('newPassword')} />}
      </Field>
      <Field label="Repite la nueva contraseña" error={errors.confirmPassword}>
        {(p) => <input {...p} type="password" autoComplete="new-password" maxLength={20} value={values.confirmPassword} onChange={set('confirmPassword')} />}
      </Field>
      <div className="form-actions">
        <Button type="submit" variant="secondary" icon="lock" loading={saving}>
          Cambiar contraseña
        </Button>
      </div>
    </form>
  );
}

export default function ProfilePage() {
  const { setUser } = useAuth();
  const { data: profile, loading, error, reload, setData } = useLoader(() => userApi.me(), []);

  if (loading && !profile) return <Spinner />;
  if (error) return <Alert onRetry={reload}>{errorMessage(error)}</Alert>;

  const fullName = [profile.name, profile.lastName].filter(Boolean).join(' ');

  const handleSaved = (updated) => {
    setData(updated);
    setUser({ uuid: updated.uuid, email: updated.email, name: updated.name, lastName: updated.lastName, role: updated.role });
  };

  return (
    <div className="page">
      <section className="card profile-head">
        <span className="avatar avatar-lg">{initials(fullName)}</span>
        <div>
          <h1>{fullName}</h1>
          <p className="muted">
            {profile.email} - <span className="pill">{ROLE_LABELS[profile.role] || profile.role}</span>
          </p>
          <p className="muted small">Fecha de nacimiento: {formatDate(profile.birthDate)}</p>
        </div>
      </section>

      <div className="two-columns">
        <section className="card">
          <h2 className="card-title">Datos personales</h2>
          <ProfileForm key={profile.uuid + profile.name + profile.lastName + profile.birthDate} profile={profile} onSaved={handleSaved} />
        </section>
        <section className="card">
          <h2 className="card-title">Seguridad</h2>
          <PasswordForm />
        </section>
      </div>
    </div>
  );
}
