package com.gp.poc.noncepoc.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gp.poc.noncepoc.service.IdempotencyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    @PostMapping("/execute")
    public ResponseEntity<Map<String, String>> executePayment(
            @RequestHeader("Idempotency-Key") String idemKey,
            HttpServletRequest request
    ) throws Exception {

        String userId = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();

        String redisKey =
                "idem:" + userId + ":" +
                        request.getRequestURI() + ":" +
                        idemKey;

        Map<String, String> response =
                Map.of("status", "PAYMENT_SUCCESS");

        // ✅ Serialize response to JSON
        String json = objectMapper.writeValueAsString(response);

        idempotencyService.storeResult(redisKey, json);

        return ResponseEntity.ok(response);
    }
}