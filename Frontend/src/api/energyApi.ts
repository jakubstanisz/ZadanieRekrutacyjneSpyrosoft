import { apiRequest } from './apiClient';
import type { ChargingWindowResponse, EnergyMixResponse } from './types';

export function fetchEnergyMix(days = 3): Promise<EnergyMixResponse> {
  return apiRequest<EnergyMixResponse>(`/api/energy-mix?days=${days}`);
}

export function fetchBestChargingWindow(hours: number): Promise<ChargingWindowResponse> {
  return apiRequest<ChargingWindowResponse>(`/api/charging-window?hours=${hours}`);
}
