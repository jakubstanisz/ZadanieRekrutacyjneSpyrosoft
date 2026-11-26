import { addHours, differenceInHours, format, isValid, parseISO } from 'date-fns';
import { pl } from 'date-fns/locale';
import type { ChargerBookedSlot } from '../api/types';

export function formatUtcDayLabel(isoDate: string): string {
  const parsed = parseISO(`${isoDate}T00:00:00Z`);
  if (!isValid(parsed)) {
    return isoDate;
  }
  return format(parsed, 'EEE, dd MMM', { locale: pl });
}

export function formatLocalDateTime(isoTimestamp: string): string {
  const parsed = parseISO(isoTimestamp);
  if (!isValid(parsed)) {
    return isoTimestamp;
  }
  return format(parsed, 'dd MMM, HH:mm', { locale: pl });
}

export function formatLocalTimeOnly(isoTimestamp: string): string {
  const parsed = parseISO(isoTimestamp);
  if (!isValid(parsed)) {
    return isoTimestamp;
  }
  return format(parsed, 'HH:mm');
}

export function formatLocalDayHeading(isoTimestamp: string): string {
  const parsed = parseISO(isoTimestamp);
  if (!isValid(parsed)) {
    return isoTimestamp;
  }
  return format(parsed, 'EEEE, dd MMMM yyyy', { locale: pl });
}

export function toLocalInputValue(isoTimestamp: string): string {
  const parsed = parseISO(isoTimestamp);
  if (!isValid(parsed)) {
    return '';
  }
  return format(parsed, "yyyy-MM-dd'T'HH:mm");
}

export function defaultStartInputValue(): string {
  const nextHour = addHours(new Date(), 1);
  nextHour.setMinutes(0, 0, 0);
  return format(nextHour, "yyyy-MM-dd'T'HH:mm");
}

export function computeEndIsoFromLocalInput(localStartValue: string, durationHours: number): string | null {
  const start = new Date(localStartValue);
  if (!isValid(start)) {
    return null;
  }
  const end = addHours(start, durationHours);
  return end.toISOString();
}

export function clampWindowDuration(startIso: string, endIso: string): number {
  const start = parseISO(startIso);
  const end = parseISO(endIso);
  if (!isValid(start) || !isValid(end)) {
    return 2;
  }
  const diff = differenceInHours(end, start);
  return Math.min(6, Math.max(1, diff || 2));
}

export function groupSlotsByLocalDay(
  slots: ChargerBookedSlot[],
): Array<{ dayKey: string; heading: string; slots: ChargerBookedSlot[] }> {
  const groups = new Map<string, { dayKey: string; heading: string; slots: ChargerBookedSlot[] }>();

  for (const slot of slots) {
    const parsed = parseISO(slot.startTime);
    if (!isValid(parsed)) {
      continue;
    }
    const dayKey = format(parsed, 'yyyy-MM-dd');
    const existing = groups.get(dayKey);
    if (existing) {
      existing.slots.push(slot);
    } else {
      groups.set(dayKey, {
        dayKey,
        heading: formatLocalDayHeading(slot.startTime),
        slots: [slot],
      });
    }
  }

  return Array.from(groups.values());
}
