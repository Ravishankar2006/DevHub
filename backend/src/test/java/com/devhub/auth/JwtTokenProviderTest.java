package com.devhub.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private static final String VALID_SECRET = "test-jwt-secret-key-for-devhub-testing-only-must-be-at-least-32-bytes-long";

    private JwtTokenProvider provider(long accessMs, long refreshMs) {
        return new JwtTokenProvider(VALID_SECRET, accessMs, refreshMs);
    }

    @Test
    void constructorRejectsSecretShorterThan32Bytes() {
        assertThrows(IllegalStateException.class, () -> new JwtTokenProvider("too-short", 900000, 604800000));
    }

    @Test
    void constructorAcceptsSecretAtLeast32BytesEvenWithNonBase64Characters() {
        assertDoesNotThrow(() -> new JwtTokenProvider(VALID_SECRET, 900000, 604800000));
    }

    @Test
    void generatedAccessTokenValidatesAndCarriesEmail() {
        JwtTokenProvider provider = provider(900000, 604800000);
        String token = provider.generateAccessToken("user@example.com");

        assertTrue(provider.validateToken(token));
        assertEquals("user@example.com", provider.getEmailFromToken(token));
    }

    @Test
    void expiredTokenFailsValidation() throws InterruptedException {
        JwtTokenProvider provider = provider(1, 604800000);
        String token = provider.generateAccessToken("user@example.com");

        Thread.sleep(10);

        assertFalse(provider.validateToken(token));
    }

    @Test
    void tamperedTokenFailsValidation() {
        JwtTokenProvider provider = provider(900000, 604800000);
        String token = provider.generateAccessToken("user@example.com");
        String tampered = token.substring(0, token.length() - 4) + "abcd";

        assertFalse(provider.validateToken(tampered));
    }

    @Test
    void garbageTokenFailsValidation() {
        JwtTokenProvider provider = provider(900000, 604800000);

        assertFalse(provider.validateToken("not-a-real-jwt"));
    }
}
