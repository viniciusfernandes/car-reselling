import {useState} from "react";
import {useTranslation} from "react-i18next";
import {api, extractErrorMessage} from "../../../service/api";
import {ServiceItem, ServiceType} from "../../../service/types";
import SelectInput from "../../../component/input/SelectInput";
import MoneyInput from "../../../component/input/MoneyInput";
import TextInput from "../../../component/input/TextInput";
import DateInput from "../../../component/input/DateInput";
import {useToast} from "../../../component/notification/ToastProvider";
import {formatDate, formatMoney, formatNumber, normalizeMoney, parseMoney} from "../../../service/formatters";

interface Props {
    vehicleId: string;
    services: ServiceItem[];
    onRefresh: () => Promise<void>;
}

const normalizeMoneyInput = (value: string) => {
    const sanitized = normalizeMoney(value);
    const [integerPart, decimalPart = ""] = sanitized.split(".");
    const normalizedDecimal = decimalPart.slice(0, 2);
    return normalizedDecimal.length > 0 ? `${integerPart}.${normalizedDecimal}` : integerPart;
};

const formatMoneyValue = (value: string) => {
    if (!value) return "";
    const numeric = Number(normalizeMoney(value));
    if (Number.isNaN(numeric)) return value;
    return formatNumber(numeric);
};

