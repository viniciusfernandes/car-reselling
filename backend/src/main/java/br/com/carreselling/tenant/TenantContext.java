package br.com.carreselling.tenant;

import br.com.carreselling.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class TenantContext {

    public int getCurrentCompanyId() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        UserPrincipal principal =
                (UserPrincipal) auth.getPrincipal();

        return principal.companyId();
    }
}
