package io.github.platovd.alnet.config;

import io.github.platovd.alnet.authentication.contex.SecurityContextWrapper;
import io.github.platovd.alnet.authentication.entrypoint.JWTEntryPointAccessDenied;
import io.github.platovd.alnet.authentication.entrypoint.JWTEntrypointUnauthenticated;
import io.github.platovd.alnet.authentication.filter.JWTAuthenticationFilter;
import io.github.platovd.alnet.authentication.provider.JWTAuthenticationProvider;
import io.github.platovd.alnet.authentication.userdetail.CustomUserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomUserDetailService customUserDetailService;
    private final JWTAuthenticationProvider jwtAuthenticationProvider;
    private final SecurityContextWrapper securityContextWrapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JWTAuthenticationFilter jwtAuthenticationFilter(AuthenticationManager authenticationManager, AuthenticationEntryPoint entryPoint) throws Exception {
        return JWTAuthenticationFilter.builder()
                .authManager(authenticationManager)
                .authenticationEntryPoint(entryPoint)
                .securityContextWrapper(securityContextWrapper)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder
                .authenticationProvider(jwtAuthenticationProvider)
                .userDetailsService(customUserDetailService).passwordEncoder(passwordEncoder());
        return builder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JWTAuthenticationFilter filter,
            JWTEntryPointAccessDenied accessDeniedEP,
            JWTEntrypointUnauthenticated unauthenticatedEP
    ) throws Exception {
        http
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/auth/**", "/error").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-resources/*", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/endpoint", "/admin/**").hasRole("ADMIN")
                        .requestMatchers("/auth/refresh").permitAll()
                        .anyRequest().authenticated()).exceptionHandling(e -> {
                    e.authenticationEntryPoint(unauthenticatedEP).accessDeniedHandler(accessDeniedEP);
                })
                .sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS));
        http.addFilterBefore(filter, BasicAuthenticationFilter.class);
        return http.build();
    }
}
