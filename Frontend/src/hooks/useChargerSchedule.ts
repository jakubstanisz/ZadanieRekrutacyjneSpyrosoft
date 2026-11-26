import { useQuery } from '@tanstack/react-query';
import { fetchChargerBookings } from '../api/bookingApi';

export function useChargerSchedule(chargerId: string | null) {
  return useQuery({
    queryKey: ['charger-bookings', chargerId],
    queryFn: () => fetchChargerBookings(chargerId!),
    enabled: Boolean(chargerId),
    staleTime: 30 * 1000,
  });
}
