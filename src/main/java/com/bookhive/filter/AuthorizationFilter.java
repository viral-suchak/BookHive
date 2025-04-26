package com.bookhive.filter;

import com.bookhive.model.User;
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


public class AuthorizationFilter implements Filter {

    // Paths that require ADMIN role
    private static final List<String> ADMIN_PATHS = Arrays.asList(
            "/books/admin",
            "/admin"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        String requestURI = httpRequest.getRequestURI();

        // Check if user is logged in
        if (session != null && session.getAttribute("username") != null) {

            // Get user role from session
            User.Role userRole = (User.Role) session.getAttribute("userRole");

            // Check if path requires admin privileges
            if (isAdminPath(requestURI) && userRole != User.Role.ADMIN) {
                // Redirect non-admin users to main page when they try to access admin paths
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/main");
                return;
            }
        }

        // Continue the filter chain
        chain.doFilter(request, response);
    }

    private boolean isAdminPath(String uri) {
        return ADMIN_PATHS.stream().anyMatch(uri::startsWith);
    }
}