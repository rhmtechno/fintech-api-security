package com.gp.poc.noncepoc.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignatureResponse {

    private String userId;
    private String deviceId;

    private String nonce;
    private long timestamp;

    private String method;
    private String path;

    private String payload;
    private String signature;
}