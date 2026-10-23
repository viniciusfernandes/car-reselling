package br.com.revendacarros.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Base64;
import java.util.Objects;

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
            log.debug("Validação do token ignorada: nulo ou em branco");
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
                log.warn("Serviço de autenticação retornou {} para GET {}", response.getStatusCode(), url);
            }
            return valid;
        } catch (RestClientException ex) {
            log.warn("Falha na validação do token ao chamar {}: {}", url, ex.getMessage());
            return false;
        } catch (Exception ex) {
            log.warn("Erro na validação do token: {}", ex.getMessage());
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
            String[] pecas = token.split("\\.");
            if (pecas.length < 2) {
                return "unknown";
            }
            byte[] decoded = Base64.getUrlDecoder().decode(pecas[1]);
            JsonNode payload = MAPPER.readTree(decoded);
            if (payload.has("email") && !payload.get("email").isNull()) {
                return payload.get("email").asText();
            }
            if (payload.has("sub") && !payload.get("sub").isNull()) {
                return payload.get("sub").asText();
            }
        } catch (Exception ex) {
            log.warn("Não foi possível extrair o nome de usuário do payload JWT: {}", ex.getMessage());
        }
        return "unknown";
    }

    /**
     * Decodes the JWT payload and returns the {@code companyId} claim (the identifier of the
     * empresa, issued by the external authentication API).
     */
    public int extractEmpresaId(String token) {
        String[] pecas = token.split("\\.");
        if (pecas.length < 2) {
            throw new AuthTokenException("Formato de token inválido.");
        }
        JsonNode payload;
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(pecas[1]);
            payload = MAPPER.readTree(decoded);
        } catch (Exception ex) {
            throw new AuthTokenException("Não foi possível extrair o identificador da empresa do token.");
        }

        int empresaId = readEmpresaIdClaim(payload, "companyId");
        if (empresaId < 0) {
            throw new AuthTokenException("O token não possui uma claim companyId válida.");
        }
        return empresaId;
    }

    private static int readEmpresaIdClaim(JsonNode payload, String claimName) {
        if (!payload.has(claimName) || payload.get(claimName).isNull()) {
            return -1;
        }
        return payload.get(claimName).asInt(-1);
    }
}
