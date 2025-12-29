package com.gp.poc.noncepoc.service;

import com.gp.poc.noncepoc.dto.SignatureRequest;
import com.gp.poc.noncepoc.dto.SignatureResponse;
import com.gp.poc.noncepoc.utils.HmacUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SignatureService {

    private final SessionKeyService sessionKeyService;

    public SignatureResponse generate(
            SignatureRequest request,
            String userId,
            String deviceId
    ) {

        long timestamp = request.getTimestamp() != null
                ? request.getTimestamp()
                : Instant.now().getEpochSecond();

        // 🔐 Fetch session key (same key used by NonceSecurityFilter)
        String sessionKey =
                sessionKeyService.getSessionKey(userId, deviceId);

        if (sessionKey == null) {
            throw new RuntimeException("Session expired");
        }

        // 🔐 Canonical payload (MUST MATCH FILTER)
        String payload =
                request.getMethod() + "\n" +
                        request.getPath() + "\n" +
                        timestamp + "\n" +
                        request.getNonce();

        String signature =
                HmacUtil.generateHmacSha256(sessionKey, payload);

        return SignatureResponse.builder()
                .userId(userId)
                .deviceId(deviceId)
                .nonce(request.getNonce())
                .timestamp(timestamp)
                .method(request.getMethod())
                .path(request.getPath())
                .payload(payload)
                .signature(signature)
                .build();
    }
}