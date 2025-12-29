package com.gp.poc.noncepoc.utils;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
@Service
public class HmacUtil {

    public static String generateHmacSha256(
            String secret,
            String payload
    ) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(key);

            byte[] rawHmac = mac.doFinal(
                    payload.getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getEncoder().encodeToString(rawHmac);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC", e);
        }
    }
}