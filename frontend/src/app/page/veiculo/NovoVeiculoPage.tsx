import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { api, extractErrorMessage, extractFieldErrors } from "../../service/api";
import { ApiResponse, MarcaItem, CorItem } from "../../service/types";
import { useToast } from "../../component/notification/ToastProvider";
import { fetchMarcas, fetchCores } from "../../service/marcaModelos";
import { parseMoney } from "../../service/formatters";
import TextInput from "../../component/input/TextInput";
import VeiculoFormFields, { VeiculoFormValues } from "./component/VeiculoFormFields";

const PLATE_REGEX = /^[A-Z]{3}[0-9]{4}$|^[A-Z]{3}[0-9][A-Z][0-9]{2}$/;

const EMPTY_FORM_VALUES: VeiculoFormValues = {
  ano: new Date().getFullYear().toString(),
  cor: "",
  modelo: "",
  marca: "",
  origemFornecedor: "INTERNET",
  precoCompra: "",
  custoFrete: "0,00",
  comissaoCompra: "0,00",
  valorFipe: "",
};

export default function NovoVeiculoPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { showToast } = useToast();

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [corOptions, setCorOptions] = useState<CorItem[]>([]);
  const [marcaOptions, setMarcaOptions] = useState<MarcaItem[]>([]);

  const [placa, setPlaca] = useState("");
  const [renavam, setRenavam] = useState("");
  const [chassi, setChassi] = useState("");
  const [formValues, setFormValues] = useState<VeiculoFormValues>(EMPTY_FORM_VALUES);

  useEffect(() => {
    const loadLookupData = async () => {
      try {
        const [marcas, cores] = await Promise.all([fetchMarcas(), fetchCores()]);
        setMarcaOptions(marcas);
        setCorOptions(cores);
      } catch (error) {
        showToast(extractErrorMessage(error), "error");
      }
    };
    loadLookupData();
  }, [showToast]);

  const getMoneyError = (value: string, required = false) => {
    if (!value && required) return t("validation.required");
    if (!value) return "";
    const numeric = parseMoney(value);
    if (Number.isNaN(numeric) || numeric < 0) return t("validation.invalidValue");
    return "";
  };

  const handleFormChange = (field: keyof VeiculoFormValues, value: string) => {
    setFormValues((prev) => ({ ...prev, [field]: value }));
  };

  const handleFormBlur = (field: keyof VeiculoFormValues, currentValue: string) => {
    const nextErrors = { ...errors };
    let val = currentValue;

    if (field === "marca" || field === "modelo" || field === "cor") {
      val = currentValue.trim().toUpperCase();
      setFormValues((prev) => ({ ...prev, [field]: val }));
    }

    switch (field) {
      case "ano":
        if (!val) nextErrors.year = t("validation.required");
        else delete nextErrors.year;
        break;
      case "cor":
      case "modelo":
      case "marca":
        if (!val) nextErrors[field] = t("validation.required");
        else delete nextErrors[field];
        break;
      case "precoCompra": {
        const e = getMoneyError(val, true);
        if (e) nextErrors.purchasePrice = e;
        else delete nextErrors.purchasePrice;
        break;
      }
      case "custoFrete": {
        const e = getMoneyError(val);
        if (e) nextErrors.freightCost = e;
        else delete nextErrors.freightCost;
        break;
      }
      case "comissaoCompra": {
        const e = getMoneyError(val);
        if (e) nextErrors.purchaseCommission = e;
        else delete nextErrors.purchaseCommission;
        break;
      }
    }
    setErrors(nextErrors);
  };

  const validate = () => {
    const nextErrors: Record<string, string> = {};
    const plate = placa.trim().toUpperCase();
    if (!plate) nextErrors.licensePlate = t("validation.required");
    else if (!PLATE_REGEX.test(plate)) nextErrors.licensePlate = t("validation.invalidPlate");
    if (!formValues.ano) nextErrors.year = t("validation.required");
    if (!formValues.cor) nextErrors.color = t("validation.required");
    if (!formValues.modelo) nextErrors.model = t("validation.required");
    if (!formValues.marca) nextErrors.brand = t("validation.required");
    const precoCompraError = getMoneyError(formValues.precoCompra, true);
    if (precoCompraError) nextErrors.purchasePrice = precoCompraError;
    const custoFreteError = getMoneyError(formValues.custoFrete);
    if (custoFreteError) nextErrors.freightCost = custoFreteError;
    const comissaoError = getMoneyError(formValues.comissaoCompra);
    if (comissaoError) nextErrors.purchaseCommission = comissaoError;
    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!validate()) return;
    try {
      const normalizedCor = formValues.cor.trim().toUpperCase();
      const normalizedMarca = formValues.marca.trim().toUpperCase();
      const normalizedModelo = formValues.modelo.trim().toUpperCase();
      setFormValues((prev) => ({
        ...prev,
        cor: normalizedCor,
        marca: normalizedMarca,
        modelo: normalizedModelo,
      }));
      setIsSubmitting(true);
      const response = await api.post<ApiResponse<{ veiculoId: string }>>("/veiculos", {
        placa: placa.trim().toUpperCase(),
        renavam: renavam || null,
        chassi: chassi || null,
        ano: Number(formValues.ano),
        cor: normalizedCor,
        modelo: normalizedModelo,
        marca: normalizedMarca,
        origemFornecedor: formValues.origemFornecedor,
        precoCompra: parseMoney(formValues.precoCompra),
        custoFrete: parseMoney(formValues.custoFrete || "0"),
        comissaoCompra: parseMoney(formValues.comissaoCompra),
        valorFipe: formValues.valorFipe ? parseMoney(formValues.valorFipe) : null,
      });
      showToast(t("vehicles.created"), "success");
      navigate(`/veiculos/${response.data.data.veiculoId}`);
    } catch (error) {
      if ((error as any)?.response?.data?.errors) {
        setErrors(extractFieldErrors((error as any).response.data.errors));
      }
      showToast(extractErrorMessage(error), "error");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-semibold">{t("newVehicle.title")}</h2>
        <p className="text-sm text-slate-500">{t("newVehicle.subtitle")}</p>
      </div>
      <form
        className="space-y-4 rounded-lg border border-slate-200 bg-white p-6 shadow-sm"
        onSubmit={handleSubmit}
      >
        <div className="grid gap-4 md:grid-cols-2">
          <TextInput
            label={t("newVehicle.licensePlate")}
            value={placa}
            required
            onChange={(e) => setPlaca(e.target.value)}
            onBlur={() => {
              const normalized = placa.trim().toUpperCase();
              setPlaca(normalized);
              const nextErrors = { ...errors };
              if (!normalized) nextErrors.licensePlate = t("validation.required");
              else if (!PLATE_REGEX.test(normalized)) nextErrors.licensePlate = t("validation.invalidPlate");
              else delete nextErrors.licensePlate;
              setErrors(nextErrors);
            }}
            error={errors.licensePlate}
          />
          <TextInput
            label={t("newVehicle.renavam")}
            value={renavam}
            onChange={(e) => setRenavam(e.target.value)}
            error={errors.renavam}
          />
          <TextInput
            label={t("newVehicle.vin")}
            value={chassi}
            onChange={(e) => setChassi(e.target.value)}
            error={errors.vin}
          />

          <VeiculoFormFields
            values={formValues}
            errors={errors}
            marcaOptions={marcaOptions}
            corOptions={corOptions}
            onChange={handleFormChange}
            onBlur={handleFormBlur}
          />
        </div>

        <div className="flex justify-end gap-3">
          <button
            type="button"
            onClick={() => navigate("/veiculos")}
            disabled={isSubmitting}
            className="rounded-md border border-slate-200 px-4 py-2 text-sm"
          >
            {t("actions.cancel")}
          </button>
          <button
            type="submit"
            disabled={isSubmitting}
            className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white disabled:cursor-not-allowed disabled:bg-slate-300"
          >
            {isSubmitting ? t("actions.saving") : t("newVehicle.save")}
          </button>
        </div>
      </form>
    </div>
  );
}
