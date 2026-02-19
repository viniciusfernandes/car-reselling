# Log & Observability

This document describes the log format, dependencies, and observability strategy.

## Log format (Logback)

Standard log pattern (console):

```
%d{yyyy-MM-dd'T'HH:mm:ss.SSSX} %-5level [%thread] %logger{36} traceId=%X{traceId:-} spanId=%X{spanId:-} - %msg%n
```

Fields:
- `traceId` and `spanId` come from MDC. When distributed tracing is enabled, they are from OpenTelemetry. When disabled, `traceId` comes from `X-Trace-Id` or a generated UUID.

Logback config is in `backend/src/main/resources/logback-spring.xml`.

## Dependencies

Backend observability stack uses:
- OpenTelemetry SDK (traces, metrics, logs)
- OTLP exporters

Dependencies are declared in `backend/build.gradle`.

## Distributed traceability (optional)

Feature toggle:

```
observability:
  tracing:
    enabled: false
```

When disabled:
- Logs still include `traceId` (from `X-Trace-Id` or generated UUID).
- No OTLP export is attempted.

When enabled:
- A server span is created for each HTTP request.
- Trace context is extracted from incoming headers (W3C Trace Context).
- MDC is populated with `traceId` and `spanId`.
- Logs, metrics, and traces are exported via OTLP.

## OTLP exporter settings

```
observability:
  tracing:
    service-name: car-reselling-api
    otlp:
      endpoint: http://localhost:3000/otlp
      headers: ""
    metrics-export-interval: 30s
```

Overrides:
- `OBSERVABILITY_TRACING_ENABLED`
- `OBSERVABILITY_SERVICE_NAME`
- `OBSERVABILITY_OTLP_ENDPOINT`
- `OBSERVABILITY_OTLP_HEADERS` (comma-separated `key=value` pairs)
- `OBSERVABILITY_METRICS_INTERVAL` (e.g., `10s`)

## Grafana local setup

Grafana runs at `http://localhost:3000`.

Important:
- Grafana itself does not ingest OTLP data. It visualizes data from datasources such as **Loki** (logs), **Tempo** (traces), and **Prometheus** (metrics).
- To receive OTLP data locally, use an OTLP receiver such as **Grafana Alloy** or **Grafana Agent**, and configure Grafana datasources to read from Loki/Tempo/Prometheus.

Once a receiver is running, point `observability.tracing.otlp.endpoint` to the receiver’s OTLP gRPC endpoint (commonly `http://localhost:4317`), or keep the default if you proxy OTLP through port 3000.

## Strategy

- Use OpenTelemetry for distributed tracing and metrics.
- Use MDC for trace correlation in logs.
- Keep tracing optional to support single-instance deployments.

## Log export to Loki

Logs are shipped directly to Loki using **loki4j** (`com.github.loki4j:loki-logback-appender`), a stable Logback appender with no alpha dependencies.

Architecture:
- **Traces** → OTel SDK → OTLP Collector (port 4317) → Tempo
- **Metrics** → OTel SDK → OTLP Collector (port 4317) → Prometheus
- **Logs** → loki4j Logback appender → Loki (port 3100) directly

The loki4j endpoint is configured via the `LOKI_URL` environment variable (defaults to `http://localhost:3100`).

Each log line in Loki carries the full formatted message including `traceId` and `spanId` from MDC, which are populated by the `CorrelationIdFilter` for every HTTP request.

### Local stack startup order

```bash
docker compose -f docker-compose-observality.yml up -d loki tempo prometheus otel-collector grafana
```

Then start the backend:

```bash
export OBSERVABILITY_TRACING_ENABLED=true
./gradlew bootRun
```

### Query logs in Grafana

1. Open `http://localhost:3000` → **Explore** → select **Loki** datasource
2. Query: `{app="car-reselling-api"}`
3. To filter by log level: `{app="car-reselling-api", level="ERROR"}`
4. Full-text search: `{app="car-reselling-api"} |= "traceId="`
