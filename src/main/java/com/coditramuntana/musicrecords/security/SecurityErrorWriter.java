package com.coditramuntana.musicrecords.security;

import com.coditramuntana.musicrecords.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Writes security failures using the very same {@link ErrorResponse} contract that
 * {@code GlobalExceptionHandler} produces, so a 401 or a 403 is indistinguishable in shape
 * from a 404 or a 409 for any client.
 *
 * <p>This exists because {@code @RestControllerAdvice} cannot help here: authentication and
 * authorization failures are raised inside the filter chain, before the dispatcher servlet
 * ever runs.
 */
@Component
@RequiredArgsConstructor
public class SecurityErrorWriter {

    /**
     * The Spring managed ObjectMapper is injected rather than instantiated, so it carries the
     * JavaTimeModule needed to serialise the LocalDateTime of the payload.
     */
    private final ObjectMapper objectMapper;

    public void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .build();

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }

}
