# 5주차 영화 평점 API 명세

Base URL: `http://localhost:8080`

## 닉네임 중복 확인

- Method: `GET`
- URL: `/members/nickname/{nickname}`
- Path parameter: `nickname` (string)
- Success: `200 OK`

```json
{
  "available": false
}
```

DB에 같은 닉네임이 있으면 `false`, 없으면 `true`입니다.

## 이메일 중복 확인

- Method: `GET`
- URL: `/members/email/{email}`
- Path parameter: `email` (string; URL 인코딩)
- Success: `200 OK`

```json
{
  "available": false
}
```

DB에 같은 이메일이 있으면 `false`, 없으면 `true`입니다. 예를 들어 `noco@umc.com`은 `/members/email/noco%40umc.com`으로 요청할 수 있습니다.

## 회원별 평점 목록

- Method: `GET`
- URL: `/members/{memberId}/ratings`
- Path parameter: `memberId` (number)
- Success: `200 OK`
- Ordering: `ratingId` 내림차순
- Error: 존재하지 않는 회원이면 `404 Not Found`

```json
[
  {
    "ratingId": 2,
    "movieId": 155,
    "score": 4,
    "comment": "다시 봐도 명작",
    "createdAt": "2026-11-01T00:00:00",
    "updatedAt": "2026-11-01T00:00:00"
  },
  {
    "ratingId": 1,
    "movieId": 550,
    "score": 5,
    "comment": "최고의 영화!",
    "createdAt": "2026-11-01T00:00:00",
    "updatedAt": "2026-11-01T00:00:00"
  }
]
```

## 회원가입

- Method: `POST`
- URL: `/members`
- Success: `201 Created`
- Password rules: 영문 대문자·소문자·숫자·특수문자(`!@#$%^&*`) 포함, 8자 이상
- Nickname rules: 2~12자
- Password is bcrypt-hashed in storage and is never returned.

Request:

```json
{
  "email": "new-user@example.com",
  "password": "Movie!1234",
  "nickname": "movieFan"
}
```

Success response:

```json
{
  "isSuccess": true,
  "code": "COMMON201",
  "message": "회원가입에 성공했습니다.",
  "result": {
    "memberId": 3,
    "email": "new-user@example.com",
    "nickname": "movieFan"
  }
}
```

## 평점 등록

- Method: `POST`
- URL: `/members/{memberId}/ratings`
- Request: `movieId` is a positive TMDB ID; `score` is an integer from 1 to 5; `comment` is optional and at most 500 characters.
- Success: `201 Created`, with the generated `ratingId` in `result`.
- Errors: member not found `404`; duplicate member/movie rating `409`; invalid fields `400`.

```json
{
  "movieId": 550,
  "score": 5,
  "comment": "최고의 영화!"
}
```

## 평점 수정

- Method: `PATCH`
- URL: `/members/{memberId}/ratings/{ratingId}`
- Request: required `score` (1~5); optional `comment` (up to 500 characters).
- Success: `200 OK`, with `result: null`.
- Errors: invalid fields `400`; missing rating `404`; another member's rating `403`.

## 평점 삭제

- Method: `DELETE`
- URL: `/members/{memberId}/ratings/{ratingId}`
- Success: `204 No Content` with an empty response body.
- Errors: missing rating `404`; another member's rating `403`.

## Standard write response and errors

Write API success and API errors use `{ isSuccess, code, message, result }`. Validation errors use HTTP `400`; email and nickname conflicts use HTTP `409` with distinct `MEMBER4091` and `MEMBER4092` codes; duplicate ratings use `RATING4091`. Database unique-key violations are translated to the corresponding conflict response.

## CORS

브라우저 프론트 출처 `http://localhost:5173`만 허용합니다. 요청에는 `GET`, `POST`, `PATCH`, `DELETE`를 허용하고 모든 요청 헤더를 허용합니다. 영화 데이터는 프론트가 TMDB에서 직접 조회하며, 이 백엔드는 회원과 평점처럼 서비스 DB에 저장하는 데이터만 제공합니다.
