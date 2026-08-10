package br.com.costumerental.nfe.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Sem hierarquia de roles: o costume-nfe so precisa autenticado-ou-401 (ver
 * JwtAuthenticationFilter). Swagger/OpenAPI tambem exige token valido em PROD, decisao
 * confirmada no plano (mais restritivo que o Rentafit hoje).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final TokenValidationService tokenValidationService;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(TokenValidationService tokenValidationService,
                           JsonAuthenticationEntryPoint authenticationEntryPoint) {
        this.tokenValidationService = tokenValidationService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint))
                .authorizeRequests(auth -> auth
                        .antMatchers("/actuator/health", "/actuator/info").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(tokenValidationService), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
