package br.com.costumerental.nfe.infrastructure.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private TokenValidationService tokenValidationService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should set Authentication in SecurityContext when token is valid")
    void doFilterInternal_validToken() throws Exception {
        filter = new JwtAuthenticationFilter(tokenValidationService);
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(tokenValidationService.validateToken("valid-token")).thenReturn("rentafit-user");

        filter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo("rentafit-user");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not set Authentication when token is invalid")
    void doFilterInternal_invalidToken() throws Exception {
        filter = new JwtAuthenticationFilter(tokenValidationService);
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(tokenValidationService.validateToken("invalid-token")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not set Authentication when Authorization header is absent")
    void doFilterInternal_noHeader() throws Exception {
        filter = new JwtAuthenticationFilter(tokenValidationService);
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not set Authentication when Authorization header has no Bearer prefix")
    void doFilterInternal_noBearerPrefix() throws Exception {
        filter = new JwtAuthenticationFilter(tokenValidationService);
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
