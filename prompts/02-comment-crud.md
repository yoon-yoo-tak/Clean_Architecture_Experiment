댓글 기능을 구현해줘.

## 구현할 API

상위 디렉토리의 `api-spec.md`를 참조하여 아래 API를 구현해줘:

1. **댓글 작성** (POST /api/posts/{postId}/comments)
   - 작성자, 비밀번호, 내용을 입력받는다
   - 비밀번호는 BCrypt로 암호화하여 저장한다
   - 존재하지 않는 게시글에 댓글 작성 시 404

2. **댓글 목록 조회** (GET /api/posts/{postId}/comments)
   - 최신순 정렬, 5건씩 페이징
   - `deleted`가 true인 댓글은 content를 `"삭제된 댓글입니다."`로 반환
   - `hasMore`로 다음 페이지 존재 여부 반환

3. **댓글 삭제** (DELETE /api/posts/{postId}/comments/{commentId})
   - 비밀번호 일치 시 Soft Delete (deleted 플래그 true 처리)
   - 실제 데이터는 삭제하지 않음

4. **게시글 상세 조회 수정** (GET /api/posts/{id})
   - 기존 게시글 상세 응답에 댓글 정보를 포함시킨다
   - 최신 5건의 댓글을 함께 반환
   - `deleted`가 true인 댓글은 content를 `"삭제된 댓글입니다."`로 반환
   - commentCount 필드를 실제 댓글 수로 반환

## 테스트

구현한 API에 대한 테스트 코드를 작성해줘:
- 각 엔드포인트의 정상 케이스
- 주요 에러 케이스 (존재하지 않는 게시글, 비밀번호 불일치, 삭제된 댓글 표시 등)
- CLAUDE.md의 테스트 규칙을 따를 것

구현 완료 후 `./gradlew test`로 테스트가 통과하는지 확인해줘.

CLAUDE.md에 정의된 아키텍처 규칙을 반드시 따라서 구현해줘.
