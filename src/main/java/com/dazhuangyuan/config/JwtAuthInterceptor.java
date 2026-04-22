package com.dazhuangyuan.config;

import com.dazhuangyuan.common.BusinessException;
import com.dazhuangyuan.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT认证拦截器
 */
@Slf4j
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // OPTIONS预检请求放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        String token = jwtUtils.resolveToken(authHeader);

        if (token == null || !jwtUtils.validateToken(token)) {
            throw new BusinessException(401, "请先登录");
        }

        Long userId = jwtUtils.getUserId(token);
        String phone = jwtUtils.getPhone(token);
        request.setAttribute("userId", userId);
        request.setAttribute("phone", phone);

        return true;
    }
}
