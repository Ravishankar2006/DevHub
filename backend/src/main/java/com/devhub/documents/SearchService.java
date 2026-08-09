package com.devhub.documents;

import com.devhub.ai.GeminiEmbeddingClient;
import com.devhub.documents.dto.SearchResultDto;
import com.devhub.users.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final int MAX_RESULTS = 10;
    private static final int SNIPPET_LENGTH = 240;

    private final DocumentChunkRepository documentChunkRepository;
    private final GeminiEmbeddingClient geminiEmbeddingClient;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<SearchResultDto> search(User currentUser, String query) {
        if (!StringUtils.hasText(query)) {
            return List.of();
        }

        float[] queryEmbedding = geminiEmbeddingClient.embed(query, "RETRIEVAL_QUERY");

        List<DocumentChunk> chunks = documentChunkRepository
                .findByDocumentUserIdAndDocumentStatus(currentUser.getId(), DocumentStatus.INDEXED);

        return chunks.stream()
                .map(chunk -> Map.entry(chunk, cosineSimilarity(queryEmbedding, parseEmbedding(chunk.getEmbedding()))))
                .sorted(Comparator.comparingDouble((Map.Entry<DocumentChunk, Double> e) -> e.getValue()).reversed())
                .limit(MAX_RESULTS)
                .map(entry -> toResultDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private SearchResultDto toResultDto(DocumentChunk chunk, double score) {
        Document document = chunk.getDocument();
        String content = chunk.getContent();
        String snippet = content.length() > SNIPPET_LENGTH ? content.substring(0, SNIPPET_LENGTH) + "..." : content;

        return SearchResultDto.builder()
                .documentId(document.getId())
                .title(document.getTitle())
                .sourceType(document.getSourceType().name())
                .snippet(snippet)
                .score(score)
                .build();
    }

    private float[] parseEmbedding(String json) {
        try {
            return objectMapper.readValue(json, float[].class);
        } catch (Exception e) {
            return new float[0];
        }
    }

    double cosineSimilarity(float[] a, float[] b) {
        if (a.length == 0 || b.length == 0 || a.length != b.length) return 0.0;

        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
