좋아요(Like) 기능을 구현해줘.

## 구현할 API

상위 디렉토리의 `api-spec.md`를 참조하여 아래 API를 구현해줘:

1. **좋아요** (POST /api/posts/{postId}/likes)
   - `X-Guest-Id` 헤더(UUID)로 사용자를 식별한다
   - `X-Guest-Id` 헤더가 없으면 400 Bad Request
   - 이미 좋아요한 상태에서 재요청 시 409 Conflict
   - 좋아요 후 해당 게시글의 likeCount와 liked 상태를 반환

2. **좋아요 취소** (DELETE /api/posts/{postId}/likes)
   - `X-Guest-Id` 헤더(UUID)로 사용자를 식별한다
   - `X-Guest-Id` 헤더가 없으면 400 Bad Request
   - 좋아요하지 않은 상태에서 요청 시 409 Conflict
   - 취소 후 해당 게시글의 likeCount와 liked 상태를 반환

3. **게시글 상세 조회 수정** (GET /api/posts/{id})
   - 기존 응답에 `liked` 필드를 추가한다
   - `X-Guest-Id` 헤더가 있으면 해당 guest의 좋아요 여부를 반환
   - `X-Guest-Id` 헤더가 없으면 `liked`는 false
   - `likeCount`를 실제 좋아요 수로 반환

4. **게시글 삭제 시**
   - 게시글 삭제 시 연관된 좋아요 데이터도 함께 삭제

## 테스트

구현한 API에 대한 테스트 코드를 작성해줘:
- 좋아요 / 좋아요 취소 정상 동작
- 중복 좋아요 시 409
- 좋아요하지 않은 상태에서 취소 시 409
- X-Guest-Id 헤더 없을 때 400
- 게시글 상세에서 liked 필드 정확성
- CLAUDE.md의 테스트 규칙을 따를 것

구현 완료 후 `./gradlew test`로 테스트가 통과하는지 확인해줘.

CLAUDE.md에 정의된 아키텍처 규칙을 반드시 따라서 구현해줘.
