import { BrowserRouter, NavLink, Route, Routes, Navigate, useLocation } from "react-router-dom";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import VehicleListPage from "./app/page/vehicle/VehicleListPage";
import NewVehiclePage from "./app/page/vehicle/NewVehiclePage";
import VehicleDetailPage from "./app/page/vehicle/VehicleDetailPage";
import DistributedVehiclesReportPage from "./app/page/report/DistributedVehiclesReportPage";
import SoldVehiclesReportPage from "./app/page/report/SoldVehiclesReportPage";
import ToastProvider from "./app/component/notification/ToastProvider";
import LoginPage from "./app/page/auth/LoginPage";
import { authTokenEvents, authTokenStorage } from "./app/service/api";
import { setAppLanguage } from "./app/i18n";

const linkClass =
  "px-3 py-2 rounded-md text-sm font-medium text-slate-600 hover:text-slate-900 hover:bg-slate-100";
const activeClass = "bg-slate-200 text-slate-900";

function RequireAuth({ children }: { children: JSX.Element }) {
  const location = useLocation();
  const token = authTokenStorage.get();
  if (!token) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  return children;
}

function AppLayout() {
  const { t, i18n } = useTranslation();
  const [token, setToken] = useState(authTokenStorage.get());

  useEffect(() => {
    const syncToken = () => setToken(authTokenStorage.get());
    window.addEventListener("storage", syncToken);
    window.addEventListener(authTokenEvents.changeEvent, syncToken);
    return () => {
      window.removeEventListener("storage", syncToken);
      window.removeEventListener(authTokenEvents.changeEvent, syncToken);
    };
  }, []);

  return (
    <ToastProvider>
      <div className="min-h-screen">
        <header className="bg-white shadow-sm">
          <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
            <h1 className="text-lg font-semibold">{t("app.title")}</h1>
            <div className="flex flex-wrap items-center gap-2">
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => setAppLanguage("pt-BR")}
                  aria-label={t("language.portuguese")}
                  className={`${linkClass} ${
                    i18n.language === "pt-BR" ? activeClass : ""
                  }`}
                >
                  🇧🇷
                </button>
                <button
                  type="button"
                  onClick={() => setAppLanguage("en-US")}
                  aria-label={t("language.english")}
                  className={`${linkClass} ${
                    i18n.language === "en-US" ? activeClass : ""
                  }`}
                >
                  🇺🇸
                </button>
              </div>
              {token ? (
                <nav className="flex flex-wrap items-center gap-2">
                  <div className="mx-1 h-5 w-px bg-slate-200" />
                  <NavLink
                    to="/vehicles"
                    className={({ isActive }) =>
                      `${linkClass} ${isActive ? activeClass : ""}`
                    }
                  >
                    {t("nav.vehicles")}
                  </NavLink>
                  <NavLink
                    to="/reports/distributed-vehicles"
                    className={({ isActive }) =>
                      `${linkClass} ${isActive ? activeClass : ""}`
                    }
                  >
                    {t("nav.reports")}
                  </NavLink>
                  <NavLink
                    to="/reports/sold-vehicles"
                    className={({ isActive }) =>
                      `${linkClass} ${isActive ? activeClass : ""}`
                    }
                  >
                    {t("nav.sales")}
                  </NavLink>
                  <button
                    type="button"
                    onClick={() => {
                      authTokenStorage.clear();
                      window.location.href = "/login";
                    }}
                    className={linkClass}
                  >
                    {t("nav.logout")}
                  </button>
                </nav>
              ) : null}
            </div>
          </div>
        </header>
        <main className="mx-auto max-w-6xl px-6 py-8">
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route
              path="/"
              element={
                <RequireAuth>
                  <VehicleListPage />
                </RequireAuth>
              }
            />
            <Route
              path="/vehicles"
              element={
                <RequireAuth>
                  <VehicleListPage />
                </RequireAuth>
              }
            />
            <Route
              path="/vehicles/new"
              element={
                <RequireAuth>
                  <NewVehiclePage />
                </RequireAuth>
              }
            />
            <Route
              path="/vehicles/:vehicleId"
              element={
                <RequireAuth>
                  <VehicleDetailPage />
                </RequireAuth>
              }
            />
            <Route
              path="/reports/distributed-vehicles"
              element={
                <RequireAuth>
                  <DistributedVehiclesReportPage />
                </RequireAuth>
              }
            />
            <Route
              path="/reports/sold-vehicles"
              element={
                <RequireAuth>
                  <SoldVehiclesReportPage />
                </RequireAuth>
              }
            />
          </Routes>
        </main>
      </div>
    </ToastProvider>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AppLayout />
    </BrowserRouter>
  );
}