export default function VehicleServicesTab({
                                               vehicleId,
                                               services,
                                               onRefresh,
                                           }: Props) {
    const {t, i18n} = useTranslation();
    const {showToast} = useToast();

    const todayIso = () => new Date().toISOString().slice(0, 10);

    const [isAddingService, setIsAddingService] = useState(false);
    const [isUpdatingService, setIsUpdatingService] = useState(false);
    const [serviceForm, setServiceForm] = useState({
        serviceType: "MECHANICAL" as ServiceType,
        serviceValue: "",
        description: "",
        startDate: todayIso(),
        endDate: "",
    });
    const [serviceErrors, setServiceErrors] = useState<Record<string, string>>({});
    const [editServiceId, setEditServiceId] = useState<string | null>(null);
    const [editServiceForm, setEditServiceForm] = useState({
        serviceType: "MECHANICAL" as ServiceType,
        serviceValue: "",
        description: "",
        startDate: "",
        endDate: "",
    });
    const [editServiceErrors, setEditServiceErrors] = useState<Record<string, string>>({});

    const serviceOptions: Array<{ value: ServiceType; label: string }> = [
        {value: "MECHANICAL", label: t("serviceTypes.MECHANICAL")},
        {value: "ELECTRICAL", label: t("serviceTypes.ELECTRICAL")},
        {value: "UPHOLSTERY", label: t("serviceTypes.UPHOLSTERY")},
        {value: "AESTHETICS", label: t("serviceTypes.AESTHETICS")},
        {value: "INSPECTION", label: t("serviceTypes.INSPECTION")},
        {value: "DISPATCHER", label: t("serviceTypes.DISPATCHER")},
        {value: "FINES", label: t("serviceTypes.FINES")},
        {value: "PARTS", label: t("serviceTypes.PARTS")},
        {value: "FREIGHT", label: t("serviceTypes.FREIGHT")},
        {value: "PURCHASE_TAX", label: t("serviceTypes.PURCHASE_TAX")},
        {value: "PURCHASE_COMMISSION", label: t("serviceTypes.PURCHASE_COMMISSION")},
        {value: "BODYWORK_AND_PAINT", label: t("serviceTypes.BODYWORK_AND_PAINT")},
        {value: "OTHERS", label: t("serviceTypes.OTHERS")},
    ];

    const getMoneyError = (value: string) => {
        if (!value) return t("validation.required");
        const numeric = parseMoney(value);
        if (Number.isNaN(numeric) || numeric < 0) return t("validation.invalidValue");
        return "";
    };

    const validateServiceForm = (form: { serviceValue: string; startDate: string }) => {
        const errors: Record<string, string> = {};
        const moneyError = getMoneyError(form.serviceValue);
        if (moneyError) errors.serviceValue = moneyError;
        if (!form.startDate) errors.startDate = t("validation.required");
        return errors;
    };

    const validateServiceValue = (value: string) => {
        const error = getMoneyError(value);
        return error ? {serviceValue: error} : {};
    };

    const handleAddService = async () => {
        const nextErrors = validateServiceForm(serviceForm);
        if (Object.keys(nextErrors).length > 0) {
            setServiceErrors(nextErrors);
            return;
        }
        try {
            setIsAddingService(true);
            await api.post(`/vehicles/${vehicleId}/services`, {
                serviceType: serviceForm.serviceType,
                serviceValue: parseMoney(serviceForm.serviceValue),
                description: serviceForm.description || null,
                startDate: serviceForm.startDate,
                endDate: serviceForm.endDate || null,
            });
            showToast(t("vehicleDetail.services.added"), "success");
            setServiceForm({
                serviceType: "MECHANICAL",
                serviceValue: "",
                description: "",
                startDate: todayIso(),
                endDate: ""
            });
            setServiceErrors({});
            await onRefresh();
        } catch (error) {
            showToast(extractErrorMessage(error), "error");
        } finally {
            setIsAddingService(false);
        }
    };

    const startEditService = (service: ServiceItem) => {
        setEditServiceId(service.id);
        setEditServiceErrors({});
        setEditServiceForm({
            serviceType: service.serviceType,
            serviceValue: formatNumber(service.serviceValue),
            description: service.description ?? "",
            startDate: service.startDate ?? "",
            endDate: service.endDate ?? "",
        });
    };

    const handleUpdateService = async () => {
        if (!editServiceId) return;
        const nextErrors = validateServiceForm(editServiceForm);
        if (Object.keys(nextErrors).length > 0) {
            setEditServiceErrors(nextErrors);
            return;
        }
        try {
            setIsUpdatingService(true);
            await api.put(`/vehicles/${vehicleId}/services/${editServiceId}`, {
                serviceType: editServiceForm.serviceType,
                serviceValue: parseMoney(editServiceForm.serviceValue),
                description: editServiceForm.description || null,
                startDate: editServiceForm.startDate,
                endDate: editServiceForm.endDate || null,
            });
            showToast(t("vehicleDetail.services.updated"), "success");
            setEditServiceId(null);
            setEditServiceErrors({});
            await onRefresh();
        } catch (error) {
            showToast(extractErrorMessage(error), "error");
        } finally {
            setIsUpdatingService(false);
        }
    };

    const handleDeleteService = async (serviceId: string) => {
        if (!window.confirm(t("confirm.deleteService"))) return;
        try {
            await api.delete(`/vehicles/${vehicleId}/services/${serviceId}`);
            showToast(t("vehicleDetail.services.deleted"), "success");
            await onRefresh();
        } catch (error) {
            showToast(extractErrorMessage(error), "error");
        }
    };

    return (
        <div className="space-y-6">
            <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
                <h3 className="text-sm font-semibold text-slate-700">
                    {t("vehicleDetail.services.addTitle")}
                </h3>
                <div className="mt-4 grid gap-4 md:grid-cols-2">
                    <SelectInput
                        label={t("vehicleDetail.services.type")}
                        value={serviceForm.serviceType}
                        options={serviceOptions}
                        required
                        onChange={(e) =>
                            setServiceForm((prev) => ({...prev, serviceType: e.target.value as ServiceType}))
                        }
                    />
                    <MoneyInput
                        label={t("vehicleDetail.services.value")}
                        value={serviceForm.serviceValue}
                        required
                        onValueChange={(value) => setServiceForm((prev) => ({...prev, serviceValue: value}))}
                        onBlur={() => setServiceErrors(validateServiceValue(serviceForm.serviceValue))}
                        error={serviceErrors.serviceValue}
                    />
                    <TextInput
                        label={t("vehicleDetail.services.description")}
                        value={serviceForm.description}
                        onChange={(e) => setServiceForm((prev) => ({...prev, description: e.target.value}))}
                    />
                    <div className=" grid gap-4 md:grid-cols-4">
                        <div>
                            <DateInput
                                label={t("vehicleDetail.services.startDate")}
                                type="date"
                                value={serviceForm.startDate}
                                required
                                onChange={(e) => {
                                    setServiceForm((prev) => ({...prev, startDate: e.target.value}));
                                    if (serviceErrors.startDate) setServiceErrors((prev) => ({...prev, startDate: ""}));
                                }}
                                error={serviceErrors.startDate}
                            />
                        </div>
                        <div>
                        <DateInput
                            label={t("vehicleDetail.services.endDate")}
                            type="date"
                            value={serviceForm.endDate}
                            onChange={(e) => setServiceForm((prev) => ({...prev, endDate: e.target.value}))}
                        />
                        </div>
                        <div></div>
                        <div></div>
                    </div>

                </div>
                <div className="mt-4 flex justify-end">
                    <button
                        type="button"
                        onClick={handleAddService}
                        className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white disabled:cursor-not-allowed disabled:bg-slate-300"
                    >
                        {isAddingService ? t("actions.adding") : t("vehicleDetail.services.addAction")}
                    </button>
                </div>

            </div>

            <div className="rounded-lg border border-slate-200 bg-white shadow-sm">
                <table className="min-w-full text-sm">
                    <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
                    <tr>
                        <th className="px-4 py-3">{t("vehicleDetail.services.table.type")}</th>
                        <th className="px-4 py-3">{t("vehicleDetail.services.table.startDate")}</th>
                        <th className="px-4 py-3">{t("vehicleDetail.services.table.endDate")}</th>
                        <th className="px-4 py-3">{t("vehicleDetail.services.table.value")}</th>
                        <th className="px-4 py-3">{t("vehicleDetail.services.table.description")}</th>
                        <th className="px-4 py-3">{t("vehicleDetail.services.table.actions")}</th>
                    </tr>
                    </thead>
                    <tbody>
                    {services.length === 0 ? (
                        <tr>
                            <td colSpan={6} className="px-4 py-6 text-center">
                                {t("vehicleDetail.services.empty")}
                            </td>
                        </tr>
                    ) : (
                        services.map((service) => (
                            <tr key={service.id} className="border-t">
                                {editServiceId === service.id ? (
                                    <>
                                        <td className="px-4 py-3">
                                            <select
                                                value={editServiceForm.serviceType}
                                                onChange={(e) =>
                                                    setEditServiceForm((prev) => ({
                                                        ...prev,
                                                        serviceType: e.target.value as ServiceType,
                                                    }))
                                                }
                                                className="w-full rounded border border-slate-200 px-2 py-1 text-sm"
                                            >
                                                {serviceOptions.map((option) => (
                                                    <option key={option.value} value={option.value}>
                                                        {option.label}
                                                    </option>
                                                ))}
                                            </select>
                                        </td>
                                        <td className="px-4 py-3">
                                            <input
                                                type="date"
                                                lang={i18n.language}
                                                value={editServiceForm.startDate}
                                                onChange={(e) =>
                                                    setEditServiceForm((prev) => ({
                                                        ...prev,
                                                        startDate: e.target.value,
                                                    }))
                                                }
                                                className="w-full rounded border border-slate-200 px-2 py-1 text-sm"
                                            />
                                            {editServiceErrors.startDate ? (
                                                <span
                                                    className="text-xs text-red-600">{editServiceErrors.startDate}</span>
                                            ) : null}
                                        </td>
                                        <td className="px-4 py-3">
                                            <input
                                                type="date"
                                                lang={i18n.language}
                                                value={editServiceForm.endDate}
                                                onChange={(e) =>
                                                    setEditServiceForm((prev) => ({
                                                        ...prev,
                                                        endDate: e.target.value,
                                                    }))
                                                }
                                                className="w-full rounded border border-slate-200 px-2 py-1 text-sm"
                                            />
                                        </td>
                                        <td className="px-4 py-3">
                                            <input
                                                type="text"
                                                inputMode="decimal"
                                                value={editServiceForm.serviceValue}
                                                onChange={(e) =>
                                                    setEditServiceForm((prev) => ({
                                                        ...prev,
                                                        serviceValue: normalizeMoneyInput(e.target.value),
                                                    }))
                                                }
                                                onBlur={() =>
                                                    setEditServiceForm((prev) => ({
                                                        ...prev,
                                                        serviceValue: formatMoneyValue(prev.serviceValue),
                                                    }))
                                                }
                                                className="w-full rounded border border-slate-200 px-2 py-1 text-sm"
                                            />
                                            {editServiceErrors.serviceValue ? (
                                                <span className="text-xs text-red-600">
                            {editServiceErrors.serviceValue}
                          </span>
                                            ) : null}
                                        </td>
                                        <td className="px-4 py-3">
                                            <input
                                                value={editServiceForm.description}
                                                onChange={(e) =>
                                                    setEditServiceForm((prev) => ({
                                                        ...prev,
                                                        description: e.target.value,
                                                    }))
                                                }
                                                className="w-full rounded border border-slate-200 px-2 py-1 text-sm"
                                            />
                                        </td>
                                        <td className="px-4 py-3">
                                            <div className="flex gap-2">
                                                <button
                                                    type="button"
                                                    onClick={handleUpdateService}
                                                    disabled={isUpdatingService}
                                                    className="text-xs font-medium text-slate-900 disabled:text-slate-400"
                                                >
                                                    {isUpdatingService ? t("actions.saving") : t("actions.save")}
                                                </button>
                                                <button
                                                    type="button"
                                                    onClick={() => {
                                                        setEditServiceId(null);
                                                        setEditServiceErrors({});
                                                    }}
                                                    className="text-xs text-slate-500"
                                                >
                                                    {t("actions.cancel")}
                                                </button>
                                            </div>
                                        </td>
                                    </>
                                ) : (
                                    <>
                                        <td className="px-4 py-3">{t(`serviceTypes.${service.serviceType}`)}</td>
                                        <td className="px-4 py-3">{formatDate(service.startDate)}</td>
                                        <td className="px-4 py-3">{service.endDate ? formatDate(service.endDate) : "-"}</td>
                                        <td className="px-4 py-3 text-right">{formatMoney(service.serviceValue)}</td>
                                        <td className="px-4 py-3">{service.description ?? "-"}</td>
                                        <td className="px-4 py-3">
                                            <div className="flex gap-2 text-xs">
                                                <button
                                                    type="button"
                                                    onClick={() => startEditService(service)}
                                                    className="text-slate-900 disabled:text-slate-400"
                                                >
                                                    {t("actions.edit")}
                                                </button>
                                                <button
                                                    type="button"
                                                    onClick={() => handleDeleteService(service.id)}
                                                    className="text-red-600 disabled:text-slate-400"
                                                >
                                                    {t("actions.delete")}
                                                </button>
                                            </div>
                                        </td>
                                    </>
                                )}
                            </tr>
                        ))
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
