package com.lwf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS跨域资源共享配置类
 * 用于配置全局的跨域访问策略，允许前端应用访问后端API
 */
@Configuration
public class CorsConfig {

    /**
     * 创建并配置CORS过滤器Bean
     * @return CorsFilter 配置好的跨域过滤器
     */
    @Bean
    public CorsFilter corsFilter() {
        // 创建CORS配置对象
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有来源的跨域请求
        config.addAllowedOriginPattern("*");
        // 允许所有的请求头
        config.addAllowedHeader("*");
        // 允许所有的HTTP方法（GET, POST, PUT, DELETE等）
        config.addAllowedMethod("*");
        // 允许发送凭证信息（如cookies）
        config.setAllowCredentials(true);

        // 创建基于URL的CORS配置源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 注册CORS配置，对所有的路径（/**）应用此配置
        source.registerCorsConfiguration("/**", config);
        // 返回配置好的CORS过滤器
        return new CorsFilter(source);
    }
}