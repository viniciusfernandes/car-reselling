package br.com.carreselling.security;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthTokenValidator {

    private final RestTemplate restTemplate;
    private final String authBaseUrl;

    public AuthTokenValidator(RestTemplateBuilder restTemplateBuilder,
                              @Value("${auth.base-url:http://localhost:8081}") String authBaseUrl) {
        this.restTemplate = restTemplateBuilder.build();
        this.authBaseUrl = authBaseUrl;
    }

    public boolean isValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                authBaseUrl + "/api/user/profile",
                java.util.Objects.requireNonNull(HttpMethod.GET),
                entity,
                String.class
            );
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            return false;
        }
    }

    public String getAuthBaseUrl() {
        return Objects.requireNonNull(authBaseUrl);
    }
}
