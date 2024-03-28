package com.provoly.error;

import com.fasterxml.jackson.annotation.JsonCreator;

public class ErrorDto {
    private final int code;
    private final String message;

    @JsonCreator
    public ErrorDto(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
