# Project: Anonymous Board (Clean Architecture)

## Tech Stack
- Java 17+
- Spring Boot 3.x
- Spring Data JPA
- H2 Database (in-memory)
- BCrypt for password hashing

## Architecture: Hexagonal (Ports & Adapters)

### Package Structure
```
com.board.clean
├── domain/                  # 핵심 도메인 (외부 의존성 없음)
│   ├── model/               # 도메인 모델 (Entity가 아닌 순수 도메인 객체)
│   ├── port/
│   │   ├── in/              # Inbound Port (UseCase 인터페이스)
│   │   └── out/             # Outbound Port (Repository 인터페이스)
│   └── exception/           # 도메인 예외
├── application/             # 유스케이스 구현
│   └── service/             # Port(in) 구현체
├── adapter/
│   ├── in/
│   │   └── web/             # Controller + Request/Response DTO
│   └── out/
│       └── persistence/     # JPA Entity + Repository 구현체 (Port(out) 구현)
└── config/                  # Spring 설정
```

### Architecture Rules (반드시 준수)

1. **Domain Layer는 외부 프레임워크에 의존하지 않는다**
   - `domain/model`에는 JPA 어노테이션, Spring 어노테이션 사용 금지
   - 순수 Java 객체로만 구성
   - 도메인 모델 안에 비즈니스 규칙을 캡슐화한다

2. **UseCase별 Port 정의**
   - 각 유스케이스는 별도의 Inbound Port 인터페이스를 갖는다
   - 예: `CreatePostUseCase`, `GetPostListUseCase`, `LikePostUseCase` 등

3. **Adapter는 Port를 통해서만 통신한다**
   - Controller → Inbound Port → Service → Outbound Port → Repository
   - Controller에서 Repository 직접 접근 금지

4. **DTO 변환은 Adapter 레이어에서 처리한다**
   - Web Adapter: Request DTO → Domain Model, Domain Model → Response DTO
   - Persistence Adapter: Domain Model ↔ JPA Entity

5. **입력 유효성 검증**
   - 형식 검증(길이, 필수값 등): Web Adapter (Controller or Request DTO)
   - 비즈니스 규칙 검증: Domain Model 또는 Application Service

6. **에러 처리**
   - 도메인 예외를 정의하고, Web Adapter에서 HTTP 상태코드로 변환
   - `@RestControllerAdvice`로 글로벌 예외 처리

## Testing

### 테스트 전략
- **통합 테스트 (필수)**: `@SpringBootTest` + `MockMvc`로 API 엔드포인트 검증
- **도메인 단위 테스트 (권장)**: 도메인 모델의 비즈니스 규칙을 순수 단위 테스트로 검증

### 통합 테스트 규칙
- 각 API 엔드포인트별로 정상 케이스와 주요 에러 케이스를 테스트한다
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
