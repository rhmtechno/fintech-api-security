package com.gp.poc.noncepoc.api;

import com.gp.poc.noncepoc.service.SessionKeyService;
import com.gp.poc.noncepoc.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final SessionKeyService sessionKeyService;

    @PostMapping("/login")
    public Map<String, Object> login(
            @RequestHeader("X-Device-Id") String deviceId
    ) {
        // ? POC: replace with real auth (DB / OTP / etc.)
        String userId = "user-123";

        String jwt = jwtUtil.generateToken(userId, deviceId);
        String sessionKey = sessionKeyService.issueSessionKey(userId, deviceId);

        return Map.of(
                "accessToken", jwt,
                "tokenType", "Bearer",
                "expiresIn", 900,
                "sessionKey", sessionKey,
                "sessionKeyExpiresIn", 600
        );
    }
}