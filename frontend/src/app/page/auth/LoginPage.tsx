import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useLocation, useNavigate } from "react-router-dom";
import axios from "axios";
import TextInput from "../../component/input/TextInput";
import { authTokenStorage, extractErrorMessage } from "../../service/api";
import { useToast } from "../../component/notification/ToastProvider";

type LoginResponse = {
  token: string;
  type?: string;
};

type LoginResponseEnvelope = {
  data?: LoginResponse;
};

const AUTH_BASE_URL = import.meta.env.VITE_AUTH_BASE_URL ?? "/auth";

export default function LoginPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const location = useLocation();
  const { showToast } = useToast();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!email || !password) {
      showToast(t("auth.validation.required"));
      return;
    }
    try {
      setIsSubmitting(true);
      const response = await axios.post<LoginResponseEnvelope>(
        `${AUTH_BASE_URL}/api/auth/login`,
        {
          email,
          password,
        }
      );
      const token = response.data?.data?.token;
      if (!token) {
        showToast(t("auth.validation.missingToken"));
        return;
      }
      authTokenStorage.set(token);
      const redirectTo = (location.state as any)?.from?.pathname || "/vehicles";
      navigate(redirectTo, { replace: true });
    } catch (error) {
      showToast(extractErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-md rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
      <h2 className="text-xl font-semibold">{t("auth.title")}</h2>
      <p className="mt-1 text-sm text-slate-500">
        {t("auth.subtitle")}
      </p>
      <form onSubmit={handleSubmit} className="mt-6 space-y-4">
        <TextInput
          label={t("auth.email")}
          type="email"
          value={email}
          required
          onChange={(event) => setEmail(event.target.value)}
        />
        <TextInput
          label={t("auth.password")}
          type="password"
          value={password}
          required
          onChange={(event) => setPassword(event.target.value)}
        />
        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full rounded-md bg-slate-900 px-4 py-2 text-sm text-white disabled:cursor-not-allowed disabled:bg-slate-300"
        >
          {isSubmitting ? t("auth.signingIn") : t("auth.signIn")}
        </button>
      </form>
    </div>
  );
}
