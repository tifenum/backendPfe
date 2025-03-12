package com.pfe.users.cors;

import org.apache.catalina.filters.CorsFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;


@Configuration
public class CorsConfig {

    @Bean
    public FilterRegistrationBean customCorsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:3000"); // Frontend origin
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);  // Apply to all paths

        // Create the CorsFilter
        CorsFilter corsFilter = new CorsFilter();
        FilterRegistrationBean bean = new FilterRegistrationBean(corsFilter);

        // Set the order to make sure this filter is executed first
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);

        // Add logging to ensure the filter is being applied
        System.out.println("CORS filter has been applied with allowed origin: http://localhost:3000");

        return bean;
    }
}
