export type DataCompletenessStatus = 'COMPLETE' | 'PARTIAL' | 'UNAVAILABLE';

export interface DailyEnergyMix {
  date: string;
  averageFuelShares: Record<string, number>;
  cleanEnergyPercentage: number;
  availableIntervals: number;
  expectedIntervals: number;
  completenessStatus: DataCompletenessStatus;
}

export interface EnergyMixResponse {
  days: DailyEnergyMix[];
  cleanestDate: string | null;
}

export interface ChargingWindowResponse {
  startTime: string;
  endTime: string;
  cleanEnergyPercentage: number;
  partialData: boolean;
}

export interface Charger {
  id: string;
  name: string;
  location: string;
  connectorType: string;
  powerKw: number;
}

export interface ChargerBookedSlot {
  startTime: string;
  endTime: string;
}

export interface Booking {
  id: string;
  chargerId: string;
  chargerName: string;
  chargerLocation: string;
  userId: string;
  startTime: string;
  endTime: string;
  durationHours: number;
  createdAt: string;
}

export interface CreateBookingPayload {
  chargerId: string;
  startTime: string;
  endTime: string;
}

export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  errors?: Record<string, string>;
  timestamp?: string;
}
