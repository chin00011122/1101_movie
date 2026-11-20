package com.umc.movie.service;

import com.umc.movie.dto.*;
import com.umc.movie.entity.*;
import com.umc.movie.exception.ApiException;
import com.umc.movie.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favorites;
    private final MemberRepository members;

    @Transactional
    public FavoriteResponse create(Long memberId, CreateFavoriteRequest request) {
        Member member = members.findById(memberId).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "MEMBER404_1", "해당 회원을 찾을 수 없습니다."));
        if (favorites.existsByMember_MemberIdAndMovieId(memberId, request.movieId())) {
            throw new ApiException(HttpStatus.CONFLICT, "FAVORITE409_1", "이미 이 영화를 즐겨찾기하셨습니다.");
        }
        return FavoriteResponse.from(favorites.save(Favorite.builder().member(member).movieId(request.movieId()).build()));
    }

    @Transactional(readOnly = true)
    public List<FavoriteResponse> list(Long memberId) {
        return favorites.findByMember_MemberIdOrderByFavoriteIdDesc(memberId).stream().map(FavoriteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public FavoriteStatusResponse status(Long memberId, Long movieId) {
        return new FavoriteStatusResponse(favorites.existsByMember_MemberIdAndMovieId(memberId, movieId));
    }

    @Transactional
    public void delete(Long memberId, Long movieId) {
        Favorite favorite = favorites.findByMember_MemberIdAndMovieId(memberId, movieId).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "FAVORITE404_1", "즐겨찾기한 영화를 찾을 수 없습니다."));
        favorites.delete(favorite);
    }
}
