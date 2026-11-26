import { MapPin, PlugZap } from 'lucide-react';
import type { Charger } from '../../api/types';
import { Badge } from '../ui/Badge';

interface ChargerSelectorProps {
  chargers: Charger[];
  selectedChargerId: string | null;
  onSelectCharger: (chargerId: string) => void;
}

export function ChargerSelector({
  chargers,
  selectedChargerId,
  onSelectCharger,
}: ChargerSelectorProps) {
  return (
    <div className="grid gap-3 sm:grid-cols-2">
      {chargers.map((charger) => {
        const isSelected = charger.id === selectedChargerId;
        return (
          <button
            key={charger.id}
            type="button"
            onClick={() => onSelectCharger(charger.id)}
            className={`group flex flex-col justify-between rounded-xl border p-3.5 text-left transition-all ${
              isSelected
                ? 'border-emerald-500 bg-emerald-50/70 shadow-sm ring-2 ring-emerald-500/20 dark:border-emerald-500 dark:bg-emerald-950/35'
                : 'border-slate-200 bg-slate-50/60 hover:border-slate-300 hover:bg-white dark:border-slate-800 dark:bg-slate-950/40 dark:hover:border-slate-700 dark:hover:bg-slate-900'
            }`}
          >
            <div className="flex w-full items-start justify-between gap-2">
              <div className="flex items-center gap-2">
                <PlugZap
                  className={`h-4 w-4 shrink-0 ${
                    isSelected ? 'text-emerald-600 dark:text-emerald-400' : 'text-slate-400'
                  }`}
                />
                <span className="text-sm font-semibold text-slate-900 dark:text-slate-100">
                  {charger.name}
                </span>
              </div>
              <Badge variant={isSelected ? 'emerald' : 'slate'}>{charger.powerKw} kW</Badge>
            </div>

            <div className="mt-2.5 flex items-center justify-between gap-2 text-xs text-slate-500 dark:text-slate-400">
              <span className="inline-flex min-w-0 items-center gap-1 truncate">
                <MapPin className="h-3.5 w-3.5 shrink-0" />
                <span className="truncate">{charger.location}</span>
              </span>
              <span className="shrink-0 font-medium text-slate-600 dark:text-slate-300">
                {charger.connectorType}
              </span>
            </div>
          </button>
        );
      })}
    </div>
  );
}
