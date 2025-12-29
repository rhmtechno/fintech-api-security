package com.gp.poc.noncepoc.api;

import com.gp.poc.noncepoc.dto.SignatureRequest;
import com.gp.poc.noncepoc.dto.SignatureResponse;
import com.gp.poc.noncepoc.service.SignatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/signature")
@RequiredArgsConstructor
public class SignatureController {

    private final SignatureService signatureService;

    @PostMapping("/generate")
    public SignatureResponse generate(
            @RequestBody SignatureRequest request,
            @RequestHeader("X-Device-Id") String deviceId
    ) {

        // 🔐 Extract userId from JWT context
        String userId = (String) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return signatureService.generate(request, userId, deviceId);
    }
}