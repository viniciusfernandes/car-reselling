package br.com.revendacarros.security;

import br.com.revendacarros.config.ApiErrorResponseWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class BearerTokenAuthFilter extends OncePerRequestFilter {

    private final AuthTokenValidator tokenValidator;

    public BearerTokenAuthFilter(AuthTokenValidator tokenValidator) {
        this.tokenValidator = tokenValidator;
    }

    @Override
    protected boolean shouldNotFilter(@org.springframework.lang.NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/")
                || path.startsWith("/api/auth")
                || path.startsWith("/actuator")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui");
    }

    @Override
    protected void doFilterInternal(@org.springframework.lang.NonNull HttpServletRequest request,
                                    @org.springframework.lang.NonNull HttpServletResponse response,
                                    @org.springframework.lang.NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            writeUnauthorized(response, request, "O cabeçalho Authorization é obrigatório.");
            return;
        }

        String token = header.substring(7);
        if (!tokenValidator.isValid(token)) {
            writeUnauthorized(response, request, "Token inválido ou expirado.");
            return;
        }

        try {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                String username = tokenValidator.extractUsername(token);
                int empresaId = tokenValidator.extractEmpresaId(token);
                UserPrincipal principal = new UserPrincipal(username, empresaId);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, java.util.List.of());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        } catch (AuthTokenException ex) {
            writeUnauthorized(response, request, ex.getMessage());
        }
    }

    private static void writeUnauthorized(HttpServletResponse response,
                                          HttpServletRequest request,
                                          String message) throws IOException {
        ApiErrorResponseWriter.write(
                response,
                HttpStatus.UNAUTHORIZED.value(),
                List.of(message),
                request
        );
    }
}
