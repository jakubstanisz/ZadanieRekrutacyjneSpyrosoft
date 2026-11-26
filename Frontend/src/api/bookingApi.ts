import { apiRequest } from './apiClient';
import type { Booking, Charger, ChargerBookedSlot, CreateBookingPayload } from './types';

export function fetchChargers(): Promise<Charger[]> {
  return apiRequest<Charger[]>('/api/chargers');
}

export function fetchChargerBookings(chargerId: string): Promise<ChargerBookedSlot[]> {
  return apiRequest<ChargerBookedSlot[]>(`/api/chargers/${chargerId}/bookings`);
}

export function fetchMyBookings(): Promise<Booking[]> {
  return apiRequest<Booking[]>('/api/bookings');
}

export function createBooking(payload: CreateBookingPayload): Promise<Booking> {
  return apiRequest<Booking>('/api/bookings', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function cancelBooking(bookingId: string): Promise<void> {
  return apiRequest<void>(`/api/bookings/${bookingId}`, {
    method: 'DELETE',
  });
}
