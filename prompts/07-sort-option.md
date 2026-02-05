게시글 목록에 정렬 옵션을 추가해줘.

## 변경 요구사항

기존에 최신순(createdAt DESC)으로만 정렬되던 게시글 목록에 **정렬 옵션**을 추가한다.

### API 변경사항

`GET /api/posts`

**추가 Query Parameter:**

| Parameter | Type   | Default  | Description |
|-----------|--------|----------|-------------|
| sort      | string | latest   | 정렬 기준: latest, views, likes |

**정렬 기준:**
- `latest`: 최신순 (createdAt DESC) — 기존과 동일
- `views`: 조회수 높은순 (viewCount DESC), 동일 시 최신순
- `likes`: 좋아요 많은순 (likeCount DESC), 동일 시 최신순

**에러 처리:**
- 허용되지 않은 sort 값: `400 Bad Request`

### 구현 시 고려사항
- 기존 검색 기능과 함께 동작해야 함 (searchType + keyword + sort 조합)
- 페이징과 함께 동작해야 함

## 테스트

- sort=latest 정상 동작 (기존과 동일)
- sort=views 정렬 확인
- sort=likes 정렬 확인
- 동일 값일 때 2차 정렬(최신순) 확인
- 검색 + 정렬 조합 동작
- 허용되지 않은 sort 값 시 400
- sort 파라미터 없을 때 기본값(latest) 동작
- CLAUDE.md의 테스트 규칙을 따를 것

구현 완료 후 `./gradlew test`로 테스트가 통과하는지 확인해줘.

CLAUDE.md에 정의된 아키텍처 규칙을 반드시 따라서 구현해줘.
