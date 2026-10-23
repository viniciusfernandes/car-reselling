import {useState} from "react";
import {useTranslation} from "react-i18next";
import {api, extractErrorMessage} from "../../../service/api";
import {ServicoItem, TipoServico} from "../../../service/types";
import SelectInput from "../../../component/input/SelectInput";
import MoneyInput from "../../../component/input/MoneyInput";
import TextInput from "../../../component/input/TextInput";
import DateInput from "../../../component/input/DateInput";
import {useToast} from "../../../component/notification/ToastProvider";
import {formatDate, formatMoney, formatNumber, normalizeMoney, parseMoney} from "../../../service/formatters";

interface Props {
    veiculoId: string;
    servicos: ServicoItem[];
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

export default function VeiculoServicosTab({
                                               veiculoId,
                                               servicos,
                                               onRefresh,
                                           }: Props) {
    const {t, i18n} = useTranslation();
    const {showToast} = useToast();

    const todayIso = () => new Date().toISOString().slice(0, 10);

    const [isAddingService, setIsAddingService] = useState(false);
    const [isUpdatingService, setIsUpdatingService] = useState(false);
    const [servicoForm, setServicoForm] = useState({
        tipoServico: "MECANICA" as TipoServico,
        valorServico: "",
        descricao: "",
        dataInicio: todayIso(),
        dataFim: "",
    });
    const [servicoErrors, setServicoErrors] = useState<Record<string, string>>({});
    const [editServicoId, setEditServicoId] = useState<string | null>(null);
    const [editServicoForm, setEditServicoForm] = useState({
        tipoServico: "MECANICA" as TipoServico,
        valorServico: "",
        descricao: "",
        dataInicio: "",
        dataFim: "",
    });
    const [editServicoErrors, setEditServicoErrors] = useState<Record<string, string>>({});

    const servicoOptions: Array<{ value: TipoServico; label: string }> = [
        {value: "MECANICA", label: t("serviceTypes.MECANICA")},
        {value: "ELETRICA", label: t("serviceTypes.ELETRICA")},
        {value: "ESTOFAMENTO", label: t("serviceTypes.ESTOFAMENTO")},
        {value: "ESTETICA", label: t("serviceTypes.ESTETICA")},
        {value: "VISTORIA", label: t("serviceTypes.VISTORIA")},
        {value: "DESPACHANTE", label: t("serviceTypes.DESPACHANTE")},
        {value: "MULTAS", label: t("serviceTypes.MULTAS")},
        {value: "PECAS", label: t("serviceTypes.PECAS")},
        {value: "FRETE", label: t("serviceTypes.FRETE")},
        {value: "IMPOSTO_COMPRA", label: t("serviceTypes.IMPOSTO_COMPRA")},
        {value: "COMISSAO_COMPRA", label: t("serviceTypes.COMISSAO_COMPRA")},
        {value: "FUNILARIA_E_PINTURA", label: t("serviceTypes.FUNILARIA_E_PINTURA")},
        {value: "OUTROS", label: t("serviceTypes.OUTROS")},
    ];

    const getMoneyError = (value: string) => {
        if (!value) return t("validation.required");
        const numeric = parseMoney(value);
        if (Number.isNaN(numeric) || numeric < 0) return t("validation.invalidValue");
        return "";
    };

    const validateServicoForm = (form: { valorServico: string; dataInicio: string }) => {
        const errors: Record<string, string> = {};
        const moneyError = getMoneyError(form.valorServico);
        if (moneyError) errors.serviceValue = moneyError;
        if (!form.dataInicio) errors.startDate = t("validation.required");
        return errors;
    };

    const validateValorServico = (value: string) => {
        const error = getMoneyError(value);
        return error ? {valorServico: error} : {};
    };

    const handleAddService = async () => {
        const nextErrors = validateServicoForm(servicoForm);
        if (Object.keys(nextErrors).length > 0) {
            setServicoErrors(nextErrors);
            return;
        }
        try {
            setIsAddingService(true);
            await api.post(`/veiculos/${veiculoId}/servicos`, {
                tipoServico: servicoForm.tipoServico,
                valorServico: parseMoney(servicoForm.valorServico),
                descricao: servicoForm.descricao || null,
                dataInicio: servicoForm.dataInicio,
                dataFim: servicoForm.dataFim || null,
            });
            showToast(t("vehicleDetail.services.added"), "success");
            setServicoForm({
                tipoServico: "MECANICA",
                valorServico: "",
                descricao: "",
                dataInicio: todayIso(),
                dataFim: ""
            });
            setServicoErrors({});
            await onRefresh();
        } catch (error) {
            showToast(extractErrorMessage(error), "error");
        } finally {
            setIsAddingService(false);
        }
    };

    const startEditService = (service: ServicoItem) => {
        setEditServicoId(service.id);
        setEditServicoErrors({});
        setEditServicoForm({
            tipoServico: service.tipoServico,
            valorServico: formatNumber(service.valorServico),
            descricao: service.descricao ?? "",
            dataInicio: service.dataInicio ?? "",
            dataFim: service.dataFim ?? "",
        });
    };

    const handleUpdateService = async () => {
        if (!editServicoId) return;
        const nextErrors = validateServicoForm(editServicoForm);
        if (Object.keys(nextErrors).length > 0) {
            setEditServicoErrors(nextErrors);
            return;
        }
        try {
            setIsUpdatingService(true);
            await api.put(`/veiculos/${veiculoId}/servicos/${editServicoId}`, {
                tipoServico: editServicoForm.tipoServico,
                valorServico: parseMoney(editServicoForm.valorServico),
                descricao: editServicoForm.descricao || null,
                dataInicio: editServicoForm.dataInicio,
                dataFim: editServicoForm.dataFim || null,
            });
            showToast(t("vehicleDetail.services.updated"), "success");
            setEditServicoId(null);
            setEditServicoErrors({});
            await onRefresh();
        } catch (error) {
            showToast(extractErrorMessage(error), "error");
        } finally {
            setIsUpdatingService(false);
        }
    };

    const handleDeleteService = async (servicoId: string) => {
        if (!window.confirm(t("confirm.deleteService"))) return;
        try {
            await api.delete(`/veiculos/${veiculoId}/servicos/${servicoId}`);
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
                        value={servicoForm.tipoServico}
                        options={servicoOptions}
                        required
                        onChange={(e) =>
                            setServicoForm((prev) => ({...prev, tipoServico: e.target.value as TipoServico}))
                        }
                    />
                    <MoneyInput
                        label={t("vehicleDetail.services.value")}
                        value={servicoForm.valorServico}
                        required
                        onValueChange={(value) => setServicoForm((prev) => ({...prev, valorServico: value}))}
                        onBlur={() => setServicoErrors(validateValorServico(servicoForm.valorServico))}
                        error={servicoErrors.serviceValue}
                    />
                    <TextInput
                        label={t("vehicleDetail.services.description")}
                        value={servicoForm.descricao}
                        onChange={(e) => setServicoForm((prev) => ({...prev, descricao: e.target.value}))}
                    />
                    <div className=" grid gap-4 md:grid-cols-4">
                        <div>
                            <DateInput
                                label={t("vehicleDetail.services.startDate")}
                                type="date"
                                value={servicoForm.dataInicio}
                                required
                                onChange={(e) => {
                                    setServicoForm((prev) => ({...prev, dataInicio: e.target.value}));
                                    if (servicoErrors.startDate) setServicoErrors((prev) => ({...prev, dataInicio: ""}));
                                }}
                                error={servicoErrors.startDate}
                            />
                        </div>
                        <div>
                        <DateInput
                            label={t("vehicleDetail.services.endDate")}
                            type="date"
                            value={servicoForm.dataFim}
                            onChange={(e) => setServicoForm((prev) => ({...prev, dataFim: e.target.value}))}
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
                    {servicos.length === 0 ? (
                        <tr>
                            <td colSpan={6} className="px-4 py-6 text-center">
                                {t("vehicleDetail.services.empty")}
                            </td>
                        </tr>
                    ) : (
                        servicos.map((service) => (
                            <tr key={service.id} className="border-t">
                                {editServicoId === service.id ? (
                                    <>
                                        <td className="px-4 py-3">
                                            <select
                                                value={editServicoForm.tipoServico}
                                                onChange={(e) =>
                                                    setEditServicoForm((prev) => ({
                                                        ...prev,
                                                        tipoServico: e.target.value as TipoServico,
                                                    }))
                                                }
                                                className="w-full rounded border border-slate-200 px-2 py-1 text-sm"
                                            >
                                                {servicoOptions.map((option) => (
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
                                                value={editServicoForm.dataInicio}
                                                onChange={(e) =>
                                                    setEditServicoForm((prev) => ({
                                                        ...prev,
                                                        dataInicio: e.target.value,
                                                    }))
                                                }
                                                className="w-full rounded border border-slate-200 px-2 py-1 text-sm"
                                            />
                                            {editServicoErrors.startDate ? (
                                                <span
                                                    className="text-xs text-red-600">{editServicoErrors.startDate}</span>
                                            ) : null}
                                        </td>
                                        <td className="px-4 py-3">
                                            <input
                                                type="date"
                                                lang={i18n.language}
                                                value={editServicoForm.dataFim}
                                                onChange={(e) =>
                                                    setEditServicoForm((prev) => ({
                                                        ...prev,
                                                        dataFim: e.target.value,
                                                    }))
                                                }
                                                className="w-full rounded border border-slate-200 px-2 py-1 text-sm"
                                            />
                                        </td>
                                        <td className="px-4 py-3">
                                            <input
                                                type="text"
                                                inputMode="decimal"
                                                value={editServicoForm.valorServico}
                                                onChange={(e) =>
                                                    setEditServicoForm((prev) => ({
                                                        ...prev,
                                                        valorServico: normalizeMoneyInput(e.target.value),
                                                    }))
                                                }
                                                onBlur={() =>
                                                    setEditServicoForm((prev) => ({
                                                        ...prev,
                                                        valorServico: formatMoneyValue(prev.valorServico),
                                                    }))
                                                }
                                                className="w-full rounded border border-slate-200 px-2 py-1 text-sm"
                                            />
                                            {editServicoErrors.serviceValue ? (
                                                <span className="text-xs text-red-600">
                            {editServicoErrors.serviceValue}
                          </span>
                                            ) : null}
                                        </td>
                                        <td className="px-4 py-3">
                                            <input
                                                value={editServicoForm.descricao}
                                                onChange={(e) =>
                                                    setEditServicoForm((prev) => ({
                                                        ...prev,
                                                        descricao: e.target.value,
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
                                                        setEditServicoId(null);
                                                        setEditServicoErrors({});
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
                                        <td className="px-4 py-3">{t(`serviceTypes.${service.tipoServico}`)}</td>
                                        <td className="px-4 py-3">{formatDate(service.dataInicio)}</td>
                                        <td className="px-4 py-3">{service.dataFim ? formatDate(service.dataFim) : "-"}</td>
                                        <td className="px-4 py-3 text-right">{formatMoney(service.valorServico)}</td>
                                        <td className="px-4 py-3">{service.descricao ?? "-"}</td>
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
