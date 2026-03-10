package com.ecobank.fundtransferservice.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {
        String customMessage = (String) request.getAttribute("auth_error_message");
        String description = customMessage;
        if (description == null || description.isBlank()) {
            description = authException != null && authException.getMessage() != null
                ? authException.getMessage()
                : "Authentication required";
        }
        if (description.contains("Full authentication is required")) {
            description = "Authentication token is missing or invalid";
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(buildUnauthorizedJson(request.getRequestURI(), description));
    }

    private String buildUnauthorizedJson(String path, String description) {
        return "{"
            + "\"statusCode\":401,"
            + "\"success\":false,"
            + "\"message\":\"Unauthorized\","
            + "\"error\":{"
            + "\"code\":\"AUTHENTICATION_ERROR\","
            + "\"description\":\"" + escapeJson(description) + "\""
            + "},"
            + "\"path\":\"" + escapeJson(path) + "\""
            + "}";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
