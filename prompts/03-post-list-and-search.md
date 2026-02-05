게시글 목록 조회와 검색 기능을 구현해줘.

## 구현할 API

상위 디렉토리의 `api-spec.md`를 참조하여 아래 기능을 구현해줘:

1. **게시글 목록 조회** (GET /api/posts)
   - 최신순(createdAt DESC) 정렬
   - 페이징: page(0-based), size(10 또는 20만 허용, 기본값 10)
   - 응답에 포함할 정보:
     - `totalPostCount`: 전체 게시글 수 (검색 필터 무관, 전체)
     - `totalCommentCount`: 전체 댓글 수 (검색 필터 무관, 전체, 삭제된 댓글 제외)
     - 각 게시글: id, title, author, createdAt, commentCount, viewCount, likeCount
     - `isNew`: 작성일이 현재 기준 3일 이내이면 true
   - 페이징 메타: page, size, totalPages, totalElements

2. **검색 기능**
   - `searchType` 파라미터: title, author, hashtag, content 중 하나
   - `keyword` 파라미터: 검색어
   - searchType과 keyword가 모두 있을 때만 검색 적용
   - 부분 일치 검색 (LIKE '%keyword%')
   - hashtag 검색은 해시태그 이름과 정확히 일치

## 테스트

구현한 API에 대한 테스트 코드를 작성해줘:
- 페이징 정상 동작 (size 10, 20)
- 허용되지 않는 size 값 처리
- 각 searchType별 검색 동작
- totalPostCount, totalCommentCount 정확성
- isNew 플래그 동작
- CLAUDE.md의 테스트 규칙을 따를 것

구현 완료 후 `./gradlew test`로 테스트가 통과하는지 확인해줘.

CLAUDE.md에 정의된 아키텍처 규칙을 반드시 따라서 구현해줘.
