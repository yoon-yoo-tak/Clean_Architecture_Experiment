댓글에 대댓글(답글) 기능을 추가해줘.

## 변경 요구사항

기존 댓글 시스템을 확장하여 **댓글에 대한 답글**을 달 수 있게 한다. 답글은 1단계만 허용한다 (답글에 답글 불가).

### API 변경사항

#### 1. 답글 작성

`POST /api/posts/{postId}/comments/{commentId}/replies`

**Request Body:**
```json
{
  "author": "string (필수, 1~50자)",
  "password": "string (필수, 4자 이상)",
  "content": "string (필수)"
}
```

**Response:** `201 Created`
```json
{
  "id": 2,
  "parentId": 1,
  "author": "답글작성자",
  "content": "답글 내용",
  "createdAt": "2024-01-02T00:00:00",
  "deleted": false
}
```

- 부모 댓글이 이미 답글인 경우 (parentId가 있는 경우): `400 Bad Request`
- 부모 댓글이 삭제된 경우에도 답글 작성 가능

#### 2. 댓글 목록 조회 변경

`GET /api/posts/{postId}/comments` 응답에서:

```json
{
  "content": [
    {
      "id": 1,
      "parentId": null,
      "author": "댓글작성자",
      "content": "댓글 내용",
      "createdAt": "2024-01-02T00:00:00",
      "deleted": false,
      "replyCount": 3
    }
  ],
  "page": 0,
  "size": 5,
  "totalElements": 12,
  "hasMore": true
}
```

- `parentId`: 답글인 경우 부모 댓글 ID, 일반 댓글은 `null`
- `replyCount`: 해당 댓글의 답글 수 (삭제된 답글 제외)
- 목록에는 **일반 댓글만** 표시 (답글은 별도 조회)

#### 3. 답글 목록 조회 (새 API)

`GET /api/posts/{postId}/comments/{commentId}/replies`

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page      | int  | 0       | 페이지 번호 |
| size      | int  | 5       | 페이지 크기 |

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 2,
      "parentId": 1,
      "author": "답글작성자",
      "content": "답글 내용",
      "createdAt": "2024-01-02T00:00:00",
      "deleted": false
    }
  ],
  "page": 0,
  "size": 5,
  "totalElements": 3,
  "hasMore": false
}
```

- 정렬: 오래된순 (createdAt ASC) — 대화 흐름 유지
- 삭제된 답글은 `"삭제된 댓글입니다."` 처리 동일

#### 4. 댓글 삭제 시
- 답글이 있는 댓글을 삭제해도 답글은 유지됨
- 답글 삭제는 기존 댓글 삭제 API와 동일하게 동작

### 기존 API 영향
- `GET /api/posts/{id}` (게시글 상세): comments 필드에 `replyCount` 추가
- `commentCount`는 일반 댓글 + 답글 전체 수

## 테스트

- 답글 작성 정상 동작
- 답글에 답글 시도 시 400
- 답글 목록 조회 (오래된순 정렬)
- 삭제된 부모 댓글에 답글 작성 가능
- replyCount 정확성
- 부모 댓글 삭제 후 답글 유지 확인
- CLAUDE.md의 테스트 규칙을 따를 것

구현 완료 후 `./gradlew test`로 테스트가 통과하는지 확인해줘.

CLAUDE.md에 정의된 아키텍처 규칙을 반드시 따라서 구현해줘.
