package com.devhub.goals;

import com.devhub.support.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HabitControllerTest extends BaseIntegrationTest {

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private HabitCheckinRepository habitCheckinRepository;

    private String createHabit(String token, String title) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("title", title));
        String response = mockMvc.perform(post("/habits")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void createRequiresTitle() throws Exception {
        String token = registerAndLogin(uniqueEmail("habit-validate"));

        mockMvc.perform(post("/habits")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void toggleTodayCheckinTogglesOnAndOff() throws Exception {
        String token = registerAndLogin(uniqueEmail("habit-toggle"));
        String id = createHabit(token, "Read daily");

        mockMvc.perform(post("/habits/" + id + "/checkins/today").header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkedInToday").value(true))
                .andExpect(jsonPath("$.currentStreak").value(1));

        mockMvc.perform(post("/habits/" + id + "/checkins/today").header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkedInToday").value(false))
                .andExpect(jsonPath("$.currentStreak").value(0));
    }

    @Test
    @Transactional
    void currentStreakCountsConsecutiveDaysEndingToday() throws Exception {
        String token = registerAndLogin(uniqueEmail("habit-streak"));
        String id = createHabit(token, "Exercise");
        UUID habitId = UUID.fromString(id);
        Habit habit = habitRepository.findById(habitId).orElseThrow();

        LocalDate today = LocalDate.now();
        for (int i = 0; i < 4; i++) {
            habitCheckinRepository.save(HabitCheckin.builder().habit(habit).checkinDate(today.minusDays(i)).build());
        }
        // A gap two days before the 4-day run -- must not be counted into the current streak.
        habitCheckinRepository.save(HabitCheckin.builder().habit(habit).checkinDate(today.minusDays(6)).build());

        mockMvc.perform(get("/habits/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStreak").value(4))
                .andExpect(jsonPath("$.checkedInToday").value(true));
    }

    @Test
    @Transactional
    void currentStreakIsZeroWhenMissedYesterdayAndToday() throws Exception {
        String token = registerAndLogin(uniqueEmail("habit-broken-streak"));
        String id = createHabit(token, "Meditate");
        UUID habitId = UUID.fromString(id);
        Habit habit = habitRepository.findById(habitId).orElseThrow();

        LocalDate today = LocalDate.now();
        habitCheckinRepository.save(HabitCheckin.builder().habit(habit).checkinDate(today.minusDays(3)).build());
        habitCheckinRepository.save(HabitCheckin.builder().habit(habit).checkinDate(today.minusDays(2)).build());

        mockMvc.perform(get("/habits/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStreak").value(0))
                .andExpect(jsonPath("$.longestStreak").value(2))
                .andExpect(jsonPath("$.checkedInToday").value(false));
    }

    @Test
    void deleteHabit() throws Exception {
        String token = registerAndLogin(uniqueEmail("habit-delete"));
        String id = createHabit(token, "Temp habit");

        mockMvc.perform(delete("/habits/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/habits/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void otherUserCannotAccessHabit() throws Exception {
        String ownerToken = registerAndLogin(uniqueEmail("habit-owner"));
        String otherToken = registerAndLogin(uniqueEmail("habit-other"));
        String id = createHabit(ownerToken, "Private habit");

        mockMvc.perform(get("/habits/" + id).header("Authorization", authHeader(otherToken)))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/habits/" + id + "/checkins/today").header("Authorization", authHeader(otherToken)))
                .andExpect(status().isNotFound());
    }
}
