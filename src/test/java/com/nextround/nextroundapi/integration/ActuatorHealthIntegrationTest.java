package com.nextround.nextroundapi.integration;

import com.nextround.nextroundapi.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ActuatorHealthIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("GET /actuator/health")
    void getActuatorHealth_Success() throws Exception{
        mockMvc.perform(get("/actuator/health").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist())
                .andExpect(jsonPath("$.details").doesNotExist());

    }

    @Test
    @DisplayName("GET /actuator/env -> unauthenticated request is rejected")
    void getActuatorEnv_Rejected() throws Exception {
        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isUnauthorized()); // Or isNotFound() if exposure excludes it before security
    }


}
