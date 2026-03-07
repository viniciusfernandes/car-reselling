/**
 * Logs current env and resolved API/auth URLs to the console once at app load.
 * Useful to verify production values in the browser DevTools.
 */
function logEnvConfig() {
  const apiBase =
    import.meta.env.VITE_API_BASE_URL != null && import.meta.env.VITE_API_BASE_URL !== ""
      ? `${String(import.meta.env.VITE_API_BASE_URL).replace(/\/$/, "")}/api/v1`
      : "/api/v1";
  const authBase =
    import.meta.env.VITE_AUTH_BASE_URL != null && import.meta.env.VITE_AUTH_BASE_URL !== ""
      ? String(import.meta.env.VITE_AUTH_BASE_URL).replace(/\/$/, "")
      : "/auth";

  console.log("[Car Reselling] Frontend config:", {
    mode: import.meta.env.MODE,
    dev: import.meta.env.DEV,
    prod: import.meta.env.PROD,
    env: {
      VITE_DEV_PORT: import.meta.env.VITE_DEV_PORT ?? "(not set)",
      VITE_API_PROXY_TARGET: import.meta.env.VITE_API_PROXY_TARGET ?? "(not set)",
      VITE_AUTH_PROXY_TARGET: import.meta.env.VITE_AUTH_PROXY_TARGET ?? "(not set)",
      VITE_API_BASE_URL: import.meta.env.VITE_API_BASE_URL ?? "(not set)",
      VITE_AUTH_BASE_URL: import.meta.env.VITE_AUTH_BASE_URL ?? "(not set)",
    },
    resolved: {
      apiBaseUrl: apiBase,
      authBaseUrl: authBase,
      loginUrl: `${authBase}/api/auth/login`,
    },
  });
}

logEnvConfig();
