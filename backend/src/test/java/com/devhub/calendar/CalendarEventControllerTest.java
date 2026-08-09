package com.devhub.calendar;

import com.devhub.support.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CalendarEventControllerTest extends BaseIntegrationTest {

    private String createEvent(String token, String title, Instant startTime) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("title", title, "startTime", startTime.toString()));
        String response = mockMvc.perform(post("/calendar/events")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void createRequiresTitleAndStartTime() throws Exception {
        String token = registerAndLogin(uniqueEmail("cal-validate"));

        mockMvc.perform(post("/calendar/events")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAndGetEvent() throws Exception {
        String token = registerAndLogin(uniqueEmail("cal-crud"));
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        String id = createEvent(token, "Interview", start);

        mockMvc.perform(get("/calendar/events/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Interview"));
    }

    @Test
    void listFiltersByTimeRange() throws Exception {
        String token = registerAndLogin(uniqueEmail("cal-range"));
        Instant now = Instant.now();
        createEvent(token, "Within range", now.plus(2, ChronoUnit.DAYS));
        createEvent(token, "Outside range", now.plus(30, ChronoUnit.DAYS));

        mockMvc.perform(get("/calendar/events")
                        .header("Authorization", authHeader(token))
                        .param("from", now.toString())
                        .param("to", now.plus(7, ChronoUnit.DAYS).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Within range"));
    }

    @Test
    void deleteEvent() throws Exception {
        String token = registerAndLogin(uniqueEmail("cal-delete"));
        String id = createEvent(token, "Temp event", Instant.now().plus(1, ChronoUnit.DAYS));

        mockMvc.perform(delete("/calendar/events/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/calendar/events/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void otherUserCannotAccessEvent() throws Exception {
        String ownerToken = registerAndLogin(uniqueEmail("cal-owner"));
        String otherToken = registerAndLogin(uniqueEmail("cal-other"));
        String id = createEvent(ownerToken, "Private event", Instant.now().plus(1, ChronoUnit.DAYS));

        mockMvc.perform(get("/calendar/events/" + id).header("Authorization", authHeader(otherToken)))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/calendar/events/" + id).header("Authorization", authHeader(otherToken)))
                .andExpect(status().isNotFound());
    }
}
