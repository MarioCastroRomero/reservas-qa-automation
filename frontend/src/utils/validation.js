export const PASSWORD_REGEX = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9])(?=\S+$).{8,20}$/;
export const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
export const CIF_REGEX = /^[ABCDEFGHJNPQRSUVW][0-9]{7}[0-9A-J]$/;
export const ADDRESS_REGEX = /^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ\s,.\-/ºª#()]{5,150}$/;

export const PASSWORD_HINT = 'Entre 8 y 20 caracteres, con mayúscula, minúscula, número y un carácter especial.';

function required(value) {
  return value !== undefined && value !== null && String(value).trim() !== '';
}

export function validateLogin({ email, password }) {
  const errors = {};
  if (!required(email)) errors.email = 'Introduce tu email';
  else if (!EMAIL_REGEX.test(email.trim())) errors.email = 'El email no tiene un formato válido';
  if (!required(password)) errors.password = 'Introduce tu contraseña';
  return errors;
}

export function validateProfile({ name, lastName, birthDate }) {
  const errors = {};
  if (!required(name)) errors.name = 'El nombre es obligatorio';
  else if (name.trim().length > 50) errors.name = 'Máximo 50 caracteres';
  if (!required(lastName)) errors.lastName = 'Los apellidos son obligatorios';
  else if (lastName.trim().length > 50) errors.lastName = 'Máximo 50 caracteres';
  if (!required(birthDate)) errors.birthDate = 'La fecha de nacimiento es obligatoria';
  else if (new Date(`${birthDate}T00:00:00`) >= new Date(new Date().toDateString()))
    errors.birthDate = 'La fecha debe ser anterior a hoy';
  return errors;
}

export function validatePassword(password) {
  if (!required(password)) return 'La contraseña es obligatoria';
  if (!PASSWORD_REGEX.test(password)) return PASSWORD_HINT;
  return null;
}

export function validateRegister(data) {
  const errors = validateProfile(data);
  if (!required(data.email)) errors.email = 'El email es obligatorio';
  else if (!EMAIL_REGEX.test(data.email.trim())) errors.email = 'El email no tiene un formato válido';
  else if (data.email.trim().length > 150) errors.email = 'Máximo 150 caracteres';
  const passwordError = validatePassword(data.password);
  if (passwordError) errors.password = passwordError;
  if (data.password !== data.confirmPassword) errors.confirmPassword = 'Las contraseñas no coinciden';
  return errors;
}

export function validateBusiness({ name, cif, address }) {
  const errors = {};
  if (!required(name)) errors.name = 'El nombre es obligatorio';
  else if (name.trim().length > 100) errors.name = 'Máximo 100 caracteres';
  if (!required(cif)) errors.cif = 'El CIF es obligatorio';
  else if (!CIF_REGEX.test(cif.trim().toUpperCase())) errors.cif = 'CIF no válido (ej.: B12345678)';
  if (!required(address)) errors.address = 'La dirección es obligatoria';
  else if (!ADDRESS_REGEX.test(address.trim()))
    errors.address = 'Entre 5 y 150 caracteres; solo letras, números y , . - / º ª # ( )';
  return errors;
}

export function validateBooking({ bookingTime, partySize, note, price }) {
  const errors = {};
  if (!required(bookingTime)) errors.bookingTime = 'Elige fecha y hora';
  else if (new Date(bookingTime) <= new Date()) errors.bookingTime = 'La fecha debe ser futura';
  const size = Number(partySize);
  if (!required(partySize) || !Number.isInteger(size) || size < 1) errors.partySize = 'Mínimo 1 persona';
  if (note && note.length > 500) errors.note = 'Máximo 500 caracteres';
  if (required(price) && (Number.isNaN(Number(price)) || Number(price) < 0)) errors.price = 'El precio no puede ser negativo';
  return errors;
}

export function hasErrors(errors) {
  return Object.keys(errors).length > 0;
}
