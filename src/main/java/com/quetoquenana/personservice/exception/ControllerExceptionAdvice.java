package com.quetoquenana.personservice.exception;

import com.quetoquenana.personservice.model.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Locale;

@Slf4j
@ControllerAdvice
public class ControllerExceptionAdvice {
    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(ImmutableFieldModificationException.class)
    public ResponseEntity<ApiResponse> handleImmutableFieldModificationException(
            ImmutableFieldModificationException ex, Locale locale) {
        log.error("Attempted to modify immutable field: {}", ex.getMessage());
        String message = messageSource.getMessage(ex.getMessageKey(), ex.getMessageArgs(), locale);
        return ResponseEntity.badRequest().body(new ApiResponse(message, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(RecordNotFoundException.class)
    public ResponseEntity<ApiResponse> handleRecordNotFoundException(
            RecordNotFoundException ex, Locale locale) {
        log.error("RecordNotFoundException: {}", ex.getMessage());
        String message = messageSource.getMessage(ex.getMessageKey(), ex.getMessageArgs(), locale);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(message, HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(DuplicateRecordException.class)
    public ResponseEntity<ApiResponse> handleDuplicateRecordException(
            DuplicateRecordException ex, Locale locale) {
        log.error("Duplicate record: {}", ex.getMessage());
        String message = messageSource.getMessage(ex.getMessageKey(), ex.getMessageArgs(), locale);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse(message, HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(InactiveRecordException.class)
    public ResponseEntity<ApiResponse> handleInactiveRecordException(
            InactiveRecordException ex, Locale locale) {
        log.error("Inactive record: {}", ex.getMessage());
        String message = messageSource.getMessage(ex.getMessageKey(), ex.getMessageArgs(), locale);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(message, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(RecordNotDeletableException.class)
    public ResponseEntity<ApiResponse> handleRecordNotDeletableException(
            RecordNotDeletableException ex, Locale locale) {
        log.error("Record not deletable: {}", ex.getMessage());
        String message = messageSource.getMessage(ex.getMessageKey(), ex.getMessageArgs(), locale);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(message, HttpStatus.BAD_REQUEST.value()));
    }
}
