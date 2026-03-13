package br.com.carreselling.config;

import br.com.carreselling.common.UuidGenerator;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String SPAN_ID_HEADER = "X-Span-Id";

    private final ObservabilityProperties observabilityProperties;
    private final OpenTelemetry openTelemetry;
    private final Tracer tracer;

    public CorrelationIdFilter(ObservabilityProperties observabilityProperties,
                               OpenTelemetry openTelemetry,
                               Tracer tracer) {
        this.observabilityProperties = observabilityProperties;
        this.openTelemetry = openTelemetry;
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (observabilityProperties.getTracing().isEnabled()) {
            handleOpenTelemetryFlow(request, response, filterChain);
            return;
        }
        String traceId = Optional.ofNullable(request.getHeader(TRACE_ID_HEADER))
            .filter(value -> !value.isBlank())
            .orElse(UuidGenerator.generate().toString());
        request.setAttribute("traceId", traceId);
        MDC.put("traceId", traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("traceId");
        }
    }

    private void handleOpenTelemetryFlow(HttpServletRequest request,
                                         HttpServletResponse response,
                                         FilterChain filterChain) throws ServletException, IOException {
        Context extracted = openTelemetry.getPropagators()
            .getTextMapPropagator()
            .extract(Context.current(), request, RequestHeaderGetter.INSTANCE);
        String spanName = request.getMethod() + " " + request.getRequestURI();
        Span span = tracer.spanBuilder(spanName)
            .setParent(extracted)
            .setSpanKind(SpanKind.SERVER)
            .startSpan();

        String traceId = span.getSpanContext().getTraceId();
        String spanId = span.getSpanContext().getSpanId();
        request.setAttribute("traceId", traceId);
        request.setAttribute("spanId", spanId);
        MDC.put("traceId", traceId);
        MDC.put("spanId", spanId);
        response.setHeader(TRACE_ID_HEADER, traceId);
        response.setHeader(SPAN_ID_HEADER, spanId);

        try (Scope scope = span.makeCurrent()) {
            filterChain.doFilter(request, response);
            span.setAttribute("http.method", request.getMethod());
            span.setAttribute("http.target", request.getRequestURI());
            span.setAttribute("http.status_code", response.getStatus());
            if (response.getStatus() >= 500) {
                span.setStatus(StatusCode.ERROR);
            }
        } catch (Exception ex) {
            span.recordException(ex);
            span.setStatus(StatusCode.ERROR);
            throw ex;
        } finally {
            MDC.remove("traceId");
            MDC.remove("spanId");
            span.end();
        }
    }

    private static final class RequestHeaderGetter implements TextMapGetter<HttpServletRequest> {
        private static final RequestHeaderGetter INSTANCE = new RequestHeaderGetter();

        @Override
        public Iterable<String> keys(HttpServletRequest carrier) {
            return java.util.Collections.list(carrier.getHeaderNames());
        }

        @Override
        public String get(HttpServletRequest carrier, String key) {
            return carrier.getHeader(key);
        }
    }
}
