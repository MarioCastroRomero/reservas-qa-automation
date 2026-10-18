const dateTimeFormat = new Intl.DateTimeFormat('es-ES', {
  weekday: 'short',
  day: 'numeric',
  month: 'short',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
});

const dateFormat = new Intl.DateTimeFormat('es-ES', { day: 'numeric', month: 'long', year: 'numeric' });

const currencyFormat = new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' });

export function formatDateTime(value) {
  if (!value) return '';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : dateTimeFormat.format(date);
}

export function formatDate(value) {
  if (!value) return '';
  const date = new Date(`${value}T00:00:00`);
  return Number.isNaN(date.getTime()) ? value : dateFormat.format(date);
}

export function formatPrice(value) {
  if (value === null || value === undefined || value === '') return '';
  return currencyFormat.format(Number(value));
}

function pad(n) {
  return String(n).padStart(2, '0');
}

export function toDateTimeInput(date) {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export function apiDateTimeToInput(value) {
  return value ? value.slice(0, 16) : '';
}

export function inputToApiDateTime(value) {
  return value && value.length === 16 ? `${value}:00` : value;
}

export function todayInput() {
  const now = new Date();
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`;
}

export function initials(name = '') {
  return name
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0].toUpperCase())
    .join('');
}
