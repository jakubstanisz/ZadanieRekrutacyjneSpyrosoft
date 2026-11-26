import { useEffect, useState } from 'react';
import { PlugZap } from 'lucide-react';
import { ApiError } from '../../api/apiClient';
import { useChargers } from '../../hooks/useChargers';
import { useChargerSchedule } from '../../hooks/useChargerSchedule';
import { Card, CardHeader } from '../ui/Card';
import { ErrorAlert, Skeleton } from '../ui/Feedback';
import { ChargerSelector } from './ChargerSelector';
import { ChargerTimeline } from './ChargerTimeline';
import { CreateBookingForm, type PrefilledWindow } from './CreateBookingForm';

interface BookingWorkspaceProps {
  prefilledWindow: PrefilledWindow | null;
}

export function BookingWorkspace({ prefilledWindow }: BookingWorkspaceProps) {
  const { data: chargers, isPending, isError, error, refetch } = useChargers();
  const [selectedChargerId, setSelectedChargerId] = useState<string | null>(null);

  useEffect(() => {
    if (chargers && chargers.length > 0 && !selectedChargerId) {
      setSelectedChargerId(chargers[0].id);
    }
  }, [chargers, selectedChargerId]);

  const selectedCharger = chargers?.find((c) => c.id === selectedChargerId);
  const scheduleQuery = useChargerSchedule(selectedChargerId);

  const errorMessage =
    error instanceof ApiError ? error.detail : 'Nie udało się pobrać listy stacji ładowania.';

  return (
    <Card>
      <CardHeader
        icon={<PlugZap className="h-5 w-5" />}
        title="Rezerwacja ładowarki"
        subtitle="Wybierz stację, sprawdź zajęte sloty w kalendarzu i zarezerwuj termin na 1–6 pełnych godzin"
      />

      {isPending ? (
        <div className="space-y-4">
          <div className="grid gap-3 sm:grid-cols-2">
            <Skeleton className="h-20" />
            <Skeleton className="h-20" />
          </div>
          <Skeleton className="h-32 w-full" />
          <Skeleton className="h-36 w-full" />
        </div>
      ) : isError ? (
        <ErrorAlert
          title="Nie udało się załadować ładowarek"
          message={errorMessage}
          onRetry={() => void refetch()}
        />
      ) : chargers && chargers.length > 0 ? (
        <div className="space-y-5">
          <ChargerSelector
            chargers={chargers}
            selectedChargerId={selectedChargerId}
            onSelectCharger={setSelectedChargerId}
          />

          <ChargerTimeline
            chargerName={selectedCharger?.name}
            slots={scheduleQuery.data ?? []}
            isLoading={scheduleQuery.isPending}
          />

          <CreateBookingForm
            chargers={chargers}
            selectedChargerId={selectedChargerId}
            onSelectCharger={setSelectedChargerId}
            prefilledWindow={prefilledWindow}
          />
        </div>
      ) : (
        <p className="text-sm text-slate-500 dark:text-slate-400">
          Brak skonfigurowanych stacji ładowania w systemie.
        </p>
      )}
    </Card>
  );
}
