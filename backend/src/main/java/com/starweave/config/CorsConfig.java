package com.starweave.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        String origins = allowedOrigins.strip();
        if ("*".equals(origins)) {
            // 开发环境：允许任意来源（不含 credentials）
            config.addAllowedOriginPattern("*");
            config.setAllowCredentials(false);
        } else {
            // 生产环境：白名单特定域名
            for (String origin : origins.split(",")) {
                origin = origin.strip();
                if (!origin.isEmpty()) {
                    config.addAllowedOrigin(origin);
                }
            }
            config.setAllowCredentials(true);
        }
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
