import { Moon, Sun, User, Zap } from 'lucide-react';
import { DEMO_USER_ID } from '../../api/apiClient';
import { useTheme } from '../../hooks/useTheme';

export function AppHeader() {
  const { theme, toggleTheme } = useTheme();

  return (
    <header className="sticky top-0 z-30 border-b border-slate-200/80 bg-white/85 backdrop-blur-md dark:border-slate-800 dark:bg-slate-950/85">
      <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-4 py-3.5 sm:px-6 lg:px-8">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-emerald-500 to-teal-600 text-white shadow-sm shadow-emerald-500/20">
            <Zap className="h-5 w-5" />
          </div>
          <div>
            <h1 className="text-base font-bold tracking-tight text-slate-900 dark:text-white sm:text-lg">
              EV Carbon Charge Optimizer
            </h1>
            <p className="hidden text-xs text-slate-500 dark:text-slate-400 sm:block">
              Miks energetyczny UK · Optymalne okno ładowania · Rezerwacje stacji
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2.5">
          <div
            title="Aktywny identyfikator użytkownika wysyłany w nagłówku X-User-Id"
            className="hidden items-center gap-1.5 rounded-xl border border-slate-200 bg-slate-50 px-3 py-1.5 text-xs font-medium text-slate-700 dark:border-slate-800 dark:bg-slate-900 dark:text-slate-300 sm:inline-flex"
          >
            <User className="h-3.5 w-3.5 text-emerald-500" />
            <span>{DEMO_USER_ID}</span>
          </div>

          <button
            type="button"
            onClick={toggleTheme}
            aria-label={theme === 'dark' ? 'Przełącz na tryb jasny' : 'Przełącz na tryb ciemny'}
            className="inline-flex h-9 items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 text-xs font-semibold text-slate-700 shadow-sm transition hover:bg-slate-100 dark:border-slate-800 dark:bg-slate-900 dark:text-slate-200 dark:hover:bg-slate-800"
          >
            {theme === 'dark' ? (
              <>
                <Sun className="h-4 w-4 text-amber-400" />
                <span className="hidden sm:inline">Jasny</span>
              </>
            ) : (
              <>
                <Moon className="h-4 w-4 text-slate-600" />
                <span className="hidden sm:inline">Ciemny</span>
              </>
            )}
          </button>
        </div>
      </div>
    </header>
  );
}
