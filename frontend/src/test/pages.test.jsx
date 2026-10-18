import { describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import App from '../App';
import { AuthProvider } from '../context/AuthContext';
import { ToastProvider } from '../context/ToastContext';

function json(status, body) {
  return new Response(body === undefined ? null : JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } });
}

function renderApp(path) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <ToastProvider>
        <AuthProvider>
          <App />
        </AuthProvider>
      </ToastProvider>
    </MemoryRouter>,
  );
}

const AUTH = { token: 't', type: 'Bearer', uuid: 'u1', email: 'ana@example.com', name: 'Ana', lastName: 'Pérez', role: 'USER' };

describe('routing and auth', () => {
  it('redirects anonymous visitors to the login page', () => {
    renderApp('/mis-reservas');
    expect(screen.getByRole('heading', { name: 'Bienvenido de nuevo' })).toBeInTheDocument();
  });

  it('validates the login form before calling the API', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch');
    renderApp('/login');
    await userEvent.click(screen.getByRole('button', { name: 'Iniciar sesión' }));
    expect(screen.getByText('Introduce tu email')).toBeInTheDocument();
    expect(fetchSpy).not.toHaveBeenCalled();
  });

  it('shows the backend error for wrong credentials', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      json(401, { status: 401, error: 'Invalid credentials', details: ['Email or password is incorrect'] }),
    );
    renderApp('/login');
    await userEvent.type(screen.getByLabelText('Email'), 'ana@example.com');
    await userEvent.type(screen.getByLabelText('Contraseña'), 'Wrong123!');
    await userEvent.click(screen.getByRole('button', { name: 'Iniciar sesión' }));
    expect(await screen.findByText('Email o contraseña incorrectos')).toBeInTheDocument();
  });

  it('logs in and shows the business catalog', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(async (url) => {
      if (url.endsWith('/auth/login')) return json(200, AUTH);
      if (url.includes('/user/me')) return json(200, { ...AUTH, birthDate: '1990-01-01' });
      if (url.includes('/business?')) {
        return json(200, [{ uuid: 'b1', name: 'Restaurante Sol', address: 'Avenida del Puerto 3', cif: 'B12345678', ownerId: 'o1', ownerName: 'Olga' }]);
      }
      return json(404, {});
    });

    renderApp('/login');
    await userEvent.type(screen.getByLabelText('Email'), 'ana@example.com');
    await userEvent.type(screen.getByLabelText('Contraseña'), 'Valid123!');
    await userEvent.click(screen.getByRole('button', { name: 'Iniciar sesión' }));

    expect(await screen.findByText('Restaurante Sol')).toBeInTheDocument();
    expect(screen.getByText('Publicar negocio')).toBeInTheDocument();
    expect(localStorage.getItem('reservas.token')).toBe('t');
  });

  it('opens the mobile menu and closes it with Escape or an outside click', async () => {
    localStorage.setItem('reservas.token', 't');
    localStorage.setItem('reservas.user', JSON.stringify(AUTH));
    vi.spyOn(globalThis, 'fetch').mockImplementation(async (url) =>
      url.includes('/user/me') ? json(200, AUTH) : json(200, []),
    );
    renderApp('/negocios');

    const toggle = screen.getByRole('button', { name: 'Abrir menú' });
    const nav = screen.getByRole('navigation', { name: 'Principal' });
    expect(nav).not.toHaveClass('nav-open');

    await userEvent.click(toggle);
    expect(nav).toHaveClass('nav-open');
    expect(toggle).toHaveAttribute('aria-expanded', 'true');

    await userEvent.keyboard('{Escape}');
    expect(nav).not.toHaveClass('nav-open');

    await userEvent.click(toggle);
    await userEvent.click(document.querySelector('main'));
    expect(nav).not.toHaveClass('nav-open');
  });

  it('shows owner actions in the received bookings panel', async () => {
    localStorage.setItem('reservas.token', 't');
    localStorage.setItem('reservas.user', JSON.stringify({ ...AUTH, role: 'BUSINESS_OWNER' }));
    vi.spyOn(globalThis, 'fetch').mockImplementation(async (url) => {
      if (url.includes('/user/me')) return json(200, { ...AUTH, role: 'BUSINESS_OWNER' });
      if (url.includes('/business/b1/bookings')) {
        return json(200, [
          { uuid: 'k1', businessId: 'b1', status: 'PENDING', bookingTime: '2999-05-10T11:00:00', partySize: 2, holderName: 'Carlos Ruiz' },
          { uuid: 'k2', businessId: 'b1', status: 'CONFIRMED', bookingTime: '2999-05-11T11:00:00', partySize: 1, holderName: 'Sara Gil' },
        ]);
      }
      if (url.includes('/business/b1')) return json(200, { uuid: 'b1', name: 'Shop', address: 'Main Street 12', ownerId: 'u1' });
      return json(404, {});
    });

    renderApp('/mis-negocios/b1/reservas');

    expect(await screen.findByText('Carlos Ruiz')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Confirmar' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Completar' })).toBeInTheDocument();
    await waitFor(() => expect(screen.getByText('Shop - Main Street 12')).toBeInTheDocument());
  });
});
