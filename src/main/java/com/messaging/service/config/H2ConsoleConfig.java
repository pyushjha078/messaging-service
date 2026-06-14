package com.messaging.service.config;

import jakarta.servlet.Servlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class H2ConsoleConfig {

    @Bean
    ServletRegistrationBean<Servlet> h2ConsoleServlet() throws ReflectiveOperationException {
        Class<?> servletClass = Class.forName("org.h2.server.web.JakartaWebServlet");
        Servlet servlet = (Servlet) servletClass.getDeclaredConstructor().newInstance();
        ServletRegistrationBean<Servlet> registration =
                new ServletRegistrationBean<>(servlet, "/h2-console/*");
        registration.setName("H2Console");
        return registration;
    }
}
