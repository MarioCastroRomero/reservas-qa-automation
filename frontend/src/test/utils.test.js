import { describe, expect, it } from 'vitest';
import { canTransition, holderActions, ownerActions } from '../utils/bookingStatus';
import { validateBooking, validateBusiness, validateRegister } from '../utils/validation';
import { apiDateTimeToInput, inputToApiDateTime } from '../utils/format';
import { errorMessage } from '../utils/errors';
import { ApiError } from '../api/client';

describe('booking status rules (same as backend BookingStatus)', () => {
  it('allows only the backend transitions', () => {
    expect(canTransition('PENDING', 'CONFIRMED')).toBe(true);
    expect(canTransition('PENDING', 'COMPLETED')).toBe(false);
    expect(canTransition('CONFIRMED', 'COMPLETED')).toBe(true);
    expect(canTransition('COMPLETED', 'CANCELLED')).toBe(false);
    expect(canTransition('CANCELLED', 'CONFIRMED')).toBe(false);
  });

  it('exposes the right actions for holder and owner', () => {
    expect(holderActions('PENDING')).toEqual({ canEdit: true, canCancel: true });
    expect(holderActions('CONFIRMED')).toEqual({ canEdit: false, canCancel: true });
    expect(holderActions('COMPLETED')).toEqual({ canEdit: false, canCancel: false });
    expect(ownerActions('PENDING')).toEqual({ canConfirm: true, canComplete: false, canCancel: true });
    expect(ownerActions('CONFIRMED')).toEqual({ canConfirm: false, canComplete: true, canCancel: true });
  });
});

describe('validation', () => {
  const validUser = {
    name: 'Ana',
    lastName: 'Pérez',
    email: 'ana@example.com',
    birthDate: '1990-01-01',
    password: 'Valid123!',
    confirmPassword: 'Valid123!',
  };

  it('accepts a valid registration and rejects weak passwords and future birth dates', () => {
    expect(validateRegister(validUser)).toEqual({});
    expect(validateRegister({ ...validUser, password: 'weak', confirmPassword: 'weak' }).password).toBeDefined();
    expect(validateRegister({ ...validUser, birthDate: '2999-01-01' }).birthDate).toBeDefined();
    expect(validateRegister({ ...validUser, confirmPassword: 'Other123!' }).confirmPassword).toBeDefined();
  });

  it('checks the CIF and address formats', () => {
    expect(validateBusiness({ name: 'Shop', cif: 'b12345678', address: 'Calle Mayor 10' })).toEqual({});
    expect(validateBusiness({ name: 'Shop', cif: 'X1234', address: 'Calle Mayor 10' }).cif).toBeDefined();
    expect(validateBusiness({ name: 'Shop', cif: 'B12345678', address: 'abc' }).address).toBeDefined();
  });

  it('requires a future time and at least one person', () => {
    expect(validateBooking({ bookingTime: '2999-01-01T10:00', partySize: 2, note: '' })).toEqual({});
    expect(validateBooking({ bookingTime: '2000-01-01T10:00', partySize: 2 }).bookingTime).toBeDefined();
    expect(validateBooking({ bookingTime: '2999-01-01T10:00', partySize: 0 }).partySize).toBeDefined();
    expect(validateBooking({ bookingTime: '2999-01-01T10:00', partySize: 1, price: -1 }).price).toBeDefined();
  });
});

describe('date conversion', () => {
  it('round-trips LocalDateTime and datetime-local values without time zone shifts', () => {
    expect(apiDateTimeToInput('2026-10-20T19:30:00')).toBe('2026-10-20T19:30');
    expect(inputToApiDateTime('2026-10-20T19:30')).toBe('2026-10-20T19:30:00');
  });
});

describe('error messages', () => {
  it('translates known backend messages', () => {
    expect(errorMessage(new ApiError(409, 'Conflict', ['CIF already exists']))).toBe('Ya existe un negocio con ese CIF');
    expect(errorMessage(new ApiError(500, 'Internal server error', ['Unexpected error']))).toMatch(/servidor/);
  });
});
