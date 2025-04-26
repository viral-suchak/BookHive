package com.bookhive.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class LoginFilter implements Filter {

    // Paths that are accessible without login
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/users/login",
            "/users/register",
            "/style.css",
            "/home.css"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        String requestURI = httpRequest.getRequestURI();

        // Allow access to public paths without login
        if (isPublicPath(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // If not a public path, check if user is logged in
        if (session == null || session.getAttribute("username") == null) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/users/login");
            return;
        }

        // User is logged in, continue
        chain.doFilter(request, response);
    }

    private boolean isPublicPath(String uri) {
        return PUBLIC_PATHS.stream().anyMatch(uri::endsWith) ||
                uri.contains("/css/");
    }
}