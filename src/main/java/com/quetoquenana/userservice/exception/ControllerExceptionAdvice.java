package com.quetoquenana.userservice.exception;

import com.quetoquenana.userservice.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Locale;

@Slf4j
@ControllerAdvice
public class ControllerExceptionAdvice {

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex
    ) {
        log.error("MethodArgumentNotValidException: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ApiResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex
    ) {
        log.error("DataIntegrityViolationException: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ApiResponse("DataIntegrityViolationException", HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse> handleAuthenticationException(
            AuthenticationException ex,
            Locale locale
    ) {
        log.error("AuthenticationException: {}", ex.getMessage());
        String message = messageSource.getMessage(ex.getMessageKey(), ex.getMessageArgs(), locale);
        return ResponseEntity.badRequest().body(new ApiResponse(message, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(ImmutableFieldModificationException.class)
    public ResponseEntity<ApiResponse> handleImmutableFieldModificationException(
            ImmutableFieldModificationException ex,
            Locale locale) {
        log.error("Attempted to modify immutable field: {}", ex.getMessage());
        String message = messageSource.getMessage(ex.getMessageKey(), ex.getMessageArgs(), locale);
        return ResponseEntity.badRequest().body(new ApiResponse(message, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(RecordNotFoundException.class)
    public ResponseEntity<ApiResponse> handleRecordNotFoundException(
            RecordNotFoundException ex,
            Locale locale
    ) {
        log.error("RecordNotFoundException: {}", ex.getMessage());
        String message = messageSource.getMessage(ex.getMessageKey(), ex.getMessageArgs(), locale);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(message, HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(DuplicateRecordException.class)
    public ResponseEntity<ApiResponse> handleDuplicateRecordException(
            DuplicateRecordException ex,
            Locale locale
    ) {
        log.error("Duplicate record: {}", ex.getMessage());
        String message = messageSource.getMessage(ex.getMessageKey(), ex.getMessageArgs(), locale);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse(message, HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(InactiveRecordException.class)
    public ResponseEntity<ApiResponse> handleInactiveRecordException(
            InactiveRecordException ex,
            Locale locale
    ) {
        log.error("Inactive record: {}", ex.getMessage());
        String message = messageSource.getMessage(ex.getMessageKey(), ex.getMessageArgs(), locale);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(message, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(RecordNotDeletableException.class)
    public ResponseEntity<ApiResponse> handleRecordNotDeletableException(
            RecordNotDeletableException ex,
            Locale locale
    ) {
        log.error("Record not deletable: {}", ex.getMessage());
        String message = messageSource.getMessage(ex.getMessageKey(), ex.getMessageArgs(), locale);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(message, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(InvalidFirebaseTokenException.class)
    public ResponseEntity<ApiResponse> handleInvalidFirebaseToken(InvalidFirebaseTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse("INVALID_FIREBASE_TOKEN -- " + ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(FirebaseUserDisabledException.class)
    public ResponseEntity<ApiResponse> handleFirebaseUserDisabled(FirebaseUserDisabledException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse("FIREBASE_USER_DISABLED -- " + ex.getMessage(), HttpStatus.FORBIDDEN.value()));
    }

    @ExceptionHandler(EmailConflictException.class)
    public ResponseEntity<ApiResponse> handleEmailConflict(EmailConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse("EMAIL_ALREADY_IN_USE -- " + ex.getMessage(), HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse("BAD_REQUEST -- " + ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("INTERNAL_ERROR -- " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}
