package com.nextround.nextroundapi.integration;

import com.nextround.nextroundapi.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CorsIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("OPTIONS /api/v1/applications -> valid preflight from allowed origin returns 200 OK")
    void preflightRequest_AllowedOrigin_Returns200AndCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/v1/applications")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,DELETE,PATCH,OPTIONS"));
    }

    @Test
    @DisplayName("OPTIONS /api/v1/applications -> preflight from disallowed origin returns 403 Forbidden")
    void preflightRequest_DisallowedOrigin_ReturnsForbidden() throws Exception {
        mockMvc.perform(options("/api/v1/applications")
                        .header(HttpHeaders.ORIGIN, "http://malicious-site.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET with allowed Origin and no token -> 401 still includes CORS header")
    void unauthenticatedRequest_AllowedOrigin_Returns401WithCorsHeader() throws Exception {
        mockMvc.perform(get("/api/v1/applications")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
    }
}