package com.isp.backend.global.security;

import com.isp.backend.global.jwt.JwtAccessDeniedHandler;
import com.isp.backend.global.jwt.JwtAuthenticationEntryPoint;
import com.isp.backend.global.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


@RequiredArgsConstructor
@Configuration
@EnableWebSecurity  // Spring Security 활성화
public class WebSecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String[] AUTH_WHITE_LIST = {
            "/api/members/login",
            "/api/members/test",
            "/error",
            "/h2-console/**"
    };


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 들어오는 요청을 처리, 애플리케이션을 보안해주는 필터

        http
                .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()))               // cors 구성
                .csrf(CsrfConfigurer::disable)                                                                       // csrf 보호 비활성화
                .formLogin(AbstractHttpConfigurer::disable)                                                        // 폼 기반 login 비활성화
                .httpBasic(HttpBasicConfigurer::disable)                                                           // HTTP basic 인증 비활성화
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // 세션 생성 X
//                .authorizeHttpRequests(
//                        requests -> {
//                            for (String url : AUTH_WHITE_LIST) {
//                                requests.requestMatchers(url).permitAll();  // 인증 없이 들어가는 url
//                            }
//                            requests.anyRequest().authenticated();
//                        }
//                )

                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/**").permitAll() // 모든 요청에 대해 모든 사용자에게 권한 부여
                )

                .exceptionHandling(exceptionHandlingConfigurer -> exceptionHandlingConfigurer                       // 예외처리 구성 - 인증 진입 점 + 엑세스 거부 헨들러 지정
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler));

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);                  // JWT 기반 인증 처리

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowCredentials(true); // 쿠키 및 자격 증명 허용
        configuration.addAllowedOrigin("*");   // 모든 IP에 응답 허용
        configuration.addAllowedMethod("*");   // 모든 Method 응답 허용
        configuration.addAllowedHeader("*");   // 모든 헤더 응답에 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }



}