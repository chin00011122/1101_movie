package com.umc.movie.controller;

import com.umc.movie.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsEmailUniqueConstraintToMemberEmailConflict() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleDataIntegrity(
                new DataIntegrityViolationException("Duplicate entry for key 'member.email'")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("MEMBER4091");
    }

    @Test
    void mapsNicknameUniqueConstraintToMemberNicknameConflict() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleDataIntegrity(
                new DataIntegrityViolationException("Duplicate entry 'emailFan' for key 'member.nickname'")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("MEMBER4092");
    }

    @Test
    void mapsRatingCompositeUniqueConstraintToRatingConflict() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleDataIntegrity(
                new DataIntegrityViolationException("Duplicate entry for key 'uk_rating_member_movie'")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("RATING4091");
    }
}
