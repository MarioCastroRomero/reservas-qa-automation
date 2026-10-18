import { Link, Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import ProtectedRoute, { GuestRoute } from './components/ProtectedRoute';
import { EmptyState } from './components/ui';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import BusinessesPage from './pages/BusinessesPage';
import BusinessDetailPage from './pages/BusinessDetailPage';
import MyBookingsPage from './pages/MyBookingsPage';
import MyBusinessesPage from './pages/MyBusinessesPage';
import BusinessBookingsPage from './pages/BusinessBookingsPage';
import ProfilePage from './pages/ProfilePage';

function NotFoundPage() {
  return (
    <EmptyState icon="search" title="Página no encontrada" action={<Link to="/negocios" className="btn btn-primary">Ir al inicio</Link>}>
      La dirección que buscas no existe.
    </EmptyState>
  );
}

export default function App() {
  return (
    <Routes>
      <Route element={<GuestRoute />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/registro" element={<RegisterPage />} />
      </Route>
      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route index element={<Navigate to="/negocios" replace />} />
          <Route path="/negocios" element={<BusinessesPage />} />
          <Route path="/negocios/:id" element={<BusinessDetailPage />} />
          <Route path="/mis-reservas" element={<MyBookingsPage />} />
          <Route path="/mis-negocios" element={<MyBusinessesPage />} />
          <Route path="/mis-negocios/:id/reservas" element={<BusinessBookingsPage />} />
          <Route path="/perfil" element={<ProfilePage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Route>
    </Routes>
  );
}
