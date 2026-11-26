import { AlertCircle, CheckCircle2, Info, X } from 'lucide-react';
import { useToast, type ToastVariant } from '../../context/ToastContext';

const STYLES: Record<ToastVariant, string> = {
  success:
    'border-emerald-200 bg-white text-slate-900 dark:border-emerald-800/80 dark:bg-slate-900 dark:text-slate-100',
  error:
    'border-rose-200 bg-white text-slate-900 dark:border-rose-800/80 dark:bg-slate-900 dark:text-slate-100',
  info: 'border-sky-200 bg-white text-slate-900 dark:border-sky-800/80 dark:bg-slate-900 dark:text-slate-100',
};

function ToastIcon({ variant }: { variant: ToastVariant }) {
  if (variant === 'success') {
    return <CheckCircle2 className="h-5 w-5 shrink-0 text-emerald-500" />;
  }
  if (variant === 'error') {
    return <AlertCircle className="h-5 w-5 shrink-0 text-rose-500" />;
  }
  return <Info className="h-5 w-5 shrink-0 text-sky-500" />;
}

export function ToastViewport() {
  const { toasts, dismissToast } = useToast();

  if (toasts.length === 0) {
    return null;
  }

  return (
    <div
      aria-live="polite"
      className="pointer-events-none fixed bottom-4 right-4 z-50 flex w-[calc(100%-2rem)] max-w-sm flex-col gap-2.5 sm:bottom-6 sm:right-6"
    >
      {toasts.map((toast) => (
        <div
          key={toast.id}
          role="status"
          className={`pointer-events-auto flex items-start gap-3 rounded-xl border p-4 shadow-lg ring-1 ring-black/5 dark:ring-white/5 ${STYLES[toast.variant]}`}
        >
          <ToastIcon variant={toast.variant} />
          <div className="min-w-0 flex-1">
            <p className="text-sm font-semibold">{toast.title}</p>
            {toast.description ? (
              <p className="mt-0.5 text-xs leading-relaxed text-slate-600 dark:text-slate-400">
                {toast.description}
              </p>
            ) : null}
          </div>
          <button
            type="button"
            onClick={() => dismissToast(toast.id)}
            aria-label="Zamknij powiadomienie"
            className="rounded-lg p-1 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600 dark:hover:bg-slate-800 dark:hover:text-slate-200"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
      ))}
    </div>
  );
}
