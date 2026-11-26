import { Leaf, RefreshCw } from 'lucide-react';
import { ApiError } from '../../api/apiClient';
import { useEnergyMix } from '../../hooks/useEnergyMix';
import { Card, CardHeader } from '../ui/Card';
import { ErrorAlert, Skeleton } from '../ui/Feedback';
import { DayMixSummaryCard } from './DayMixSummaryCard';
import { EnergyMixChart } from './EnergyMixChart';

export function EnergyMixSection() {
  const { data, isPending, isError, error, refetch, isFetching } = useEnergyMix(3);

  const errorMessage =
    error instanceof ApiError
      ? error.detail
      : 'Nie udało się pobrać danych o miksie energetycznym Wielkiej Brytanii.';

  return (
    <Card>
      <CardHeader
        icon={<Leaf className="h-5 w-5" />}
        title="Miks energetyczny Wielkiej Brytanii"
        subtitle="Średni udział czystej energii (wiatr, słońce, atom, biomasa, woda) w ciągu najbliższych 3 dni"
        action={
          <button
            type="button"
            onClick={() => void refetch()}
            disabled={isFetching}
            className="inline-flex items-center gap-1.5 rounded-xl border border-slate-200 bg-slate-50 px-3 py-1.5 text-xs font-medium text-slate-700 transition hover:bg-slate-100 disabled:opacity-50 dark:border-slate-800 dark:bg-slate-800/70 dark:text-slate-200 dark:hover:bg-slate-800"
          >
            <RefreshCw className={`h-3.5 w-3.5 ${isFetching ? 'animate-spin' : ''}`} />
            Odśwież
          </button>
        }
      />

      {isPending ? (
        <div className="space-y-4">
          <Skeleton className="h-64 w-full" />
          <div className="grid gap-3 md:grid-cols-3">
            <Skeleton className="h-36" />
            <Skeleton className="h-36" />
            <Skeleton className="h-36" />
          </div>
        </div>
      ) : isError ? (
        <ErrorAlert
          title="Brak danych z Carbon Intensity API"
          message={errorMessage}
          onRetry={() => void refetch()}
        />
      ) : data ? (
        <div className="space-y-5">
          <EnergyMixChart days={data.days} cleanestDate={data.cleanestDate} />
          <div className="grid gap-3 md:grid-cols-3">
            {data.days.map((day) => (
              <DayMixSummaryCard
                key={day.date}
                day={day}
                isCleanest={data.cleanestDate === day.date}
              />
            ))}
          </div>
        </div>
      ) : null}
    </Card>
  );
}
