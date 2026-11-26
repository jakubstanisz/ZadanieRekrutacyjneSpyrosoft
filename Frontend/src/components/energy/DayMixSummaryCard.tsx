import { Award, Leaf, Wind } from 'lucide-react';
import type { DailyEnergyMix } from '../../api/types';
import { formatUtcDayLabel } from '../../utils/dateUtils';
import { Badge } from '../ui/Badge';

interface DayMixSummaryCardProps {
  day: DailyEnergyMix;
  isCleanest: boolean;
}

const CLEAN_FUELS: Array<{ key: string; label: string }> = [
  { key: 'wind', label: 'Wiatr' },
  { key: 'nuclear', label: 'Atom' },
  { key: 'solar', label: 'Słońce' },
  { key: 'biomass', label: 'Biomasa' },
  { key: 'hydro', label: 'Woda' },
];

export function DayMixSummaryCard({ day, isCleanest }: DayMixSummaryCardProps) {
  const isUnavailable = day.completenessStatus === 'UNAVAILABLE';
  const isPartial = day.completenessStatus === 'PARTIAL';

  return (
    <div
      className={`flex flex-col justify-between rounded-xl border p-4 transition-all ${
        isCleanest
          ? 'border-emerald-400 bg-emerald-50/60 shadow-sm ring-1 ring-emerald-400/40 dark:border-emerald-600/80 dark:bg-emerald-950/30 dark:ring-emerald-600/30'
          : 'border-slate-200 bg-slate-50/70 dark:border-slate-800 dark:bg-slate-950/50'
      }`}
    >
      <div>
        <div className="flex flex-wrap items-center justify-between gap-2">
          <span className="text-sm font-semibold capitalize text-slate-900 dark:text-slate-100">
            {formatUtcDayLabel(day.date)}
          </span>
          <div className="flex items-center gap-1.5">
            {isCleanest ? (
              <Badge variant="emerald">
                <Award className="h-3 w-3" />
                Najczystszy dzień
              </Badge>
            ) : null}
            {isPartial ? <Badge variant="amber">Niepełne dane ({day.availableIntervals}/{day.expectedIntervals})</Badge> : null}
            {isUnavailable ? <Badge variant="rose">Brak danych</Badge> : null}
          </div>
        </div>

        <div className="mt-3 flex items-baseline gap-2">
          <span
            className={`text-2xl font-bold tabular-nums ${
              isCleanest ? 'text-emerald-600 dark:text-emerald-400' : 'text-slate-900 dark:text-slate-100'
            }`}
          >
            {isUnavailable ? '—' : `${day.cleanEnergyPercentage.toFixed(1)}%`}
          </span>
          <span className="inline-flex items-center gap-1 text-xs text-slate-500 dark:text-slate-400">
            <Leaf className="h-3.5 w-3.5 text-emerald-500" />
            czystej energii
          </span>
        </div>
      </div>

      {!isUnavailable ? (
        <div className="mt-4 grid grid-cols-3 gap-1.5 border-t border-slate-200/80 pt-3 text-[11px] dark:border-slate-800">
          {CLEAN_FUELS.map(({ key, label }) => (
            <div key={key} className="flex items-center justify-between rounded-md bg-white/70 px-2 py-1 dark:bg-slate-900/70">
              <span className="text-slate-500 dark:text-slate-400">{label}</span>
              <span className="font-semibold tabular-nums text-slate-800 dark:text-slate-200">
                {(day.averageFuelShares[key] ?? 0).toFixed(1)}%
              </span>
            </div>
          ))}
          <div className="flex items-center justify-between rounded-md bg-white/70 px-2 py-1 dark:bg-slate-900/70">
            <span className="inline-flex items-center gap-0.5 text-slate-500 dark:text-slate-400">
              <Wind className="h-3 w-3" />
              Gaz
            </span>
            <span className="font-semibold tabular-nums text-slate-800 dark:text-slate-200">
              {(day.averageFuelShares.gas ?? 0).toFixed(1)}%
            </span>
          </div>
        </div>
      ) : null}
    </div>
  );
}
