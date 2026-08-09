package com.devhub.documents;

import com.devhub.support.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DocumentControllerTest extends BaseIntegrationTest {

    private String uploadDocument(String token, String filename, String content) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", filename, "text/plain", content.getBytes());
        String response = mockMvc.perform(multipart("/documents")
                        .file(file)
                        .header("Authorization", authHeader(token)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void uploadRejectsUnsupportedFileType() throws Exception {
        String token = registerAndLogin(uniqueEmail("doc-validate"));
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", "binary".getBytes());

        mockMvc.perform(multipart("/documents").file(file).header("Authorization", authHeader(token)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadTextFileStartsPending() throws Exception {
        String token = registerAndLogin(uniqueEmail("doc-upload"));

        String response = mockMvc.perform(multipart("/documents")
                        .file(new MockMultipartFile("file", "notes.txt", "text/plain", "Some plain text content.".getBytes()))
                        .header("Authorization", authHeader(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("notes.txt"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(get("/documents").header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id));
    }

    @Test
    void deleteDocument() throws Exception {
        String token = registerAndLogin(uniqueEmail("doc-delete"));
        String id = uploadDocument(token, "temp.txt", "temp content");

        mockMvc.perform(delete("/documents/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/documents").header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void otherUserCannotDeleteDocument() throws Exception {
        String ownerToken = registerAndLogin(uniqueEmail("doc-owner"));
        String otherToken = registerAndLogin(uniqueEmail("doc-other"));
        String id = uploadDocument(ownerToken, "private.txt", "private content");

        mockMvc.perform(delete("/documents/" + id).header("Authorization", authHeader(otherToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchWithNoIndexedContentReturnsEmptyList() throws Exception {
        String token = registerAndLogin(uniqueEmail("search-empty"));

        mockMvc.perform(get("/search").header("Authorization", authHeader(token)).param("q", "anything"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
