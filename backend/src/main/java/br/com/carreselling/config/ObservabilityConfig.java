package br.com.carreselling.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ObservabilityProperties.class)
public class ObservabilityConfig {

    @Bean
    public OpenTelemetry openTelemetry(ObservabilityProperties properties) {
        if (!properties.getTracing().isEnabled()) {
            return OpenTelemetry.noop();
        }
        Resource resource = Resource.getDefault()
            .merge(Resource.create(io.opentelemetry.api.common.Attributes.of(
                AttributeKey.stringKey("service.name"),
                properties.getTracing().getServiceName()
            )));

        Map<String, String> headers = HeaderParser.parse(properties.getTracing().getOtlp().getHeaders());

        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
            .setEndpoint(properties.getTracing().getOtlp().getEndpoint())
            .build();

        OtlpGrpcMetricExporter metricExporter = OtlpGrpcMetricExporter.builder()
            .setEndpoint(properties.getTracing().getOtlp().getEndpoint())
            .build();

        OtlpGrpcLogRecordExporter logExporter = OtlpGrpcLogRecordExporter.builder()
            .setEndpoint(properties.getTracing().getOtlp().getEndpoint())
            .build();

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .setResource(resource)
            .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
            .build();

        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
            .setResource(resource)
            .registerMetricReader(PeriodicMetricReader.builder(metricExporter)
                .setInterval(properties.getTracing().getMetricsExportInterval())
                .build())
            .build();

        SdkLoggerProvider loggerProvider = SdkLoggerProvider.builder()
            .setResource(resource)
            .addLogRecordProcessor(BatchLogRecordProcessor.builder(logExporter).build())
            .build();

        OpenTelemetrySdk sdk = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setMeterProvider(meterProvider)
            .setLoggerProvider(loggerProvider)
            .setPropagators(ContextPropagators.create(TextMapPropagator.composite(
                W3CTraceContextPropagator.getInstance()
            )))
            .buildAndRegisterGlobal();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            tracerProvider.shutdown().join(10, TimeUnit.SECONDS);
            meterProvider.shutdown().join(10, TimeUnit.SECONDS);
            loggerProvider.shutdown().join(10, TimeUnit.SECONDS);
        }));

        return sdk;
    }

    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry, ObservabilityProperties properties) {
        return openTelemetry.getTracer(properties.getTracing().getServiceName());
    }

    private static final class HeaderParser {
        private HeaderParser() {
        }

        static Map<String, String> parse(String rawHeaders) {
            if (rawHeaders == null || rawHeaders.isBlank()) {
                return Map.of();
            }
            String[] pairs = rawHeaders.split(",");
            java.util.Map<String, String> headers = new java.util.HashMap<>();
            for (String pair : pairs) {
                String[] parts = pair.trim().split("=", 2);
                if (parts.length == 2 && !parts[0].isBlank()) {
                    headers.put(parts[0].trim(), parts[1].trim());
                }
            }
            return headers;
        }
    }
}
