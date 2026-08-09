package com.devhub.documents;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentIndexServiceTest {

    private final DocumentIndexService documentIndexService =
            new DocumentIndexService(null, null, null, null, null, null, null, null);

    @Test
    void emptyTextProducesNoChunks() {
        assertTrue(documentIndexService.chunkText("").isEmpty());
        assertTrue(documentIndexService.chunkText("   ").isEmpty());
    }

    @Test
    void shortTextProducesASingleChunk() {
        List<String> chunks = documentIndexService.chunkText("A short paragraph of text.");

        assertEquals(1, chunks.size());
        assertEquals("A short paragraph of text.", chunks.get(0));
    }

    @Test
    void longTextIsSplitIntoOverlappingChunks() {
        String text = "x".repeat(2500);

        List<String> chunks = documentIndexService.chunkText(text);

        // step = 1000 - 100 = 900; chunk boundaries at 0, 900, 1800 -> 3 chunks for 2500 chars.
        assertEquals(3, chunks.size());
        assertEquals(1000, chunks.get(0).length());
        assertEquals(1000, chunks.get(1).length());
        assertEquals(700, chunks.get(2).length());
    }

    @Test
    void textExactlyAtChunkBoundaryProducesExpectedChunks() {
        String text = "x".repeat(1000);

        List<String> chunks = documentIndexService.chunkText(text);

        assertEquals(1, chunks.size());
        assertEquals(1000, chunks.get(0).length());
    }

    @Test
    void chunksDoNotIncludeEmptyTrailingPieces() {
        String text = "word ".repeat(400).strip();

        List<String> chunks = documentIndexService.chunkText(text);

        assertTrue(chunks.stream().noneMatch(String::isBlank));
    }
}
