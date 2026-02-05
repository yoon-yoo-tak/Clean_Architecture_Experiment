# Anonymous Board API Specification

## 공통 사항

- Base URL: `/api`
- Content-Type: `application/json`
- 비밀번호 검증 실패 시: `403 Forbidden`
- 리소스 없음: `404 Not Found`
- 유효성 검증 실패: `400 Bad Request`
- 좋아요 사용자 식별: `X-Guest-Id` 헤더 (UUID)

---

## 1. 게시글 CRUD

### 1.1 게시글 작성

`POST /api/posts`

**Request Body:**
```json
{
  "title": "string (필수, 1~200자)",
  "content": "string (필수)",
  "author": "string (필수, 1~50자)",
  "password": "string (필수, 4자 이상)",
  "hashtags": ["string"] // 선택, 최대 5개, 각 태그 1~30자
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "title": "제목",
  "content": "내용",
  "author": "작성자",
  "hashtags": ["태그1", "태그2"],
  "viewCount": 0,
  "likeCount": 0,
  "commentCount": 0,
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00"
}
```

### 1.2 게시글 상세 조회

`GET /api/posts/{id}`

- 호출 시 조회수(viewCount) 1 증가

**Response:** `200 OK`
```json
{
  "id": 1,
  "title": "제목",
  "content": "내용",
  "author": "작성자",
  "hashtags": ["태그1", "태그2"],
  "viewCount": 43,
  "likeCount": 0,
  "commentCount": 0,
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00"
}
```

- 이 단계에서는 댓글, 좋아요 관련 필드는 기본값(0, 빈 목록)으로 반환

### 1.3 게시글 수정

`PUT /api/posts/{id}`

**Request Body:**
```json
{
  "title": "수정된 제목",
  "content": "수정된 내용",
  "password": "비밀번호",
  "hashtags": ["태그1", "태그3"]
}
```

- 비밀번호 일치 시에만 수정 가능

**Response:** `200 OK` (게시글 상세와 동일한 형태, 단 viewCount 증가 없음)

### 1.4 게시글 삭제

`DELETE /api/posts/{id}`

**Request Body:**
```json
{
  "password": "비밀번호"
}
```

- 비밀번호 일치 시에만 삭제 (Hard Delete)

**Response:** `204 No Content`

---

## 2. 댓글 (Comments)

### 2.1 댓글 작성

`POST /api/posts/{postId}/comments`

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
  "id": 1,
  "author": "댓글작성자",
  "content": "댓글 내용",
  "createdAt": "2024-01-02T00:00:00",
  "deleted": false
}
```

### 2.2 댓글 목록 조회 (더보기)

`GET /api/posts/{postId}/comments`

**Query Parameters:**
| Parameter | Type | Default | Description           |
|-----------|------|---------|-----------------------|
| page      | int  | 0       | 페이지 번호 (0-based) |
| size      | int  | 5       | 페이지 크기 (고정 5)  |

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "author": "댓글작성자",
      "content": "댓글 내용",
      "createdAt": "2024-01-02T00:00:00",
      "deleted": false
    }
  ],
  "page": 0,
  "size": 5,
  "totalElements": 12,
  "hasMore": true
}
```

- 정렬: 최신순 (createdAt DESC)
- `deleted`가 `true`인 댓글은 `content`를 `"삭제된 댓글입니다."` 로 반환

### 2.3 댓글 삭제

`DELETE /api/posts/{postId}/comments/{commentId}`

**Request Body:**
```json
{
  "password": "비밀번호"
}
```

- 비밀번호 일치 시 Soft Delete (deleted 플래그 처리)

**Response:** `204 No Content`

### 2.4 게시글 상세 조회에 댓글 포함

`GET /api/posts/{id}` 응답이 아래와 같이 확장된다:

```json
{
  "id": 1,
  "title": "제목",
  "content": "내용",
  "author": "작성자",
  "hashtags": ["태그1", "태그2"],
  "viewCount": 43,
  "likeCount": 0,
  "commentCount": 5,
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00",
  "comments": {
    "content": [
      {
        "id": 10,
        "author": "댓글작성자",
        "content": "댓글 내용",
        "createdAt": "2024-01-02T00:00:00",
        "deleted": false
      }
    ],
    "page": 0,
    "size": 5,
    "totalElements": 12,
    "hasMore": true
  }
}
```

- `comments`: 최신순 5건
- `deleted`가 `true`인 댓글은 `content`를 `"삭제된 댓글입니다."` 로 반환
- `commentCount`: 삭제되지 않은 댓글의 총 수
- 게시글 삭제 시 연관 댓글도 함께 삭제

---

## 3. 게시글 목록 + 검색

### 3.1 게시글 목록 조회

`GET /api/posts`

**Query Parameters:**
| Parameter  | Type   | Default | Description                                |
|------------|--------|---------|--------------------------------------------|
| page       | int    | 0       | 페이지 번호 (0-based)                      |
| size       | int    | 10      | 페이지 크기 (10 또는 20만 허용)            |
| searchType | string | -       | 검색 유형: title, author, hashtag, content  |
| keyword    | string | -       | 검색 키워드                                |

**Response:** `200 OK`
```json
{
  "totalPostCount": 100,
  "totalCommentCount": 523,
  "posts": [
    {
      "id": 1,
      "title": "제목",
      "author": "작성자",
      "createdAt": "2024-01-01T00:00:00",
      "commentCount": 5,
      "viewCount": 42,
      "likeCount": 3,
      "isNew": true
    }
  ],
  "page": 0,
  "size": 10,
  "totalPages": 10,
  "totalElements": 100
}
```

- `totalPostCount`: 전체 게시글 수 (검색 필터 무관)
- `totalCommentCount`: 전체 댓글 수 (검색 필터 무관, 삭제된 댓글 제외)
- `isNew`: 작성일이 현재 기준 3일 이내이면 `true`
- 정렬: 최신순 (createdAt DESC)

### 3.2 검색

- `searchType`과 `keyword`가 모두 있을 때만 검색 적용
- title, author, content: 부분 일치 검색 (LIKE '%keyword%')
- hashtag: 해시태그 이름과 정확히 일치

---

## 4. 좋아요 (Likes)

### 4.1 좋아요

`POST /api/posts/{postId}/likes`

**Required Header:** `X-Guest-Id: {uuid}`

- `X-Guest-Id` 헤더가 없으면: `400 Bad Request`
- 이미 좋아요한 상태에서 재요청 시: `409 Conflict`

**Response:** `200 OK`
```json
{
  "likeCount": 4,
  "liked": true
}
```

### 4.2 좋아요 취소

`DELETE /api/posts/{postId}/likes`

**Required Header:** `X-Guest-Id: {uuid}`

- `X-Guest-Id` 헤더가 없으면: `400 Bad Request`
- 좋아요하지 않은 상태에서 요청 시: `409 Conflict`

**Response:** `200 OK`
```json
{
  "likeCount": 3,
  "liked": false
}
```

### 4.3 게시글 상세 조회에 좋아요 포함

`GET /api/posts/{id}` 응답에 아래 필드가 추가된다:

- `liked`: `X-Guest-Id` 헤더 기준으로 좋아요 여부 (`true`/`false`)
- `X-Guest-Id` 헤더가 없으면 `liked`는 `false`
- `likeCount`: 실제 좋아요 수
- 게시글 삭제 시 연관 좋아요 데이터도 함께 삭제
