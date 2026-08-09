package com.devhub.documents;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SearchServiceTest {

    private final SearchService searchService = new SearchService(null, null, null);

    @Test
    void identicalVectorsHaveSimilarityOne() {
        float[] a = {1f, 2f, 3f};
        float[] b = {1f, 2f, 3f};

        assertEquals(1.0, searchService.cosineSimilarity(a, b), 1e-6);
    }

    @Test
    void orthogonalVectorsHaveSimilarityZero() {
        float[] a = {1f, 0f};
        float[] b = {0f, 1f};

        assertEquals(0.0, searchService.cosineSimilarity(a, b), 1e-6);
    }

    @Test
    void oppositeVectorsHaveSimilarityNegativeOne() {
        float[] a = {1f, 0f};
        float[] b = {-1f, 0f};

        assertEquals(-1.0, searchService.cosineSimilarity(a, b), 1e-6);
    }

    @Test
    void mismatchedLengthsReturnZero() {
        float[] a = {1f, 2f};
        float[] b = {1f, 2f, 3f};

        assertEquals(0.0, searchService.cosineSimilarity(a, b), 1e-6);
    }

    @Test
    void emptyVectorsReturnZero() {
        assertEquals(0.0, searchService.cosineSimilarity(new float[0], new float[0]), 1e-6);
    }

    @Test
    void zeroVectorReturnsZeroRatherThanDividingByZero() {
        float[] a = {0f, 0f, 0f};
        float[] b = {1f, 2f, 3f};

        assertEquals(0.0, searchService.cosineSimilarity(a, b), 1e-6);
    }
}
