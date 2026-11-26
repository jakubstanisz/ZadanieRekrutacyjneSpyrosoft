import { useQuery } from '@tanstack/react-query';
import { fetchBestChargingWindow } from '../api/energyApi';

export function useChargingWindow(hours: number) {
  return useQuery({
    queryKey: ['charging-window', hours],
    queryFn: () => fetchBestChargingWindow(hours),
    staleTime: 5 * 60 * 1000,
    retry: 1,
  });
}
