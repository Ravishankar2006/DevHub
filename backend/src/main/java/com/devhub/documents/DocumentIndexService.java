package com.devhub.documents;

import com.devhub.ai.GeminiChatClient;
import com.devhub.ai.GeminiEmbeddingClient;
import com.devhub.common.ApiException;
import com.devhub.jobs.AiJob;
import com.devhub.notes.Note;
import com.devhub.notes.NoteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIndexService {

    private static final int CHUNK_SIZE = 1000;
    private static final int CHUNK_OVERLAP = 100;

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentFileStorageService storageService;
    private final NoteRepository noteRepository;
    private final GeminiChatClient geminiChatClient;
    private final GeminiEmbeddingClient geminiEmbeddingClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public void performIndexing(AiJob job) {
        Document document = documentRepository.findById(job.getTargetId())
                .orElseThrow(() -> new ApiException("Document no longer exists", HttpStatus.NOT_FOUND));

        try {
            String text = resolveText(document);
            if (!StringUtils.hasText(text)) {
                throw new ApiException("No text content found to index", HttpStatus.BAD_REQUEST);
            }

            List<String> chunks = chunkText(text);
            documentChunkRepository.deleteByDocumentId(document.getId());

            int index = 0;
            for (String chunkContent : chunks) {
                float[] embedding = geminiEmbeddingClient.embed(chunkContent, "RETRIEVAL_DOCUMENT");
                DocumentChunk chunk = DocumentChunk.builder()
                        .document(document)
                        .chunkIndex(index++)
                        .content(chunkContent)
                        .embedding(objectMapper.writeValueAsString(embedding))
                        .build();
                documentChunkRepository.save(chunk);
            }

            document.setStatus(DocumentStatus.INDEXED);
            document.setIndexedAt(Instant.now());
            document.setErrorMessage(null);
            documentRepository.save(document);
            log.info("Document indexing completed for document {} ({} chunks)", document.getId(), chunks.size());
        } catch (Exception e) {
            document.setStatus(DocumentStatus.FAILED);
            document.setErrorMessage(e.getMessage());
            documentRepository.save(document);
            throw e instanceof RuntimeException re ? re : new ApiException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String resolveText(Document document) {
        if (document.getSourceType() == DocumentSourceType.NOTE) {
            Note note = noteRepository.findById(document.getSourceId())
                    .orElseThrow(() -> new ApiException("Linked note no longer exists", HttpStatus.NOT_FOUND));
            return note.getContent();
        }

        if (document.getExtractedText() != null) {
            return document.getExtractedText();
        }

        if (document.getFileName() != null && document.getFileName().toLowerCase().endsWith(".pdf")) {
            Resource resource = storageService.loadAsResource(document.getStoragePath());
            byte[] bytes;
            try {
                bytes = resource.getInputStream().readAllBytes();
            } catch (IOException e) {
                throw new ApiException("Could not read uploaded file", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            String extracted = geminiChatClient.extractPdfText(bytes);
            document.setExtractedText(extracted);
            return extracted;
        }

        return null;
    }

    private List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();
        String normalized = text.strip();
        if (normalized.isEmpty()) return chunks;

        int step = CHUNK_SIZE - CHUNK_OVERLAP;
        for (int start = 0; start < normalized.length(); start += step) {
            int end = Math.min(start + CHUNK_SIZE, normalized.length());
            String chunk = normalized.substring(start, end).strip();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }
            if (end == normalized.length()) break;
        }
        return chunks;
    }
}
