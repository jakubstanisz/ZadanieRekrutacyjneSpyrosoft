import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ApiError } from '../api/apiClient';
import { cancelBooking, createBooking, fetchMyBookings } from '../api/bookingApi';
import type { CreateBookingPayload } from '../api/types';
import { useToast } from '../context/ToastContext';

function resolveFriendlyBookingError(error: unknown, fallbackTitle: string): { title: string; description: string } {
  if (error instanceof ApiError) {
    if (error.status === 409) {
      return {
        title: 'Wybrany termin jest już zajęty',
        description: 'Ta ładowarka ma już rezerwację w podanym przedziale czasowym. Wybierz inny termin lub inną ładowarkę.',
      };
    }
    return {
      title: error.title || fallbackTitle,
      description: error.detail,
    };
  }
  return {
    title: fallbackTitle,
    description: 'Wystąpił nieoczekiwany błąd.',
  };
}

export function useMyBookings() {
  return useQuery({
    queryKey: ['my-bookings'],
    queryFn: fetchMyBookings,
    staleTime: 30 * 1000,
  });
}

export function useCreateBookingMutation() {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: (payload: CreateBookingPayload) => createBooking(payload),
    onSuccess: (created) => {
      void queryClient.invalidateQueries({ queryKey: ['my-bookings'] });
      void queryClient.invalidateQueries({ queryKey: ['charger-bookings', created.chargerId] });
      showToast({
        variant: 'success',
        title: 'Rezerwacja potwierdzona',
        description: `Zarezerwowano ${created.chargerName} na ${created.durationHours} h.`,
      });
    },
    onError: (error) => {
      const msg = resolveFriendlyBookingError(error, 'Nie udało się utworzyć rezerwacji');
      showToast({
        variant: 'error',
        title: msg.title,
        description: msg.description,
      });
    },
  });
}

export function useCancelBookingMutation() {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: (bookingId: string) => cancelBooking(bookingId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['my-bookings'] });
      void queryClient.invalidateQueries({ queryKey: ['charger-bookings'] });
      showToast({
        variant: 'info',
        title: 'Rezerwacja anulowana',
        description: 'Zwolniony termin jest ponownie dostępny dla kierowców.',
      });
    },
    onError: (error) => {
      const msg = resolveFriendlyBookingError(error, 'Nie udało się anulować rezerwacji');
      showToast({
        variant: 'error',
        title: msg.title,
        description: msg.description,
      });
    },
  });
}
