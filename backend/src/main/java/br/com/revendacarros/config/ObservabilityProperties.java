package br.com.revendacarros.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "observability")
public class ObservabilityProperties {

    private final Tracing tracing = new Tracing();

    public Tracing getTracing() {
        return tracing;
    }

    public static class Tracing {
        private boolean habilitado;
        private String serviceName = "revenda-carros-api";
        private final Otlp otlp = new Otlp();
        private Duration metricsExportInterval = Duration.ofSeconds(30);

        public boolean isHabilitado() {
            return habilitado;
        }

        public void setHabilitado(boolean habilitado) {
            this.habilitado = habilitado;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public Otlp getOtlp() {
            return otlp;
        }

        public Duration getMetricsExportInterval() {
            return metricsExportInterval;
        }

        public void setMetricsExportInterval(Duration metricsExportInterval) {
            this.metricsExportInterval = metricsExportInterval;
        }
    }

    public static class Otlp {
        private String endpoint = "http://localhost:3000/otlp";
        private String headers = "";

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getHeaders() {
            return headers;
        }

        public void setHeaders(String headers) {
            this.headers = headers;
        }
    }
}
