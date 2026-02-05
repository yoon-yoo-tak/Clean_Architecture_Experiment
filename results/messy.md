# Experiment Results: [messy]

## Phase 1: 프로젝트 초기 세팅 + 게시글 CRUD

| Metric              | Value |
|---------------------|---|
| Input Tokens        | 19 |
| Output Tokens       | 18,785 |
| Total Tokens        | 694,743 |
| Cache Creation      | 31,979 |
| Cache Read          | 643,960 |
| Tool Calls          | 16 |
| Conversation Turns  | 37 |
| Build Success       | Y |
| API Test Pass       | Y |
| Notes               | tools: Bash:5, TaskUpdate:5, TaskCreate:2, Write:2, Read:1, Edit:1 |

## Phase 2: 댓글 기능

| Metric              | Value |
|---------------------|---|
| Input Tokens        | 3,998 |
| Output Tokens       | 5,093 |
| Total Tokens        | 1,860,342 |
| Cache Creation      | 39,056 |
| Cache Read          | 1,812,195 |
| Tool Calls          | 38 |
| Conversation Turns  | 44 |
| Build Success       | Y |
| API Test Pass       | Y |
| Notes               | tools: TaskUpdate:14, TaskCreate:7, Bash:6, Write:5, Edit:4, Read:2 |

## Phase 3: 게시글 목록 + 검색

| Metric              | Value |
|---------------------|---|
| Input Tokens        | 38 |
| Output Tokens       | 12,201 |
| Total Tokens        | 1,901,569 |
| Cache Creation      | 45,460 |
| Cache Read          | 1,843,870 |
| Tool Calls          | 36 |
| Conversation Turns  | 47 |
| Build Success       | Y |
| API Test Pass       | Y |
| Notes               | tools: TaskUpdate:12, Edit:8, Bash:7, TaskCreate:6, Task:2, Read:1 |

## Phase 4: 좋아요 기능

| Metric              | Value |
|---------------------|---|
| Input Tokens        | 3,981 |
| Output Tokens       | 9,247 |
| Total Tokens        | 1,016,678 |
| Cache Creation      | 36,851 |
| Cache Read          | 966,599 |
| Tool Calls          | 21 |
| Conversation Turns  | 37 |
| Build Success       | Y |
| API Test Pass       | Y |
| Notes               | tools: TaskUpdate:7, Edit:5, Write:3, Bash:3, Read:2, TaskCreate:1 |

## Phase 5: 비밀번호 변경 기능 (기능 변경)

| Metric              | Value |
|---------------------|---|
| Input Tokens        | 44 |
| Output Tokens       | 2,998 |
| Total Tokens        | 249,733 |
| Cache Creation      | 25,190 |
| Cache Read          | 221,501 |
| Tool Calls          | 6 |
| Conversation Turns  | 10 |
| Build Success       | Y |
| API Test Pass       | Y |
| Notes               | tools: Edit:3, Glob:1, Read:1, Bash:1 |

## Phase 6: 대댓글 기능 (기능 변경)

| Metric              | Value |
|---------------------|---|
| Input Tokens        | 110 |
| Output Tokens       | 4,759 |
| Total Tokens        | 790,704 |
| Cache Creation      | 38,231 |
| Cache Read          | 747,604 |
| Tool Calls          | 17 |
| Conversation Turns  | 23 |
| Build Success       | Y |
| API Test Pass       | Y |
| Notes               | tools: Edit:9, Read:4, Bash:3, Task:1 |

## Phase 7: 정렬 옵션 (기능 변경)

| Metric              | Value |
|---------------------|---|
| Input Tokens        | 107 |
| Output Tokens       | 5,217 |
| Total Tokens        | 394,290 |
| Cache Creation      | 32,430 |
| Cache Read          | 356,536 |
| Tool Calls          | 9 |
| Conversation Turns  | 16 |
| Build Success       | Y |
| API Test Pass       | Y |
| Notes               | tools: Edit:5, Read:2, Glob:1, Bash:1 |

## Total Summary

| Metric              | Value |
|---------------------|-------|
| Total Input Tokens  | 8,297 |
| Total Output Tokens | 58,300 |
| Total Tokens        | 6,908,059 |
| Total Tool Calls    | 143 |
| Total Turns         | 214 |
