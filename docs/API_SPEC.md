# 영화 평점 API 명세 (5~7주차)

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

## 평점 등록 (6주차 경로, 현재는 변경됨)

- Method: `POST`
- URL: `/members/{memberId}/ratings`
- Request: `movieId` is a positive TMDB ID; `score` is an integer from 1 to 5; `comment` is optional and at most 500 characters.
- Success: `201 Created`, with the generated `ratingId` in `result`.
- Errors: member not found `404`; duplicate member/movie rating `409`; invalid fields `400`.

7주차부터 쓰기 API는 토큰을 사용하며 현재 경로는 아래의 **인증 기반 평점 쓰기** 명세를 따릅니다.

```json
{
  "movieId": 550,
  "score": 5,
  "comment": "최고의 영화!"
}
```

## 평점 수정 (6주차 경로, 현재는 변경됨)

- Method: `PATCH`
- URL: `/members/{memberId}/ratings/{ratingId}`
- Request: required `score` (1~5); optional `comment` (up to 500 characters).
- Success: `200 OK`, with `result: null`.
- Errors: invalid fields `400`; missing rating `404`; another member's rating `403`.

## 평점 삭제 (6주차 경로, 현재는 변경됨)

- Method: `DELETE`
- URL: `/members/{memberId}/ratings/{ratingId}`
- Success: `204 No Content` with an empty response body.
- Errors: missing rating `404`; another member's rating `403`.

## Standard write response and errors

Write API success and API errors use `{ isSuccess, code, message, result }`. Validation errors use HTTP `400`; email and nickname conflicts use HTTP `409` with distinct `MEMBER4091` and `MEMBER4092` codes; duplicate ratings use `RATING4091`. Database unique-key violations are translated to the corresponding conflict response.

## CORS

브라우저 프론트 출처 `http://localhost:5173`만 허용합니다. 요청에는 `GET`, `POST`, `PATCH`, `DELETE`, `OPTIONS`를 허용하고 모든 요청 헤더를 허용합니다. 영화 데이터는 프론트가 TMDB에서 직접 조회하며, 이 백엔드는 회원과 평점처럼 서비스 DB에 저장하는 데이터만 제공합니다.

## 로그인

- Method: `POST`
- URL: `/auth/login`
- Request: email and password
- Success: `200 OK`; token is returned at `result.accessToken`.
- Invalid email or password: `401 Unauthorized`, code `AUTH401_1` (same response for both cases).

```json
{
  "email": "test@test.com",
  "password": "Test1234!"
}
```

## 내 정보 조회 및 수정

- `GET /members/me`: `Authorization: Bearer {token}` required; returns email, nickname, and createdAt in the common response envelope.
- `PATCH /members/me`: same header; request body contains a 2~12 character nickname. A nickname used by another member returns `409` with code `MEMBER409_1`.
- Missing token returns `401` / `AUTH401_2`; invalid token returns `401` / `AUTH401_3`; expired token returns `401` / `AUTH401_4`.

## 인증 기반 평점 쓰기

All three endpoints require `Authorization: Bearer {token}`. The request body does not contain `memberId`; the server reads the member ID from the token.

| Feature | Method / URL | Success |
| --- | --- | --- |
| Create | `POST /ratings` | `201 Created`, `result` is a rating DTO |
| Update | `PATCH /ratings/{ratingId}` | `200 OK`, `result` is a rating DTO |
| Delete | `DELETE /ratings/{ratingId}` | `200 OK`, `result: null` |

Missing rating returns `404` / `RATING404_1`; another member's rating returns `403` / `RATING403_1`; duplicate member/movie rating returns `409` / `RATING409_1`. Public `GET /members/{memberId}/ratings` remains unchanged.
