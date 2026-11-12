import { useEffect, useState } from 'react';
import { businessApi } from '../api/endpoints';
import { errorMessage } from '../utils/errors';
import { Alert, Button, Spinner } from './ui';

export const DAYS = [
  ['MONDAY', 'Lunes'],
  ['TUESDAY', 'Martes'],
  ['WEDNESDAY', 'Miércoles'],
  ['THURSDAY', 'Jueves'],
  ['FRIDAY', 'Viernes'],
  ['SATURDAY', 'Sábado'],
  ['SUNDAY', 'Domingo'],
];

const hhmm = (time) => (time ? time.slice(0, 5) : '');

export default function OpeningHoursEditor({ businessId, onSaved, onCancel }) {
  const [rows, setRows] = useState(null);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    let alive = true;
    businessApi
      .openingHours(businessId)
      .then((slots) => {
        if (!alive) return;
        const byDay = Object.fromEntries(slots.map((s) => [s.dayOfWeek, s]));
        setRows(
          DAYS.map(([day]) => ({
            day,
            open: Boolean(byDay[day]),
            openTime: hhmm(byDay[day]?.openTime) || '09:00',
            closeTime: hhmm(byDay[day]?.closeTime) || '18:00',
          })),
        );
      })
      .catch((err) => alive && setError(errorMessage(err)));
    return () => {
      alive = false;
    };
  }, [businessId]);

  const update = (day, patch) => setRows((list) => list.map((r) => (r.day === day ? { ...r, ...patch } : r)));

  const save = async () => {
    setError('');
    const invalid = rows.find((r) => r.open && r.openTime >= r.closeTime);
    if (invalid) {
      setError('La hora de apertura debe ser anterior a la de cierre');
      return;
    }
    setSaving(true);
    try {
      const slots = rows
        .filter((r) => r.open)
        .map((r) => ({ dayOfWeek: r.day, openTime: r.openTime, closeTime: r.closeTime }));
      await businessApi.saveOpeningHours(businessId, slots);
      onSaved(slots);
    } catch (err) {
      setError(errorMessage(err));
      setSaving(false);
    }
  };

  if (!rows) return error ? <Alert>{error}</Alert> : <Spinner label="Cargando horario..." />;

  return (
    <div className="form" data-testid="opening-hours-editor">
      <Alert>{error}</Alert>
      <p className="muted small">Sin ningún día marcado, el negocio acepta reservas a cualquier hora.</p>
      {rows.map((row) => {
        const label = DAYS.find(([d]) => d === row.day)[1];
        return (
          <div key={row.day} className="hours-row" data-testid={`hours-${row.day}`}>
            <label className="hours-day">
              <input
                type="checkbox"
                checked={row.open}
                onChange={(e) => update(row.day, { open: e.target.checked })}
                aria-label={`${label} abierto`}
              />
              <span>{label}</span>
            </label>
            <input
              type="time"
              value={row.openTime}
              disabled={!row.open}
              aria-label={`${label} apertura`}
              onChange={(e) => update(row.day, { openTime: e.target.value })}
            />
            <span aria-hidden="true">-</span>
            <input
              type="time"
              value={row.closeTime}
              disabled={!row.open}
              aria-label={`${label} cierre`}
              onChange={(e) => update(row.day, { closeTime: e.target.value })}
            />
          </div>
        );
      })}
      <div className="form-actions">
        <Button variant="ghost" onClick={onCancel} disabled={saving}>
          Cancelar
        </Button>
        <Button onClick={save} loading={saving} data-testid="hours-save">
          Guardar horario
        </Button>
      </div>
    </div>
  );
}
