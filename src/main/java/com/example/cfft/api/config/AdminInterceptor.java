package com.example.cfft.api.config;

import com.example.cfft.common.utils.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class AdminInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(AdminInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 记录请求方法和头信息
        logger.info("HTTP Method: " + request.getMethod());
        logger.info("Headers: " + Collections.list(request.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(h -> h, request::getHeader)));

        // 如果请求方法是 OPTIONS，直接返回 true
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())||request.getRequestURI().startsWith("/location") && request.getMethod().equalsIgnoreCase("GET")) {
            return true;
        }

        String token = request.getHeader("token");
        if (token == null || !TokenUtil.verifyToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        return true;
    }
}
