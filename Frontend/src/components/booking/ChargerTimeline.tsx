import { CalendarDays, Clock } from 'lucide-react';
import { differenceInMinutes, parseISO, startOfDay } from 'date-fns';
import type { ChargerBookedSlot } from '../../api/types';
import { formatLocalTimeOnly, groupSlotsByLocalDay } from '../../utils/dateUtils';
import { Skeleton } from '../ui/Feedback';

interface ChargerTimelineProps {
  chargerName?: string;
  slots: ChargerBookedSlot[];
  isLoading: boolean;
}

function slotBarPosition(slot: ChargerBookedSlot): { leftPct: number; widthPct: number } {
  const start = parseISO(slot.startTime);
  const end = parseISO(slot.endTime);
  const dayStart = startOfDay(start);
  const startMin = Math.max(0, differenceInMinutes(start, dayStart));
  const endMin = Math.min(1440, Math.max(startMin + 15, differenceInMinutes(end, dayStart)));
  return {
    leftPct: (startMin / 1440) * 100,
    widthPct: Math.max(4, ((endMin - startMin) / 1440) * 100),
  };
}

export function ChargerTimeline({ chargerName, slots, isLoading }: ChargerTimelineProps) {
  const grouped = groupSlotsByLocalDay(slots);

  return (
    <div className="rounded-xl border border-slate-200/80 bg-slate-50/70 p-4 dark:border-slate-800 dark:bg-slate-950/50">
      <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <CalendarDays className="h-4 w-4 text-emerald-600 dark:text-emerald-400" />
          <h3 className="text-sm font-semibold text-slate-900 dark:text-slate-100">
            Zajęte terminy {chargerName ? `— ${chargerName}` : ''}
          </h3>
        </div>
        <span className="text-[11px] text-slate-500 dark:text-slate-400">
          Oś doby (00:00 – 24:00 czasu lokalnego)
        </span>
      </div>

      {isLoading ? (
        <Skeleton className="h-24 w-full" />
      ) : grouped.length === 0 ? (
        <div className="rounded-lg border border-dashed border-slate-300/80 px-4 py-5 text-center text-xs text-slate-500 dark:border-slate-800 dark:text-slate-400">
          Brak rezerwacji dla wybranej ładowarki — wszystkie godziny są obecnie wolne.
        </div>
      ) : (
        <div className="space-y-3">
          {grouped.map((group) => (
            <div
              key={group.dayKey}
              className="rounded-lg border border-slate-200/70 bg-white p-3 dark:border-slate-800 dark:bg-slate-900/80"
            >
              <p className="mb-2 text-xs font-semibold capitalize text-slate-700 dark:text-slate-200">
                {group.heading}
              </p>

              {/* 24-hour visual occupancy bar */}
              <div className="relative h-3 w-full overflow-hidden rounded-full bg-slate-100 dark:bg-slate-800">
                {group.slots.map((slot) => {
                  const pos = slotBarPosition(slot);
                  return (
                    <div
                      key={`${slot.startTime}-${slot.endTime}`}
                      title={`${formatLocalTimeOnly(slot.startTime)} – ${formatLocalTimeOnly(slot.endTime)}`}
                      style={{ left: `${pos.leftPct}%`, width: `${pos.widthPct}%` }}
                      className="absolute top-0 h-full rounded-full bg-amber-500/90 dark:bg-amber-400/90"
                    />
                  );
                })}
              </div>

              <div className="mt-1 flex justify-between text-[10px] text-slate-400">
                <span>00:00</span>
                <span>06:00</span>
                <span>12:00</span>
                <span>18:00</span>
                <span>24:00</span>
              </div>

              <div className="mt-2.5 flex flex-wrap gap-1.5">
                {group.slots.map((slot) => (
                  <span
                    key={`badge-${slot.startTime}-${slot.endTime}`}
                    className="inline-flex items-center gap-1 rounded-md border border-amber-200 bg-amber-50 px-2 py-0.5 text-[11px] font-medium text-amber-800 dark:border-amber-800/70 dark:bg-amber-950/60 dark:text-amber-300"
                  >
                    <Clock className="h-3 w-3" />
                    {formatLocalTimeOnly(slot.startTime)} – {formatLocalTimeOnly(slot.endTime)}
                  </span>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
