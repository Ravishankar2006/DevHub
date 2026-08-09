package com.devhub.auth;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitFilterTest {

    private MockHttpServletRequest loginRequest(String ip) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");
        request.addHeader("X-Forwarded-For", ip);
        return request;
    }

    private FilterChain passThroughChain() {
        return (req, res) -> { };
    }

    @Test
    void allowsRequestsWithinLimit() throws Exception {
        RateLimitFilter filter = new RateLimitFilter();
        String ip = "192.0.2.1";

        for (int i = 0; i < 5; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(loginRequest(ip), response, passThroughChain());
            assertNotEquals(429, response.getStatus());
        }
    }

    @Test
    void blocksTheSixthRequestInTheSameWindow() throws Exception {
        RateLimitFilter filter = new RateLimitFilter();
        String ip = "192.0.2.2";

        for (int i = 0; i < 5; i++) {
            filter.doFilter(loginRequest(ip), new MockHttpServletResponse(), passThroughChain());
        }

        MockHttpServletResponse sixth = new MockHttpServletResponse();
        filter.doFilter(loginRequest(ip), sixth, passThroughChain());

        assertEquals(429, sixth.getStatus());
    }

    @Test
    void differentIpsHaveIndependentLimits() throws Exception {
        RateLimitFilter filter = new RateLimitFilter();

        for (int i = 0; i < 5; i++) {
            filter.doFilter(loginRequest("192.0.2.3"), new MockHttpServletResponse(), passThroughChain());
        }

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(loginRequest("192.0.2.4"), response, passThroughChain());

        assertNotEquals(429, response.getStatus());
    }

    @Test
    void unrelatedPathsAreNeverLimited() throws Exception {
        RateLimitFilter filter = new RateLimitFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/projects");
        request.addHeader("X-Forwarded-For", "192.0.2.5");

        for (int i = 0; i < 10; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, passThroughChain());
            assertNotEquals(429, response.getStatus());
        }
    }

    @Test
    void getRequestsAreNeverLimited() throws Exception {
        RateLimitFilter filter = new RateLimitFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/login");
        request.addHeader("X-Forwarded-For", "192.0.2.6");

        for (int i = 0; i < 10; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, passThroughChain());
            assertNotEquals(429, response.getStatus());
        }
    }
}
