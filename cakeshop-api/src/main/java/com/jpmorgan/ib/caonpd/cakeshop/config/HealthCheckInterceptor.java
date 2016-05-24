package com.jpmorgan.ib.caonpd.cakeshop.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Component
public class HealthCheckInterceptor extends HandlerInterceptorAdapter {

    private static final String UNHEALTHY_URI = "/unhealthy";
    private static final String ERROR_URI = "/error";

    @Autowired
    private AppStartup appStartup;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (appStartup.isHealthy()
                || request.getRequestURI().indexOf(UNHEALTHY_URI) >= 0
                || request.getRequestURI().indexOf(ERROR_URI) >= 0) {

            return true;
        }

        response.sendRedirect(request.getContextPath() + UNHEALTHY_URI);
        return false;
    }

}