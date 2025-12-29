package com.gp.poc.noncepoc.security;

import com.gp.poc.noncepoc.exception.SecurityViolationException;
import com.gp.poc.noncepoc.service.RedisNonceService;
import com.gp.poc.noncepoc.service.SessionKeyService;
import com.gp.poc.noncepoc.utils.HmacUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@Component
public class NonceSecurityFilter extends OncePerRequestFilter {

    private final RedisNonceService nonceService;
    private final SessionKeyService sessionKeyService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Value("${security.nonce.ttl-seconds:300}")
    private long nonceTtlSeconds;

    public NonceSecurityFilter(RedisNonceService nonceService,
                               SessionKeyService sessionKeyService,
                                @Qualifier("handlerExceptionResolver")
                               HandlerExceptionResolver handlerExceptionResolver) {
        this.nonceService = nonceService;
        this.sessionKeyService = sessionKeyService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/auth/")
                || path.startsWith("/internal/")
                || path.startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {

            // ---------------------------
            // Authentication check
            // ---------------------------
            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || authentication.getPrincipal() == null) {
                throw new SecurityViolationException(
                        401,
                        "UNAUTHENTICATED",
                        "User is not authenticated"
                );
            }

            String userId = authentication.getPrincipal().toString();

            // ---------------------------
            // Headers
            // ---------------------------
            String deviceId = request.getHeader("X-Device-Id");
            String nonce = request.getHeader("X-Nonce");
            String timestampHeader = request.getHeader("X-Timestamp");
            String signature = request.getHeader("X-Signature");

            if (deviceId == null || nonce == null
                    || timestampHeader == null || signature == null) {

                throw new SecurityViolationException(
                        400,
                        "INVALID_REQUEST",
                        "Missing security headers"
                );
            }

            // ---------------------------
            // Timestamp
            // ---------------------------
            long timestamp;
            try {
                timestamp = Long.parseLong(timestampHeader);
            } catch (Exception e) {
                throw new SecurityViolationException(
                        400,
                        "INVALID_TIMESTAMP",
                        "Timestamp must be epoch seconds"
                );
            }

            long now = Instant.now().getEpochSecond();
            if (Math.abs(now - timestamp) > 300) {
                throw new SecurityViolationException(
                        401,
                        "REQUEST_EXPIRED",
                        "Request timestamp is expired"
                );
            }

            // ---------------------------
            // Session key
            // ---------------------------
            String sessionKey =
                    sessionKeyService.getSessionKey(userId, deviceId);

            if (sessionKey == null) {
                throw new SecurityViolationException(
                        401,
                        "SESSION_EXPIRED",
                        "Session expired"
                );
            }

            // ---------------------------
            // Nonce replay
            // ---------------------------
            String nonceKey =
                    "nonce:" + userId + ":" +
                            request.getMethod() + ":" +
                            request.getRequestURI() + ":" +
                            nonce;

            if (nonceService.isReplay(nonceKey)) {
                throw new SecurityViolationException(
                        401,
                        "REPLAY_ATTACK",
                        "Replay attack detected"
                );
            }

            // ---------------------------
            // Signature
            // ---------------------------
            String payload =
                    request.getMethod() + "\n" +
                            request.getRequestURI() + "\n" +
                            timestamp + "\n" +
                            nonce;

            String expectedSignature =
                    HmacUtil.generateHmacSha256(sessionKey, payload);

            byte[] expected = Base64.getDecoder()
                    .decode(expectedSignature.trim());
            byte[] actual = Base64.getDecoder()
                    .decode(signature.trim());

            if (!MessageDigest.isEqual(expected, actual)) {
                throw new SecurityViolationException(
                        401,
                        "INVALID_SIGNATURE",
                        "Signature verification failed"
                );
            }

            // ---------------------------
            // Store nonce AFTER validation
            // ---------------------------
            nonceService.storeNonce(nonceKey, nonceTtlSeconds);

            filterChain.doFilter(request, response);

        } catch (SecurityViolationException ex) {

            handlerExceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    ex
            );
        }
    }
}