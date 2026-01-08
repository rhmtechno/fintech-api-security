package com.gp.poc.noncepoc.security;

import com.gp.poc.noncepoc.service.RateLimitingService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RateLimitingFilterTest {

    @Mock
    private RateLimitingService rateLimitingService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Bucket bucket;

    @Mock
    private ConsumptionProbe probe;

    @InjectMocks
    private RateLimitingFilter rateLimitingFilter;

    @Test
    void shouldAllowRequestWhenBucketIsNotFull() throws Exception {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRequestURI()).thenReturn("/api/data");
        when(rateLimitingService.resolveBucket(anyString(), anyString())).thenReturn(bucket);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);
        when(probe.isConsumed()).thenReturn(true);
        when(probe.getRemainingTokens()).thenReturn(9L);

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        verify(response).addHeader("X-RateLimit-Remaining", "9");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldBlockRequestWhenBucketIsFull() throws Exception {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRequestURI()).thenReturn("/api/data");
        when(rateLimitingService.resolveBucket(anyString(), anyString())).thenReturn(bucket);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);
        when(probe.isConsumed()).thenReturn(false);
        when(probe.getNanosToWaitForRefill()).thenReturn(5_000_000_000L);

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        verify(response).addHeader("X-RateLimit-Retry-After-Seconds", "5");
        verify(response).sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Too Many Requests");
        verify(filterChain, never()).doFilter(request, response);
    }
}
