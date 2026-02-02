import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import enUS from "./locales/en-US.json";
import ptBR from "./locales/pt-BR.json";

const storedLanguage = localStorage.getItem("app-language");
const defaultLanguage = storedLanguage || "pt-BR";

i18n.use(initReactI18next).init({
  resources: {
    "en-US": { translation: enUS },
    "pt-BR": { translation: ptBR },
  },
  lng: defaultLanguage,
  fallbackLng: "pt-BR",
  interpolation: {
    escapeValue: false,
  },
  returnNull: false,
});

export const setAppLanguage = (language: "pt-BR" | "en-US") => {
  localStorage.setItem("app-language", language);
  i18n.changeLanguage(language);
};

export default i18n;
