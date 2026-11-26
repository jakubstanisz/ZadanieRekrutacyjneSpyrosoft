import { AppHeader } from './components/layout/AppHeader';
import { ToastViewport } from './components/ui/ToastViewport';
import { DashboardPage } from './pages/DashboardPage';

export function App() {
  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 transition-colors dark:bg-slate-950 dark:text-slate-100">
      <AppHeader />
      <DashboardPage />
      <ToastViewport />
    </div>
  );
}

export default App;
