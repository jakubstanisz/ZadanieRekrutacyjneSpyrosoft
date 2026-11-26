import { AlertTriangle, RefreshCw } from 'lucide-react';

export function Skeleton({ className = '' }: { className?: string }) {
  return (
    <div
      className={`animate-pulse rounded-xl bg-slate-200/80 dark:bg-slate-800/80 ${className}`}
      aria-hidden="true"
    />
  );
}

interface ErrorAlertProps {
  title: string;
  message: string;
  onRetry?: () => void;
}

export function ErrorAlert({ title, message, onRetry }: ErrorAlertProps) {
  return (
    <div
      role="alert"
      className="flex flex-col gap-3 rounded-xl border border-rose-200 bg-rose-50/90 p-4 text-rose-900 dark:border-rose-900/70 dark:bg-rose-950/50 dark:text-rose-200 sm:flex-row sm:items-center sm:justify-between"
    >
      <div className="flex items-start gap-3">
        <AlertTriangle className="mt-0.5 h-5 w-5 shrink-0 text-rose-600 dark:text-rose-400" />
        <div>
          <p className="text-sm font-semibold">{title}</p>
          <p className="mt-0.5 text-sm opacity-90">{message}</p>
        </div>
      </div>
      {onRetry ? (
        <button
          type="button"
          onClick={onRetry}
          className="inline-flex shrink-0 items-center gap-1.5 self-start rounded-lg border border-rose-300 bg-white px-3 py-1.5 text-xs font-semibold text-rose-700 transition hover:bg-rose-100 dark:border-rose-800 dark:bg-rose-950 dark:text-rose-200 dark:hover:bg-rose-900/60 sm:self-center"
        >
          <RefreshCw className="h-3.5 w-3.5" />
          Spróbuj ponownie
        </button>
      ) : null}
    </div>
  );
}
