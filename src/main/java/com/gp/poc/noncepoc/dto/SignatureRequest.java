package com.gp.poc.noncepoc.dto;

import lombok.Data;

@Data
public class SignatureRequest {

    private String nonce;

    // optional
    private Long timestamp;

    // example: /api/payments/execute
    private String path;

    // example: POST
    private String method;
}