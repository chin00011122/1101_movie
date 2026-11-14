package com.umc.movie.util;

import com.umc.movie.exception.ApiException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private static final String SECRET = "umc-11th-movie-rating-secret-key-please-change-me";

    @Test
    void createsTokenAndReadsMemberId() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, 3600L);
        String token = jwtUtil.createAccessToken(17L, "member@example.com");

        assertThat(jwtUtil.getMemberId(token)).isEqualTo(17L);
    }

    @Test
    void rejectsTokenSignedWithAnotherSecret() {
        JwtUtil issuer = new JwtUtil(SECRET, 3600L);
        JwtUtil verifier = new JwtUtil("another-valid-secret-key-with-at-least-32-bytes", 3600L);
        String token = issuer.createAccessToken(17L, "member@example.com");

        assertThatThrownBy(() -> verifier.getMemberId(token))
                .isInstanceOf(ApiException.class)
                .hasMessage("유효하지 않은 토큰입니다.");
    }

    @Test
    void rejectsExpiredTokenWithDedicatedError() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, -1L);
        String token = jwtUtil.createAccessToken(17L, "member@example.com");

        assertThatThrownBy(() -> jwtUtil.getMemberId(token))
                .isInstanceOf(ApiException.class)
                .hasMessage("토큰이 만료되었습니다. 다시 로그인해주세요.");
    }
}
