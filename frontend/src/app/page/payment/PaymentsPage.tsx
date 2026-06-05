import { useState } from "react";
import { useTranslation } from "react-i18next";
import DashboardTab from "./component/DashboardTab";
import WarrantyTab from "./component/WarrantyTab";
import OperationalCostTab from "./component/OperationalCostTab";
import ProLaboreBonusTab from "./component/ProLaboreBonusTab";

type TabKey = "dashboard" | "warranty" | "operational" | "bonus";

export default function PaymentsPage() {
  const { t } = useTranslation();
  const [activeTab, setActiveTab] = useState<TabKey>("dashboard");

  const tabs: { key: TabKey; label: string; description: string }[] = [
    {
      key: "dashboard",
      label: t("payments.tabs.financial"),
      description: t("payments.tabs.financialDesc"),
    },
    {
      key: "warranty",
      label: t("payments.tabs.warranty"),
      description: t("payments.tabs.warrantyDesc"),
    },
    {
      key: "operational",
      label: t("payments.tabs.operational"),
      description: t("payments.tabs.operationalDesc"),
    },
    {
      key: "bonus",
      label: t("payments.tabs.bonus"),
      description: t("payments.tabs.bonusDesc"),
    },
  ];

  const activeTabMeta = tabs.find((tab) => tab.key === activeTab);

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-semibold">{t("payments.title")}</h2>
        <p className="text-sm text-slate-500">{t("payments.subtitle")}</p>
      </div>

      <div className="rounded-xl border border-slate-200 bg-white shadow-sm">
        <div className="border-b border-slate-200">
          <nav className="flex overflow-x-auto" aria-label="Payments tabs">
            {tabs.map((tab) => (
              <button
                key={tab.key}
                type="button"
                onClick={() => setActiveTab(tab.key)}
                className={`flex-shrink-0 border-b-2 px-6 py-4 text-sm font-medium transition-colors focus:outline-none ${
                  activeTab === tab.key
                    ? "border-slate-900 text-slate-900"
                    : "border-transparent text-slate-500 hover:border-slate-300 hover:text-slate-700"
                }`}
              >
                {tab.label}
              </button>
            ))}
          </nav>
        </div>

        <div className="px-6 py-2 border-b border-slate-100 bg-slate-50">
          <p className="text-xs text-slate-500">{activeTabMeta?.description}</p>
        </div>

        <div className="p-6">
          {activeTab === "dashboard" ? <DashboardTab /> : null}
          {activeTab === "warranty" ? <WarrantyTab /> : null}
          {activeTab === "operational" ? <OperationalCostTab /> : null}
          {activeTab === "bonus" ? <ProLaboreBonusTab /> : null}
        </div>
      </div>
    </div>
  );
}
