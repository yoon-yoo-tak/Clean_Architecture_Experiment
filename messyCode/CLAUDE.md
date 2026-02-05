# Project: Anonymous Board (Messy)

## Tech Stack
- Java 17+
- Spring Boot 3.x
- Spring Data JPA
- H2 Database (in-memory)
- BCrypt for password hashing

## Architecture Rules (반드시 준수)

이 프로젝트는 의도적으로 아래의 스타일로 작성한다. 리팩토링하지 말 것.

### Package Structure
```
com.board.messy
├── controller/    # Controller (비즈니스 로직 포함 가능)
├── service/       # Service (God Class 스타일)
├── repository/    # JPA Repository
├── entity/        # JPA Entity (DTO 겸용)
└── config/        # Spring 설정
```

### 코딩 스타일 (반드시 따를 것)

1. **Service는 기능 도메인별로 하나씩만 만든다**
   - `PostService` 하나에 게시글 관련 모든 로직 포함
   - `CommentService` 하나에 댓글 관련 모든 로직 포함
   - 메서드가 길어져도 분리하지 않는다

2. **JPA Entity를 API 응답에 그대로 사용한다**
   - 별도의 Response DTO 클래스를 만들지 않는다
   - `@JsonIgnore`로 숨길 필드를 처리한다
   - 필요하면 Entity에 API 응답용 필드/메서드를 추가한다
   - 목록 조회 등 별도 형태가 필요한 경우에만 `Map<String, Object>` 또는 inline DTO를 사용한다

3. **비즈니스 로직을 Controller에 일부 포함시킨다**
   - 예: 비밀번호 검증, 조건 분기 등을 Controller에서 직접 처리해도 된다
   - Service와 Controller 사이에 로직이 분산되어도 괜찮다

4. **중복 코드를 허용한다**
   - 비슷한 로직이 여러 곳에 있어도 공통 메서드로 추출하지 않는다
   - 각 메서드가 독립적으로 전체 로직을 포함한다

5. **에러 처리는 각 메서드에서 직접 한다**
   - 글로벌 예외 핸들러를 만들지 않는다
   - 각 Controller 메서드에서 try-catch 또는 if-else로 직접 처리한다
   - `ResponseEntity`로 상태코드를 직접 반환한다

6. **매직 넘버와 하드코딩을 사용한다**
   - 상수를 별도로 정의하지 않는다
   - 예: 페이지 크기를 `10`, 해시태그 최대 개수를 `5`로 코드에 직접 작성

7. **도메인 검증 로직을 Entity나 별도 클래스로 분리하지 않는다**
   - 모든 검증은 Service 또는 Controller 메서드 내에서 if문으로 처리

## Testing

### 테스트 규칙
- `@SpringBootTest` + `MockMvc`로 API 엔드포인트를 통합 테스트한다
- 단위 테스트는 작성하지 않는다. 통합 테스트만 작성한다
- 하나의 테스트 클래스에 관련 API 테스트를 모두 포함한다 (예: `PostApiTest`에 게시글 CRUD 전부)
- 테스트 데이터는 각 테스트 메서드에서 직접 설정한다
- H2 in-memory DB를 사용하므로 별도 테스트 DB 설정 불필요
- `@Transactional`로 테스트 간 데이터 격리

### 검증 항목 (모든 API 공통)
- HTTP 상태 코드
- 응답 본문의 필수 필드 존재 여부
- 비즈니스 규칙 (비밀번호 불일치 시 403, 리소스 없음 시 404 등)

## API Specification
- 상위 디렉토리의 `api-spec.md` 참조

## Build & Run
```bash
./gradlew bootRun
./gradlew test
```
