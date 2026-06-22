package br.com.carreselling.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.MediaType;

public final class ApiErrorResponseWriter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ApiErrorResponseWriter() {
    }

    public static void write(HttpServletResponse response,
                             int status,
                             List<String> errors,
                             HttpServletRequest request) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        MAPPER.writeValue(response.getOutputStream(), new ApiErrorResponse(errors, traceId(request)));
    }

    private static String traceId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object traceId = request.getAttribute("traceId");
        return traceId == null ? null : traceId.toString();
    }
}
