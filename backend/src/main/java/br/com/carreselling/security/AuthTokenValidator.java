package br.com.carreselling.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthTokenValidator {

    private static final Logger log = LoggerFactory.getLogger(AuthTokenValidator.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final RestTemplate restTemplate;
    private final String authBaseUrl;

    public AuthTokenValidator(RestTemplateBuilder restTemplateBuilder,
                              @Value("${auth.base-url:http://localhost:8081}") String authBaseUrl) {
        this.restTemplate = restTemplateBuilder.build();
        this.authBaseUrl = authBaseUrl == null ? "http://localhost:8081" : authBaseUrl.trim().replaceAll("/+$", "");
    }

    public boolean isValid(String token) {
        if (token == null || token.isBlank()) {
            log.debug("Token validation skipped: null or blank");
            return false;
        }
        String url = authBaseUrl + "/api/user/profile";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                Objects.requireNonNull(HttpMethod.GET),
                entity,
                String.class
            );
            boolean valid = response.getStatusCode().is2xxSuccessful();
            if (!valid) {
                log.warn("Auth service returned {} for GET {}", response.getStatusCode(), url);
            }
            return valid;
        } catch (RestClientException ex) {
            log.warn("Token validation failed calling {}: {}", url, ex.getMessage());
            return false;
        } catch (Exception ex) {
            log.warn("Token validation error: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Decodes the JWT payload (without re-verifying the signature — the auth service already
     * did that in {@link #isValid}) and returns the {@code email} claim, falling back to
     * {@code sub}, then to {@code "unknown"} if neither claim is present.
     */
    public String extractUsername(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return "unknown";
            }
            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            JsonNode payload = MAPPER.readTree(decoded);
            if (payload.has("email") && !payload.get("email").isNull()) {
                return payload.get("email").asText();
            }
            if (payload.has("sub") && !payload.get("sub").isNull()) {
                return payload.get("sub").asText();
            }
        } catch (Exception ex) {
            log.warn("Could not extract username from JWT payload: {}", ex.getMessage());
        }
        return "unknown";
    }
}
