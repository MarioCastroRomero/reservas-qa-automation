export const STATUSES = ['PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'REJECTED', 'NO_SHOW'];

export const STATUS_LABELS = {
  PENDING: 'Pendiente',
  CONFIRMED: 'Confirmada',
  COMPLETED: 'Completada',
  CANCELLED: 'Cancelada',
  REJECTED: 'Rechazada',
  NO_SHOW: 'No presentado',
};

const TRANSITIONS = {
  PENDING: ['CONFIRMED', 'CANCELLED', 'REJECTED'],
  CONFIRMED: ['COMPLETED', 'CANCELLED', 'NO_SHOW'],
  COMPLETED: [],
  CANCELLED: [],
  REJECTED: [],
  NO_SHOW: [],
};

export function canTransition(from, to) {
  return (TRANSITIONS[from] || []).includes(to);
}

export function holderActions(status) {
  return {
    canEdit: status === 'PENDING',
    canCancel: canTransition(status, 'CANCELLED'),
  };
}

export function ownerActions(status) {
  return {
    canConfirm: canTransition(status, 'CONFIRMED'),
    canComplete: canTransition(status, 'COMPLETED'),
    canCancel: canTransition(status, 'CANCELLED'),
  };
}

export function ownerExtraActions(booking) {
  return {
    canReject: canTransition(booking.status, 'REJECTED'),
    canNoShow: canTransition(booking.status, 'NO_SHOW') && new Date(booking.bookingTime) <= new Date(),
  };
}

export function canReview(booking) {
  return booking.status === 'COMPLETED' && !booking.reviewed;
}
