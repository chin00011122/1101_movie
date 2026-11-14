# UMC 영화 평점 API (5~7주차)

Spring Boot 3.5 / Java 21 프로젝트입니다. 회원가입·로그인, JWT 인증, 내 정보 관리, 평점 조회와 토큰 기반 평점 CRUD를 구현합니다. `favorite`는 다음 주차를 위해 스키마만 준비합니다.

## 로컬 실행

1. MySQL에서 `src/main/resources/db/schema.sql`을 실행합니다. 이 스크립트는 `umc_movie` 데이터베이스, 세 테이블, 테스트용 회원·평점 데이터를 만듭니다.
2. IntelliJ의 `MovieApplication` Run Configuration에서 **Environment variables**를 설정합니다. DB 접속용 `DB_USER`, `DB_PASSWORD`, JWT용 `JWT_SECRET`, `JWT_ACCESS_EXPIRATION`이 필요합니다. 비밀번호와 JWT 비밀키를 코드나 설정 파일에 적거나 Git에 올리지 마세요. 필요하면 `DB_URL`도 지정할 수 있습니다.
3. `MovieApplication`을 실행합니다. 앱은 `ddl-auto: validate`로 기존 스키마와 엔티티를 확인합니다.
4. 기존 프론트 프로젝트 `D:/Dev/UMC/week2/front/umcine`를 `pnpm dev`로 실행합니다. Vite 출처는 `http://localhost:5173`이고, Spring CORS는 이 출처만 허용합니다.

## API 명세

요청과 응답의 필드별 명세는 [`docs/API_SPEC.md`](docs/API_SPEC.md)에 있습니다. 로그인과 인증 이후 요청은 `Authorization: Bearer {token}` 헤더를 사용합니다.

| 기능 | Method / URL | 응답 |
| --- | --- | --- |
| 닉네임 중복 확인 | `GET /members/nickname/{nickname}` | `{ "available": false }` |
| 이메일 중복 확인 | `GET /members/email/{email}` | `{ "available": false }` |
| 회원별 평점 조회 | `GET /members/{memberId}/ratings` | 평점 DTO 배열, 최신순 |
| 로그인 | `POST /auth/login` | Access Token |
| 내 정보 조회/수정 | `GET/PATCH /members/me` | 내 정보 DTO |
| 평점 등록/수정/삭제 | `POST /ratings`, `PATCH/DELETE /ratings/{ratingId}` | 공통 응답 |

회원 ID가 존재하지 않으면 평점 조회는 404를 반환합니다. TMDB의 영화 데이터는 저장하지 않고 `movieId`만 평점에 보관합니다.

## CORS 확인

간이 페이지는 `mock-frontend/index.html`입니다. CORS 설정 전 동작을 확인하려면 `WebConfig`를 잠시 비활성화하고, 백엔드와 간이 페이지를 실행한 뒤 브라우저 Console을 확인합니다. 제출용 최종 상태에서는 `WebConfig`를 다시 활성화한 뒤 간이 페이지 및 UMCine의 회원가입/프로필 화면에서 호출을 확인합니다.

허용 출처는 `http://localhost:5173`으로 제한했습니다. 개발 중 실제 프론트에서 사용하는 출처만 허용하면 다른 웹사이트가 브라우저를 통해 API 응답을 읽는 범위를 줄일 수 있습니다. CORS는 브라우저가 응답을 JavaScript에 공개할지 결정하는 정책이며, Postman이나 네이티브 앱 요청에는 적용되지 않습니다.
