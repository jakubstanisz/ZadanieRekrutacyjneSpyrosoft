import { useQuery } from '@tanstack/react-query';
import { fetchEnergyMix } from '../api/energyApi';

export function useEnergyMix(days = 3) {
  return useQuery({
    queryKey: ['energy-mix', days],
    queryFn: () => fetchEnergyMix(days),
    staleTime: 5 * 60 * 1000,
    retry: 1,
  });
}
