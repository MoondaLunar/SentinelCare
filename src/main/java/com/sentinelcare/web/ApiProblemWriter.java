package com.sentinelcare.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Writes RFC 7807 problem+json responses from places outside MVC dispatch
 * (the JWT filter and the security exception handlers), where a controller
 * advice cannot reach.
 */
public class ApiProblemWriter {

    private final ObjectMapper objectMapper;

    public ApiProblemWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletResponse response, int status, String title, String detail) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "about:blank");
        body.put("title", title);
        body.put("status", status);
        body.put("detail", detail);
        response.setStatus(status);
        response.setContentType("application/problem+json");
        objectMapper.writeValue(response.getOutputStream(), body);
    }

    public void write(HttpServletResponse response, HttpStatus status, String title, String detail) throws IOException {
        write(response, status.value(), title, detail);
    }
}