package com.umc.movie.controller;

import com.umc.movie.dto.ApiResponse;
import com.umc.movie.exception.ApiException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("요청값을 확인해주세요.");
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure("COMMON4001", message));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(ApiResponse.failure(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException exception) {
        String detail = exception.getMostSpecificCause().getMessage();
        String normalized = detail == null ? "" : detail.toLowerCase();
        String constraint = normalized;
        int keyPosition = normalized.lastIndexOf("for key ");
        if (keyPosition >= 0) {
            constraint = normalized.substring(keyPosition + "for key ".length())
                    .replaceAll("[`'\"\\s]", "");
        }

        if (constraint.contains("uk_rating_member_movie")) {
            return conflict("RATING4091", "이미 이 영화에 평점을 남기셨습니다.");
        }
        if (constraint.equals("email") || constraint.endsWith(".email") || constraint.endsWith("_email")) {
            return conflict("MEMBER4091", "이미 사용 중인 이메일입니다.");
        }
        if (constraint.equals("nickname") || constraint.endsWith(".nickname") || constraint.endsWith("_nickname")) {
            return conflict("MEMBER4092", "이미 사용 중인 닉네임입니다.");
        }
        return conflict("COMMON4091", "이미 존재하는 데이터입니다.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatus(ResponseStatusException exception) {
        int status = exception.getStatusCode().value();
        String message = exception.getReason() == null ? "요청을 처리할 수 없습니다." : exception.getReason();
        return ResponseEntity.status(exception.getStatusCode())
                .body(ApiResponse.failure("COMMON" + status, message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException exception) {
        return conflict("COMMON4092", exception.getMessage());
    }

    private ResponseEntity<ApiResponse<Void>> conflict(String code, String message) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(code, message));
    }
}
