package br.com.revendacarros.tenant;

import br.com.revendacarros.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class TenantContext {

    public int getCurrentEmpresaId() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        UserPrincipal principal =
                (UserPrincipal) auth.getPrincipal();

        return principal.empresaId();
    }
}
