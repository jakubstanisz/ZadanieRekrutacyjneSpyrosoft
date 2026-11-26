import { useState } from 'react';
import { ArrowRight, BatteryCharging, Clock, Sparkles } from 'lucide-react';
import { ApiError } from '../../api/apiClient';
import { useChargingWindow } from '../../hooks/useChargingWindow';
import { formatLocalDateTime } from '../../utils/dateUtils';
import { Badge } from '../ui/Badge';
import { Card, CardHeader } from '../ui/Card';
import { ErrorAlert, Skeleton } from '../ui/Feedback';

interface ChargingWindowOptimizerProps {
  onApplyWindowToBooking: (startIso: string, durationHours: number) => void;
}

export function ChargingWindowOptimizer({ onApplyWindowToBooking }: ChargingWindowOptimizerProps) {
  const [hours, setHours] = useState<number>(3);
  const { data, isPending, isError, error, refetch } = useChargingWindow(hours);

  const errorMessage =
    error instanceof ApiError
      ? error.detail
      : 'Nie udało się wyznaczyć optymalnego okna ładowania.';

  return (
    <Card className="flex h-full flex-col justify-between">
      <div>
        <CardHeader
          icon={<Sparkles className="h-5 w-5" />}
          title="Najlepsze okno ładowania"
          subtitle="Ciągły przedział w prognozie na jutro i pojutrze (może przechodzić przez północ)"
        />

        <div className="rounded-xl border border-slate-200/80 bg-slate-50/70 p-4 dark:border-slate-800 dark:bg-slate-950/60">
          <div className="mb-2 flex items-center justify-between">
            <label htmlFor="charging-hours-slider" className="text-sm font-medium text-slate-700 dark:text-slate-300">
              Czas ładowania
            </label>
            <span className="inline-flex items-center gap-1 rounded-lg bg-emerald-100 px-2.5 py-0.5 text-sm font-bold tabular-nums text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300">
              <Clock className="h-3.5 w-3.5" />
              {hours} {hours === 1 ? 'godzina' : hours <= 4 ? 'godziny' : 'godzin'}
            </span>
          </div>

          <input
            id="charging-hours-slider"
            type="range"
            min={1}
            max={6}
            step={1}
            value={hours}
            onChange={(event) => setHours(Number(event.target.value))}
            className="h-2 w-full cursor-pointer appearance-none rounded-lg bg-slate-200 accent-emerald-600 dark:bg-slate-800 dark:accent-emerald-500"
          />

          <div className="mt-1.5 flex justify-between text-[11px] font-medium text-slate-400">
            {[1, 2, 3, 4, 5, 6].map((step) => (
              <button
                key={step}
                type="button"
                onClick={() => setHours(step)}
                className={`rounded px-1.5 py-0.5 transition ${
                  hours === step
                    ? 'bg-emerald-500/15 font-bold text-emerald-600 dark:text-emerald-400'
                    : 'hover:text-slate-600 dark:hover:text-slate-200'
                }`}
              >
                {step}h
              </button>
            ))}
          </div>
        </div>

        <div className="mt-4">
          {isPending ? (
            <Skeleton className="h-40 w-full" />
          ) : isError ? (
            <ErrorAlert
              title="Nie udało się wyliczyć okna"
              message={errorMessage}
              onRetry={() => void refetch()}
            />
          ) : data ? (
            <div className="rounded-xl border border-emerald-200 bg-gradient-to-br from-emerald-50/90 to-teal-50/60 p-4 dark:border-emerald-800/70 dark:from-emerald-950/50 dark:to-teal-950/30">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <span className="inline-flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wider text-emerald-700 dark:text-emerald-300">
                  <BatteryCharging className="h-4 w-4" />
                  Rekomendowany termin
                </span>
                {data.partialData ? <Badge variant="amber">Prognoza częściowa</Badge> : null}
              </div>

              <div className="mt-3 grid gap-2 sm:grid-cols-2">
                <div className="rounded-lg bg-white/80 p-2.5 dark:bg-slate-900/70">
                  <p className="text-[11px] font-medium text-slate-500 dark:text-slate-400">Start</p>
                  <p className="mt-0.5 text-sm font-bold text-slate-900 dark:text-slate-100">
                    {formatLocalDateTime(data.startTime)}
                  </p>
                </div>
                <div className="rounded-lg bg-white/80 p-2.5 dark:bg-slate-900/70">
                  <p className="text-[11px] font-medium text-slate-500 dark:text-slate-400">Koniec</p>
                  <p className="mt-0.5 text-sm font-bold text-slate-900 dark:text-slate-100">
                    {formatLocalDateTime(data.endTime)}
                  </p>
                </div>
              </div>

              <div className="mt-3 flex items-center justify-between border-t border-emerald-200/70 pt-3 dark:border-emerald-800/50">
                <span className="text-xs text-slate-600 dark:text-slate-300">Średni udział czystej energii</span>
                <span className="text-xl font-extrabold tabular-nums text-emerald-600 dark:text-emerald-400">
                  {data.cleanEnergyPercentage.toFixed(1)}%
                </span>
              </div>
            </div>
          ) : null}
        </div>
      </div>

      {data ? (
        <button
          type="button"
          onClick={() => onApplyWindowToBooking(data.startTime, hours)}
          className="mt-4 inline-flex w-full items-center justify-center gap-2 rounded-xl bg-emerald-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-emerald-700 active:scale-[0.99] dark:bg-emerald-500 dark:text-slate-950 dark:hover:bg-emerald-400"
        >
          Zarezerwuj to okno
          <ArrowRight className="h-4 w-4" />
        </button>
      ) : null}
    </Card>
  );
}
