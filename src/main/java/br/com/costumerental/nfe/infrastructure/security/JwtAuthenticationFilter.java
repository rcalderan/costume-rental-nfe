package br.com.costumerental.nfe.infrastructure.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Nao existe tabela de usuario/role no costume-nfe: o Rentafit ja decide quem pode chamar
 * o servico antes do frontend bater aqui. Por isso o filtro so verifica "autenticado ou nao"
 * (sem hierarquia de roles), diferente do SecurityFilter do Rentafit.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenValidationService tokenValidationService;

    public JwtAuthenticationFilter(TokenValidationService tokenValidationService) {
        this.tokenValidationService = tokenValidationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = recoverToken(request);
        if (token != null) {
            String subject = tokenValidationService.validateToken(token);
            if (subject != null) {
                var authentication = new UsernamePasswordAuthenticationToken(subject, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring("Bearer ".length());
    }
}
