# Experiment Results: [clean]

## Phase 1: 프로젝트 초기 세팅 + 게시글 CRUD

| Metric              | Value |
|---------------------|---|
| Input Tokens        | 36 |
| Output Tokens       | 7,540 |
| Total Tokens        | 1,352,064 |
| Cache Creation      | 34,647 |
| Cache Read          | 1,309,841 |
| Tool Calls          | 33 |
| Conversation Turns  | 58 |
| Build Success       | Y |
| API Test Pass       | Y |
| Notes               | tools: Bash:10, TaskUpdate:8, Write:7, TaskCreate:6, Read:1, Edit:1 |

## Phase 2: 댓글 기능

| Metric              | Value |
|---------------------|---|
| Input Tokens        | 3,991 |
| Output Tokens       | 17,952 |
| Total Tokens        | 1,729,946 |
| Cache Creation      | 52,846 |
| Cache Read          | 1,655,157 |
| Tool Calls          | 31 |
| Conversation Turns  | 66 |
| Build Success       | Y |
| API Test Pass       | Y |
| Notes               | tools: TaskUpdate:8, TaskCreate:7, Write:6, Edit:5, Bash:3, Read:2 |

## Phase 3: 게시글 목록 + 검색

| Metric              | Value |
|---------------------|---|
| Input Tokens        | 4,007 |
| Output Tokens       | 11,719 |
| Total Tokens        | 2,531,817 |
| Cache Creation      | 49,181 |
| Cache Read          | 2,466,910 |
| Tool Calls          | 47 |
| Conversation Turns  | 60 |
| Build Success       | Y |
| API Test Pass       | Y |
| Notes               | tools: TaskUpdate:14, Edit:11, TaskCreate:7, Read:5, Bash:5, Write:4, Grep:1 |

## Phase 4: 좋아요 기능

| Metric              | Value |
|---------------------|---|
| Input Tokens        | 3,988 |
| Output Tokens       | 19,229 |
| Total Tokens        | 1,913,210 |
| Cache Creation      | 60,831 |
| Cache Read          | 1,829,162 |
| Tool Calls          | 28 |
| Conversation Turns  | 55 |
| Build Success       | Y |
| API Test Pass       | Y |
| Notes               | tools: Bash:7, Write:6, Read:4, TaskUpdate:4, Edit:4, TaskCreate:3 |

## Phase 5: 비밀번호 변경 기능 (기능 변경)

| Metric              | Value |
|---------------------|---|
| Input Tokens        | 116 |
| Output Tokens       | 2,843 |
| Total Tokens        | 789,179 |
| Cache Creation      | 33,964 |
| Cache Read          | 752,256 |
| Tool Calls          | 18 |
| Conversation Turns  | 23 |
| Build Success       | Y |
| API Test Pass       | Y |
| Notes               | tools: Edit:10, Read:4, Write:2, Task:1, Bash:1 |

## Phase 6: 대댓글 기능 (기능 변경)

| Metric              | Value |
|---------------------|---|
| Input Tokens        | 380 |
| Output Tokens       | 18,283 |
| Total Tokens        | 3,696,764 |
| Cache Creation      | 64,453 |
| Cache Read          | 3,613,648 |
| Tool Calls          | 62 |
| Conversation Turns  | 94 |
| Build Success       | Y |
| API Test Pass       | Y |
| Notes               | tools: Edit:23, TaskUpdate:13, Bash:9, Read:7, Write:4, Glob:2, Grep:2, Task:1, TaskCreate:1 |

## Phase 7: 정렬 옵션 (기능 변경)

| Metric              | Value |
|---------------------|---|
| Input Tokens        | 116 |
| Output Tokens       | 10,239 |
| Total Tokens        | 877,864 |
| Cache Creation      | 40,501 |
| Cache Read          | 827,008 |
| Tool Calls          | 18 |
| Conversation Turns  | 25 |
| Build Success       | Y |
| API Test Pass       | Y |
| Notes               | tools: Edit:10, Read:4, Bash:2, Task:1, Glob:1 |

## Total Summary

| Metric              | Value |
|---------------------|-------|
| Total Input Tokens  | 12,634 |
| Total Output Tokens | 87,805 |
| Total Tokens        | 12,890,844 |
| Total Tool Calls    | 237 |
| Total Turns         | 381 |
