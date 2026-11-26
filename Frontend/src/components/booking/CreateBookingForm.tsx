import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { AlertCircle, CalendarPlus, Loader2 } from 'lucide-react';
import { ApiError } from '../../api/apiClient';
import type { Charger } from '../../api/types';
import { useCreateBookingMutation } from '../../hooks/useBookings';
import {
  computeEndIsoFromLocalInput,
  defaultStartInputValue,
  formatLocalDateTime,
  toLocalInputValue,
} from '../../utils/dateUtils';

export interface PrefilledWindow {
  startTime: string;
  durationHours: number;
  nonce: number;
}

interface CreateBookingFormProps {
  chargers: Charger[];
  selectedChargerId: string | null;
  onSelectCharger: (chargerId: string) => void;
  prefilledWindow: PrefilledWindow | null;
}

export function CreateBookingForm({
  chargers,
  selectedChargerId,
  onSelectCharger,
  prefilledWindow,
}: CreateBookingFormProps) {
  const [startLocal, setStartLocal] = useState<string>(defaultStartInputValue);
  const [durationHours, setDurationHours] = useState<number>(2);
  const [clientError, setClientError] = useState<string | null>(null);

  const mutation = useCreateBookingMutation();

  useEffect(() => {
    if (!prefilledWindow) {
      return;
    }
    const localFormatted = toLocalInputValue(prefilledWindow.startTime);
    if (localFormatted) {
      setStartLocal(localFormatted);
    }
    setDurationHours(prefilledWindow.durationHours);
    setClientError(null);
  }, [prefilledWindow]);

  const computedEndIso = useMemo(
    () => computeEndIsoFromLocalInput(startLocal, durationHours),
    [startLocal, durationHours],
  );

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setClientError(null);

    if (!selectedChargerId) {
      setClientError('Wybierz ładowarkę przed utworzeniem rezerwacji.');
      return;
    }

    const startDate = new Date(startLocal);
    if (Number.isNaN(startDate.getTime())) {
      setClientError('Podaj poprawną datę i godzinę rozpoczęcia.');
      return;
    }

    if (startDate.getTime() < Date.now()) {
      setClientError('Rezerwacja nie może zaczynać się w przeszłości.');
      return;
    }

    const endIso = computeEndIsoFromLocalInput(startLocal, durationHours);
    if (!endIso) {
      setClientError('Nie udało się wyliczyć godziny zakończenia.');
      return;
    }

    mutation.mutate({
      chargerId: selectedChargerId,
      startTime: startDate.toISOString(),
      endTime: endIso,
    });
  };

  const serverError =
    mutation.isError && mutation.error instanceof ApiError
      ? mutation.error.status === 409
        ? 'Ten termin koliduje z inną rezerwacją na wybranej ładowarce. Wybierz inny przedział.'
        : mutation.error.detail
      : null;

  const displayedError = clientError || serverError;

  return (
    <form
      onSubmit={handleSubmit}
      className="rounded-xl border border-slate-200/80 bg-slate-50/70 p-4 dark:border-slate-800 dark:bg-slate-950/50"
    >
      <h3 className="mb-3 flex items-center gap-2 text-sm font-semibold text-slate-900 dark:text-slate-100">
        <CalendarPlus className="h-4 w-4 text-emerald-600 dark:text-emerald-400" />
        Nowa rezerwacja
      </h3>

      <div className="grid gap-3 sm:grid-cols-3">
        <div className="sm:col-span-1">
          <label
            htmlFor="booking-charger"
            className="mb-1 block text-xs font-medium text-slate-600 dark:text-slate-400"
          >
            Ładowarka
          </label>
          <select
            id="booking-charger"
            value={selectedChargerId ?? ''}
            onChange={(event) => onSelectCharger(event.target.value)}
            className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 focus:border-emerald-500 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100"
          >
            {chargers.map((charger) => (
              <option key={charger.id} value={charger.id}>
                {charger.name} ({charger.powerKw} kW)
              </option>
            ))}
          </select>
        </div>

        <div className="sm:col-span-1">
          <label
            htmlFor="booking-start"
            className="mb-1 block text-xs font-medium text-slate-600 dark:text-slate-400"
          >
            Data i godzina rozpoczęcia
          </label>
          <input
            id="booking-start"
            type="datetime-local"
            value={startLocal}
            onChange={(event) => {
              setStartLocal(event.target.value);
              setClientError(null);
            }}
            required
            className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 focus:border-emerald-500 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100"
          />
        </div>

        <div className="sm:col-span-1">
          <label
            htmlFor="booking-duration"
            className="mb-1 block text-xs font-medium text-slate-600 dark:text-slate-400"
          >
            Czas trwania (1–6 h)
          </label>
          <select
            id="booking-duration"
            value={durationHours}
            onChange={(event) => setDurationHours(Number(event.target.value))}
            className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 focus:border-emerald-500 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100"
          >
            {[1, 2, 3, 4, 5, 6].map((h) => (
              <option key={h} value={h}>
                {h} {h === 1 ? 'godzina' : h <= 4 ? 'godziny' : 'godzin'}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="mt-3 flex flex-wrap items-center justify-between gap-3">
        <p className="text-xs text-slate-500 dark:text-slate-400">
          Koniec ładowania:{' '}
          <span className="font-semibold text-slate-800 dark:text-slate-200">
            {computedEndIso ? formatLocalDateTime(computedEndIso) : '—'}
          </span>
        </p>

        <button
          type="submit"
          disabled={mutation.isPending || !selectedChargerId}
          className="inline-flex items-center gap-2 rounded-xl bg-emerald-600 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-60 dark:bg-emerald-500 dark:text-slate-950 dark:hover:bg-emerald-400"
        >
          {mutation.isPending ? (
            <>
              <Loader2 className="h-4 w-4 animate-spin" />
              Rezerwowanie...
            </>
          ) : (
            'Zarezerwuj termin'
          )}
        </button>
      </div>

      {displayedError ? (
        <div
          role="alert"
          className="mt-3 flex items-start gap-2 rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-xs text-rose-800 dark:border-rose-900/70 dark:bg-rose-950/60 dark:text-rose-200"
        >
          <AlertCircle className="mt-0.5 h-4 w-4 shrink-0 text-rose-500" />
          <span>{displayedError}</span>
        </div>
      ) : null}
    </form>
  );
}
