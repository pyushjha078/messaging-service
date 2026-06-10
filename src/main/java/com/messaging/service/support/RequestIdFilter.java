package com.messaging.service.support;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.MDC;

@Component
public class RequestIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String id = Optional.ofNullable(req.getHeader("X-Request-Id"))
                .orElse(UUID.randomUUID().toString());
        MDC.put("requestId", id);
        response.setHeader("X-Request-Id", id);
        try {
            filterChain.doFilter(req, response);
        } finally {
            MDC.clear();
        }
    }
}