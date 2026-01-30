import { BrowserRouter, NavLink, Route, Routes } from "react-router-dom";
import VehicleListPage from "./app/page/vehicle/VehicleListPage";
import NewVehiclePage from "./app/page/vehicle/NewVehiclePage";
import VehicleDetailPage from "./app/page/vehicle/VehicleDetailPage";
import DistributedVehiclesReportPage from "./app/page/report/DistributedVehiclesReportPage";
import SoldVehiclesReportPage from "./app/page/report/SoldVehiclesReportPage";
import ToastProvider from "./app/component/notification/ToastProvider";

const linkClass =
  "px-3 py-2 rounded-md text-sm font-medium text-slate-600 hover:text-slate-900 hover:bg-slate-100";
const activeClass = "bg-slate-200 text-slate-900";

export default function App() {
  return (
    <BrowserRouter>
      <ToastProvider>
        <div className="min-h-screen">
          <header className="bg-white shadow-sm">
            <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
              <h1 className="text-lg font-semibold">Car Reselling</h1>
              <nav className="flex gap-2">
                <NavLink
                  to="/vehicles"
                  className={({ isActive }) =>
                    `${linkClass} ${isActive ? activeClass : ""}`
                  }
                >
                  Vehicles
                </NavLink>
                <NavLink
                  to="/reports/distributed-vehicles"
                  className={({ isActive }) =>
                    `${linkClass} ${isActive ? activeClass : ""}`
                  }
                >
                  Reports
                </NavLink>
                <NavLink
                  to="/reports/sold-vehicles"
                  className={({ isActive }) =>
                    `${linkClass} ${isActive ? activeClass : ""}`
                  }
                >
                  Sales
                </NavLink>
              </nav>
            </div>
          </header>
          <main className="mx-auto max-w-6xl px-6 py-8">
            <Routes>
              <Route path="/" element={<VehicleListPage />} />
              <Route path="/vehicles" element={<VehicleListPage />} />
              <Route path="/vehicles/new" element={<NewVehiclePage />} />
              <Route path="/vehicles/:vehicleId" element={<VehicleDetailPage />} />
              <Route
                path="/reports/distributed-vehicles"
                element={<DistributedVehiclesReportPage />}
              />
              <Route
                path="/reports/sold-vehicles"
                element={<SoldVehiclesReportPage />}
              />
            </Routes>
          </main>
        </div>
      </ToastProvider>
    </BrowserRouter>
  );
}
