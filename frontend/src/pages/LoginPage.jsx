import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Alert, Button, Field } from '../components/ui';
import AuthLayout from './AuthLayout';
import { hasErrors, validateLogin } from '../utils/validation';
import { errorMessage } from '../utils/errors';

export default function LoginPage() {
  const { login, sessionExpired } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [values, setValues] = useState({ email: '', password: '' });
  const [errors, setErrors] = useState({});
  const [formError, setFormError] = useState('');
  const [loading, setLoading] = useState(false);

  const set = (field) => (event) => {
    setValues((v) => ({ ...v, [field]: event.target.value }));
    setErrors((e) => ({ ...e, [field]: undefined }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const found = validateLogin(values);
    setErrors(found);
    setFormError('');
    if (hasErrors(found)) return;

    setLoading(true);
    try {
      await login(values.email, values.password);
      navigate(location.state?.from || '/negocios', { replace: true });
    } catch (error) {
      setFormError(errorMessage(error));
      setLoading(false);
    }
  };

  return (
    <AuthLayout title="Bienvenido de nuevo" subtitle="Inicia sesión para gestionar tus reservas.">
      {sessionExpired && !formError && <Alert type="info">Tu sesión ha caducado. Vuelve a iniciar sesión.</Alert>}
      <form onSubmit={handleSubmit} noValidate className="form">
        <Alert>{formError}</Alert>
        <Field label="Email" error={errors.email}>
          {(p) => <input {...p} type="email" autoComplete="email" value={values.email} onChange={set('email')} placeholder="tu@email.com" />}
        </Field>
        <Field label="Contraseña" error={errors.password}>
          {(p) => <input {...p} type="password" autoComplete="current-password" value={values.password} onChange={set('password')} />}
        </Field>
        <Button type="submit" loading={loading} className="btn-block">
          Iniciar sesión
        </Button>
      </form>
      <p className="auth-switch">
        ¿No tienes cuenta? <Link to="/registro">Crear una cuenta</Link>
      </p>
    </AuthLayout>
  );
}
