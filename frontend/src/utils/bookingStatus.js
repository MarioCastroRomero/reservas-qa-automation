export const STATUSES = ['PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED'];

export const STATUS_LABELS = {
  PENDING: 'Pendiente',
  CONFIRMED: 'Confirmada',
  COMPLETED: 'Completada',
  CANCELLED: 'Cancelada',
};

const TRANSITIONS = {
  PENDING: ['CONFIRMED', 'CANCELLED'],
  CONFIRMED: ['COMPLETED', 'CANCELLED'],
  COMPLETED: [],
  CANCELLED: [],
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
