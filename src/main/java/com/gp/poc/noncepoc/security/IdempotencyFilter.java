package com.gp.poc.noncepoc.security;

import com.gp.poc.noncepoc.exception.SecurityViolationException;
import com.gp.poc.noncepoc.service.IdempotencyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@Slf4j
public class IdempotencyFilter extends OncePerRequestFilter {

    private final IdempotencyService idempotencyService;
    private final HandlerExceptionResolver resolver;

    public IdempotencyFilter(
            IdempotencyService idempotencyService,
            @Qualifier("handlerExceptionResolver")
            HandlerExceptionResolver resolver
    ) {
        this.idempotencyService = idempotencyService;
        this.resolver = resolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Only apply to money-moving APIs
        return !request.getRequestURI().startsWith("/api/payments");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            Authentication auth =
                    SecurityContextHolder.getContext().getAuthentication();

            String userId = auth.getPrincipal().toString();

            String idemKey = request.getHeader("Idempotency-Key");

            if (idemKey == null || idemKey.isBlank()) {
                throw new SecurityViolationException(
                        400,
                        "IDEMPOTENCY_KEY_MISSING",
                        "Idempotency-Key header is required"
                );
            }

            String redisKey =
                    "idem:" + userId + ":" +
                            request.getRequestURI() + ":" +
                            idemKey;

            // If result already exists ? return it
            var cached = idempotencyService.getResult(redisKey);
            if (cached.isPresent()) {
                log.info("Idempotent request found in cache: {}", redisKey);
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().write(cached.get());
                return;
            }

            // Acquire lock
            boolean acquired = idempotencyService.tryAcquire(redisKey);
            if (!acquired) {
                throw new SecurityViolationException(
                        409,
                        "IDEMPOTENT_REQUEST_IN_PROGRESS",
                        "Request is already being processed"
                );
            }

            filterChain.doFilter(request, response);

        } catch (SecurityViolationException ex) {
            resolver.resolveException(request, response, null, ex);
        }
    }
}