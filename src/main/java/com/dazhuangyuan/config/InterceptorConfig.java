package com.dazhuangyuan.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器注册配置
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthInterceptor jwtAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 需要登录才能访问的接口
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns(
                        "/api/auth/userInfo",
                        "/api/auth/updateProfile",
                        "/api/volunteer/**",
                        "/api/pay/createOrder",
                        "/api/pay/status/**",
                        "/api/profile/**",
                        "/api/admin/**"
                );
    }
}
