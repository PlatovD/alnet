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
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * Основной класс конфигурации безопасности приложения.
 * Настраивает аутентификацию, авторизацию и фильтры безопасности для REST API.
 *
 * @author PlatovD
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Пользовательский сервис для загрузки данных пользователей.
     */
    private final CustomUserDetailService customUserDetailService;

    /**
     * Провайдер аутентификации для JWT токенов.
     */
    private final JWTAuthenticationProvider jwtAuthenticationProvider;

    /**
     * Обертка для работы с контекстом безопасности.
     */
    private final SecurityContextWrapper securityContextWrapper;

    /**
     * Создает и настраивает кодировщик паролей.
     * Использует алгоритм BCrypt для хеширования паролей.
     *
     * @return настроенный кодировщик паролей
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Создает и настраивает фильтр для JWT аутентификации.
     *
     * @param authenticationManager менеджер аутентификации
     * @param entryPoint точка входа для обработки ошибок аутентификации
     * @return настроенный фильтр JWT аутентификации
     * @throws Exception если возникает ошибка при создании фильтра
     */
    @Bean
    public JWTAuthenticationFilter jwtAuthenticationFilter(AuthenticationManager authenticationManager, AuthenticationEntryPoint entryPoint) throws Exception {
        return JWTAuthenticationFilter.builder()
                .authManager(authenticationManager)
                .authenticationEntryPoint(entryPoint)
                .securityContextWrapper(securityContextWrapper)
                .build();
    }

    /**
     * Создает и настраивает менеджер аутентификации.
     *
     * @param http объект HttpSecurity для настройки
     * @return настроенный менеджер аутентификации
     * @throws Exception если возникает ошибка при создании менеджера
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder
                .authenticationProvider(jwtAuthenticationProvider)
                .userDetailsService(customUserDetailService).passwordEncoder(passwordEncoder());
        return builder.build();
    }

    /**
     * Создает и настраивает цепочку фильтров безопасности.
     *
     * @param http объект HttpSecurity для настройки
     * @param filter фильтр JWT аутентификации
     * @param accessDeniedEP обработчик ошибок доступа
     * @param unauthenticatedEP обработчик ошибок аутентификации
     * @return настроенную цепочку фильтров безопасности
     * @throws Exception если возникает ошибка при создании цепочки
     */
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
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/auth/**", "/error").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-resources/*", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/endpoint", "/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()).exceptionHandling(e -> {
                    e.authenticationEntryPoint(unauthenticatedEP).accessDeniedHandler(accessDeniedEP);
                })
                .sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS));
        http.addFilterBefore(filter, BasicAuthenticationFilter.class);
        return http.build();
    }
}