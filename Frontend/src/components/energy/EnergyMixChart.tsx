import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import type { DailyEnergyMix } from '../../api/types';
import { formatUtcDayLabel } from '../../utils/dateUtils';

interface ChartDatum {
  date: string;
  label: string;
  cleanEnergy: number;
  fossilAndOther: number;
  isCleanest: boolean;
  shares: Record<string, number>;
}

const FUEL_LABELS: Record<string, string> = {
  wind: 'Wiatr',
  solar: 'Słońce',
  nuclear: 'Atom',
  hydro: 'Woda',
  biomass: 'Biomasa',
  gas: 'Gaz',
  coal: 'Węgiel',
  imports: 'Import',
  other: 'Inne',
};

interface CustomTooltipProps {
  active?: boolean;
  payload?: Array<{ payload: ChartDatum }>;
}

function EnergyChartTooltip({ active, payload }: CustomTooltipProps) {
  if (!active || !payload || payload.length === 0) {
    return null;
  }
  const item = payload[0].payload;
  const topFuels = Object.entries(item.shares)
    .filter(([, value]) => value > 0)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 6);

  return (
    <div className="min-w-[210px] rounded-xl border border-slate-200 bg-white/95 p-3 text-xs shadow-lg backdrop-blur dark:border-slate-700 dark:bg-slate-900/95">
      <div className="mb-2 flex items-center justify-between gap-2 border-b border-slate-100 pb-1.5 dark:border-slate-800">
        <span className="font-semibold text-slate-900 dark:text-slate-100">{item.label}</span>
        {item.isCleanest ? (
          <span className="rounded-full bg-emerald-100 px-2 py-0.5 text-[10px] font-bold text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300">
            Najczystszy
          </span>
        ) : null}
      </div>
      <div className="mb-2 flex items-center justify-between font-semibold text-emerald-600 dark:text-emerald-400">
        <span>Czysta energia:</span>
        <span>{item.cleanEnergy.toFixed(1)}%</span>
      </div>
      <div className="space-y-1">
        {topFuels.map(([fuel, perc]) => (
          <div key={fuel} className="flex items-center justify-between text-slate-600 dark:text-slate-300">
            <span>{FUEL_LABELS[fuel] ?? fuel}</span>
            <span className="font-medium tabular-nums">{perc.toFixed(1)}%</span>
          </div>
        ))}
      </div>
    </div>
  );
}

interface EnergyMixChartProps {
  days: DailyEnergyMix[];
  cleanestDate: string | null;
}

export function EnergyMixChart({ days, cleanestDate }: EnergyMixChartProps) {
  const data: ChartDatum[] = days.map((day) => {
    const clean = Number(day.cleanEnergyPercentage.toFixed(1));
    const rest = Math.max(0, Number((100 - clean).toFixed(1)));
    return {
      date: day.date,
      label: formatUtcDayLabel(day.date),
      cleanEnergy: clean,
      fossilAndOther: day.completenessStatus === 'UNAVAILABLE' ? 0 : rest,
      isCleanest: day.date === cleanestDate,
      shares: day.averageFuelShares,
    };
  });

  return (
    <div className="h-64 w-full sm:h-72">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data} margin={{ top: 10, right: 12, left: -12, bottom: 4 }} barCategoryGap="24%">
          <CartesianGrid strokeDasharray="3 3" stroke="rgba(148, 163, 184, 0.25)" vertical={false} />
          <XAxis
            dataKey="label"
            tick={{ fill: '#64748b', fontSize: 12 }}
            axisLine={false}
            tickLine={false}
          />
          <YAxis
            domain={[0, 100]}
            unit="%"
            tick={{ fill: '#64748b', fontSize: 12 }}
            axisLine={false}
            tickLine={false}
          />
          <Tooltip content={<EnergyChartTooltip />} cursor={{ fill: 'rgba(16, 185, 129, 0.08)' }} />
          <Bar dataKey="cleanEnergy" name="Czysta energia" stackId="mix" radius={[6, 6, 0, 0]}>
            {data.map((entry) => (
              <Cell
                key={entry.date}
                fill={entry.isCleanest ? '#10b981' : '#34d399'}
                fillOpacity={entry.isCleanest ? 1 : 0.72}
              />
            ))}
          </Bar>
          <Bar
            dataKey="fossilAndOther"
            name="Pozostałe źródła"
            stackId="mix"
            fill="rgba(148, 163, 184, 0.32)"
            radius={[6, 6, 0, 0]}
          />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
