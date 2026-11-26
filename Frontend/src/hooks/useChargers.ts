import { useQuery } from '@tanstack/react-query';
import { fetchChargers } from '../api/bookingApi';

export function useChargers() {
  return useQuery({
    queryKey: ['chargers'],
    queryFn: fetchChargers,
    staleTime: 10 * 60 * 1000,
  });
}
