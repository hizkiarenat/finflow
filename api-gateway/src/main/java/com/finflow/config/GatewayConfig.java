package com.finflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.finflow.security.JwtAuthInterceptor;

import lombok.RequiredArgsConstructor;

/**
 * Daftarkan JwtAuthInterceptor agar berlaku untuk semua request
 */
@Configuration
@RequiredArgsConstructor
public class GatewayConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/**"); // berlaku untuk semua path
    }
}
