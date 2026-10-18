import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { Alert, Button, Field } from '../components/ui';
import AuthLayout from './AuthLayout';
import { PASSWORD_HINT, hasErrors, validateRegister } from '../utils/validation';
import { errorMessage } from '../utils/errors';
import { todayInput } from '../utils/format';

const EMPTY = { name: '', lastName: '', email: '', birthDate: '', password: '', confirmPassword: '' };

export default function RegisterPage() {
  const { register } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const [values, setValues] = useState(EMPTY);
  const [errors, setErrors] = useState({});
  const [formError, setFormError] = useState('');
  const [loading, setLoading] = useState(false);

  const set = (field) => (event) => {
    setValues((v) => ({ ...v, [field]: event.target.value }));
    setErrors((e) => ({ ...e, [field]: undefined }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const found = validateRegister(values);
    setErrors(found);
    setFormError('');
    if (hasErrors(found)) return;

    setLoading(true);
    try {
      await register({
        name: values.name.trim(),
        lastName: values.lastName.trim(),
        email: values.email.trim(),
        birthDate: values.birthDate,
        password: values.password,
      });
      toast.success(`¡Cuenta creada! Hola, ${values.name.trim()}.`);
      navigate('/negocios', { replace: true });
    } catch (error) {
      const fieldErrors = error.fieldErrors || {};
      if (error.status === 409) setErrors({ email: errorMessage(error) });
      else if (hasErrors(fieldErrors)) setErrors(fieldErrors);
      else setFormError(errorMessage(error));
      setLoading(false);
    }
  };

  return (
    <AuthLayout title="Crea tu cuenta" subtitle="Solo te llevará un minuto.">
      <form onSubmit={handleSubmit} noValidate className="form">
        <Alert>{formError}</Alert>
        <div className="form-row">
          <Field label="Nombre" error={errors.name}>
            {(p) => <input {...p} autoComplete="given-name" maxLength={50} value={values.name} onChange={set('name')} />}
          </Field>
          <Field label="Apellidos" error={errors.lastName}>
            {(p) => <input {...p} autoComplete="family-name" maxLength={50} value={values.lastName} onChange={set('lastName')} />}
          </Field>
        </div>
        <Field label="Email" error={errors.email}>
          {(p) => <input {...p} type="email" autoComplete="email" maxLength={150} value={values.email} onChange={set('email')} placeholder="tu@email.com" />}
        </Field>
        <Field label="Fecha de nacimiento" error={errors.birthDate}>
          {(p) => <input {...p} type="date" max={todayInput()} value={values.birthDate} onChange={set('birthDate')} />}
        </Field>
        <Field label="Contraseña" error={errors.password} hint={PASSWORD_HINT}>
          {(p) => <input {...p} type="password" autoComplete="new-password" maxLength={20} value={values.password} onChange={set('password')} />}
        </Field>
        <Field label="Repite la contraseña" error={errors.confirmPassword}>
          {(p) => (
            <input {...p} type="password" autoComplete="new-password" maxLength={20} value={values.confirmPassword} onChange={set('confirmPassword')} />
          )}
        </Field>
        <Button type="submit" loading={loading} className="btn-block">
          Crear cuenta
        </Button>
      </form>
      <p className="auth-switch">
        ¿Ya tienes cuenta? <Link to="/login">Inicia sesión</Link>
      </p>
    </AuthLayout>
  );
}
