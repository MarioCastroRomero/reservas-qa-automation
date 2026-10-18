const TRANSLATIONS = {
  'Email or password is incorrect': 'Email o contraseña incorrectos',
  'Email is already registered': 'Ya existe una cuenta con ese email',
  'CIF already exists': 'Ya existe un negocio con ese CIF',
  'Business has pending or confirmed bookings':
    'No se puede eliminar: el negocio tiene reservas pendientes o confirmadas',
  'Owners cannot book their own business': 'No puedes reservar en tu propio negocio',
  'Only pending bookings can be modified': 'Solo se pueden modificar reservas pendientes',
  'Current password is incorrect': 'La contraseña actual no es correcta',
  'New password must be different from the current one': 'La nueva contraseña debe ser distinta de la actual',
  'Business not found': 'Negocio no encontrado',
  'Booking not found': 'Reserva no encontrada',
  'User not found': 'Usuario no encontrado',
  'Invalid UUID': 'Identificador no válido',
  'The resource was modified by someone else, please retry': 'Otra persona ha modificado este elemento. Vuelve a intentarlo.',
  'You do not have permission to perform this action': 'No tienes permiso para hacer esto',
};

export function translate(message) {
  if (!message) return message;
  if (TRANSLATIONS[message]) return TRANSLATIONS[message];
  const transition = /^Cannot change a booking from (\w+) to (\w+)$/.exec(message);
  if (transition) return 'La reserva ya no admite ese cambio de estado. Recarga la página.';
  return message;
}

export function errorMessage(error) {
  if (!error) return '';
  if (error.status === 403 && !error.details?.length) return 'No tienes permiso para hacer esto';
  if (error.status >= 500) return 'Error inesperado del servidor. Inténtalo de nuevo.';
  return translate(error.details?.[0] || error.message) || 'Ha ocurrido un error';
}
