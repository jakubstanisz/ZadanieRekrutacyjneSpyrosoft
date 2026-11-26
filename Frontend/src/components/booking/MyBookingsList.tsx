import { CalendarCheck2, Loader2, MapPin, Trash2 } from 'lucide-react';
import { ApiError } from '../../api/apiClient';
import { useCancelBookingMutation, useMyBookings } from '../../hooks/useBookings';
import { formatLocalDateTime } from '../../utils/dateUtils';
import { Badge } from '../ui/Badge';
import { Card, CardHeader } from '../ui/Card';
import { ErrorAlert, Skeleton } from '../ui/Feedback';

export function MyBookingsList() {
  const { data: bookings, isPending, isError, error, refetch } = useMyBookings();
  const cancelMutation = useCancelBookingMutation();

  const errorMessage =
    error instanceof ApiError ? error.detail : 'Nie udało się pobrać listy Twoich rezerwacji.';

  return (
    <Card>
      <CardHeader
        icon={<CalendarCheck2 className="h-5 w-5" />}
        title="Moje rezerwacje"
        subtitle="Lista aktywnych rezerwacji przypisanych do Twojego konta"
        action={bookings ? <Badge variant="slate">{bookings.length} szt.</Badge> : null}
      />

      {isPending ? (
        <div className="space-y-3">
          <Skeleton className="h-20 w-full" />
          <Skeleton className="h-20 w-full" />
        </div>
      ) : isError ? (
        <ErrorAlert
          title="Błąd wczytywania rezerwacji"
          message={errorMessage}
          onRetry={() => void refetch()}
        />
      ) : bookings && bookings.length === 0 ? (
        <div className="rounded-xl border border-dashed border-slate-300 px-4 py-8 text-center text-sm text-slate-500 dark:border-slate-800 dark:text-slate-400">
          Nie masz jeszcze żadnych rezerwacji. Wybierz ładowarkę powyżej lub użyj rekomendowanego okna ładowania.
        </div>
      ) : (
        <ul className="space-y-3">
          {bookings?.map((booking) => {
            const isCancelling =
              cancelMutation.isPending && cancelMutation.variables === booking.id;

            return (
              <li
                key={booking.id}
                className="flex flex-col justify-between gap-3 rounded-xl border border-slate-200/80 bg-slate-50/70 p-4 transition hover:border-slate-300 dark:border-slate-800 dark:bg-slate-950/50 dark:hover:border-slate-700 sm:flex-row sm:items-center"
              >
                <div className="min-w-0 space-y-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="text-sm font-semibold text-slate-900 dark:text-slate-100">
                      {booking.chargerName}
                    </span>
                    <Badge variant="emerald">{booking.durationHours} h</Badge>
                  </div>

                  {booking.chargerLocation ? (
                    <p className="flex items-center gap-1 text-xs text-slate-500 dark:text-slate-400">
                      <MapPin className="h-3.5 w-3.5 shrink-0" />
                      <span className="truncate">{booking.chargerLocation}</span>
                    </p>
                  ) : null}

                  <p className="text-xs font-medium text-slate-700 dark:text-slate-300">
                    {formatLocalDateTime(booking.startTime)} → {formatLocalDateTime(booking.endTime)}
                  </p>
                </div>

                <button
                  type="button"
                  onClick={() => cancelMutation.mutate(booking.id)}
                  disabled={isCancelling}
                  className="inline-flex shrink-0 items-center justify-center gap-1.5 self-start rounded-xl border border-rose-200 bg-white px-3 py-2 text-xs font-semibold text-rose-600 transition hover:bg-rose-50 disabled:opacity-50 dark:border-rose-900/70 dark:bg-slate-900 dark:text-rose-400 dark:hover:bg-rose-950/60 sm:self-center"
                >
                  {isCancelling ? (
                    <Loader2 className="h-3.5 w-3.5 animate-spin" />
                  ) : (
                    <Trash2 className="h-3.5 w-3.5" />
                  )}
                  Anuluj
                </button>
              </li>
            );
          })}
        </ul>
      )}
    </Card>
  );
}
