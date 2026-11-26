import { useCallback, useRef, useState } from 'react';
import { BookingWorkspace } from '../components/booking/BookingWorkspace';
import type { PrefilledWindow } from '../components/booking/CreateBookingForm';
import { MyBookingsList } from '../components/booking/MyBookingsList';
import { ChargingWindowOptimizer } from '../components/energy/ChargingWindowOptimizer';
import { EnergyMixSection } from '../components/energy/EnergyMixSection';
import { useToast } from '../context/ToastContext';

export function DashboardPage() {
  const [prefilledWindow, setPrefilledWindow] = useState<PrefilledWindow | null>(null);
  const bookingSectionRef = useRef<HTMLDivElement>(null);
  const { showToast } = useToast();

  const handleApplyWindowToBooking = useCallback(
    (startTime: string, durationHours: number) => {
      setPrefilledWindow({
        startTime,
        durationHours,
        nonce: Date.now(),
      });
      bookingSectionRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
      showToast({
        variant: 'info',
        title: 'Uzupełniono formularz rezerwacji',
        description: `Wybrano rekomendowane okno na ${durationHours} h. Wybierz ładowarkę i potwierdź rezerwację.`,
      });
    },
    [showToast],
  );

  return (
    <main className="mx-auto max-w-7xl space-y-6 px-4 py-6 sm:px-6 lg:px-8">
      <div className="grid gap-6 lg:grid-cols-12">
        <div className="lg:col-span-8">
          <EnergyMixSection />
        </div>
        <div className="lg:col-span-4">
          <ChargingWindowOptimizer onApplyWindowToBooking={handleApplyWindowToBooking} />
        </div>
      </div>

      <div ref={bookingSectionRef} className="grid gap-6 lg:grid-cols-12">
        <div className="lg:col-span-7">
          <BookingWorkspace prefilledWindow={prefilledWindow} />
        </div>
        <div className="lg:col-span-5">
          <MyBookingsList />
        </div>
      </div>
    </main>
  );
}
