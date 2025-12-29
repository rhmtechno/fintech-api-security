package com.gp.poc.noncepoc.exception;

import lombok.Getter;

@Getter
public class SecurityViolationException extends RuntimeException {

    private final int httpStatus;
    private final String code;

    public SecurityViolationException(
            int httpStatus,
            String code,
            String message
    ) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
    }

}