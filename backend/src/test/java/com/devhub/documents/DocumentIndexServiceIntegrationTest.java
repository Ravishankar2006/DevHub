package com.devhub.documents;

import com.devhub.jobs.AiJob;
import com.devhub.support.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DocumentIndexServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private DocumentIndexService documentIndexService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @Test
    void performIndexingChunksTextAndFlipsStatusToIndexed() throws Exception {
        when(geminiEmbeddingClient.embed(anyString(), anyString())).thenReturn(new float[]{0.1f, 0.2f, 0.3f});

        String token = registerAndLogin(uniqueEmail("index-integration"));
        String content = "This is a short document about semantic search and embeddings.";
        MockMultipartFile file = new MockMultipartFile("file", "notes.txt", "text/plain", content.getBytes());

        String response = mockMvc.perform(multipart("/documents").file(file).header("Authorization", authHeader(token)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID documentId = UUID.fromString(objectMapper.readTree(response).get("id").asText());

        documentIndexService.performIndexing(AiJob.builder().targetId(documentId).build());

        Document document = documentRepository.findById(documentId).orElseThrow();
        assertEquals(DocumentStatus.INDEXED, document.getStatus());

        long chunkCount = documentChunkRepository.findAll().stream()
                .filter(chunk -> chunk.getDocument().getId().equals(documentId))
                .count();
        assertEquals(1, chunkCount);
    }

    @Test
    void performIndexingMarksDocumentFailedWhenTextExtractionFails() throws Exception {
        when(geminiChatClient.extractPdfText(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("Gemini unavailable"));

        String token = registerAndLogin(uniqueEmail("index-fail"));
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", "%PDF-1.4 fake".getBytes());

        String response = mockMvc.perform(multipart("/documents").file(file).header("Authorization", authHeader(token)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID documentId = UUID.fromString(objectMapper.readTree(response).get("id").asText());

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> documentIndexService.performIndexing(AiJob.builder().targetId(documentId).build()));

        Document document = documentRepository.findById(documentId).orElseThrow();
        assertEquals(DocumentStatus.FAILED, document.getStatus());
    }
}
