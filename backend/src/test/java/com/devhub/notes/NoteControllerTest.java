package com.devhub.notes;

import com.devhub.support.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NoteControllerTest extends BaseIntegrationTest {

    private String createNote(String token, String title, String content) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("title", title, "content", content));
        String response = mockMvc.perform(post("/notes")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void createRequiresTitle() throws Exception {
        String token = registerAndLogin(uniqueEmail("note-validate"));

        mockMvc.perform(post("/notes")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAndGetNote() throws Exception {
        String token = registerAndLogin(uniqueEmail("note-crud"));
        String id = createNote(token, "Meeting notes", "Discussed the roadmap.");

        mockMvc.perform(get("/notes/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Meeting notes"))
                .andExpect(jsonPath("$.content").value("Discussed the roadmap."));
    }

    @Test
    void searchFindsNoteByContent() throws Exception {
        String token = registerAndLogin(uniqueEmail("note-search"));
        createNote(token, "Unrelated", "Nothing relevant here.");
        createNote(token, "Rust notes", "Ownership and borrowing rules.");

        mockMvc.perform(get("/notes").header("Authorization", authHeader(token)).param("q", "borrowing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Rust notes"));
    }

    @Test
    void deleteNote() throws Exception {
        String token = registerAndLogin(uniqueEmail("note-delete"));
        String id = createNote(token, "Temp note", "content");

        mockMvc.perform(delete("/notes/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/notes/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void otherUserCannotAccessNote() throws Exception {
        String ownerToken = registerAndLogin(uniqueEmail("note-owner"));
        String otherToken = registerAndLogin(uniqueEmail("note-other"));
        String id = createNote(ownerToken, "Private note", "secret");

        mockMvc.perform(get("/notes/" + id).header("Authorization", authHeader(otherToken)))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/notes/" + id).header("Authorization", authHeader(otherToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void folderCrudAndOwnershipIsolation() throws Exception {
        String ownerToken = registerAndLogin(uniqueEmail("folder-owner"));
        String otherToken = registerAndLogin(uniqueEmail("folder-other"));

        String createBody = objectMapper.writeValueAsString(Map.of("name", "Work"));
        String createResponse = mockMvc.perform(post("/notes/folders")
                        .header("Authorization", authHeader(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Work"))
                .andReturn().getResponse().getContentAsString();
        String folderId = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(get("/notes/folders").header("Authorization", authHeader(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(put("/notes/folders/" + folderId)
                        .header("Authorization", authHeader(otherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "Hijacked"))))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/notes/folders/" + folderId).header("Authorization", authHeader(ownerToken)))
                .andExpect(status().isNoContent());
    }
}
