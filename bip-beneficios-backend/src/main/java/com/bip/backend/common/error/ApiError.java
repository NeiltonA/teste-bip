package com.bip.backend.common.error;

import java.time.Instant;
import java.util.List;
import lombok.Value;

@Value
public class ApiError {

    Instant timestamp;
    int status;
    String error;
    List<String> messages;

    public static ApiError of(int status, String error, String message) {
        return new ApiError(Instant.now(), status, error, List.of(message));
    }

    public static ApiError of(int status, String error, List<String> messages) {
        return new ApiError(Instant.now(), status, error, messages);
    }
}
